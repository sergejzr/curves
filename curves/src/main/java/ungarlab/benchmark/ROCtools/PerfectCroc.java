/*  c 2002,2003 Andrew I. Schein

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 

*/

/*
 This code makes a CROC curve using the empirical distribution
 (i.e. no smoothing of the curve is performed).
 also this code outputs the area under the curve, printing
 this value to the screen.

 The code implements the CROC curve described in `Evaluating Hot- and 
 Cold-Start Recommendations' 

  usage: java -Xmx450M ungarlab.benchmark.ROCtools.PerfectCroc infile outfile

  Expected input format is: person movie prediction answer
  on each line

  Sample input:

  

1       5       5.279852796685874E-6    1.0
1       8       2.048197750270712E-6    1.0
1       9       4.499078432591497E-6    1.0
1       16      1.2768533533381445E-6   0.0
1       19      4.1970116150293884E-7   1.0
1       30      4.647299156252203E-6    0.0
1       33      2.28907966203729E-6     1.0
1       36      2.1706362708847925E-6   1.0


*/


/* Assumptions (or lack of):
 1. Want to plot more/fewer points?  Adjust the variable jump.
 2. Ids can start at 0 or 1 or even higher, we keep track of 
  which p,m have a prediction and only compute using active pairs.

*/

package ungarlab.benchmark.ROCtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import ungarlab.util.Debug;

public class PerfectCroc {
    
    int maxPerson = 0;  // What is the maximum id used for a person
    int maxMovie  = 0;  // What is the maxumum id used for a movie
    String sep = "\t";  // separates the points 1.0sep1.0 in the output
    
    String infile;
    String outfile;
    int totalGood,totalBad,grandTotal = 0;


    double[][] prediction;  // stores predictions for each person, movie pair
    boolean[][] active;  // Did this person,movie pair occur in the input file?

    int[] counters; // Each person gets their own counter.
    boolean[][] answer;      // stores the correct answer for each person, movie pair
                             // true = "success" false = "failure"
    
    int[][] movies;      //[p][count] keep track of movie orderings for each person
    // because we will be sorting the prediction[person],active[person] and answer[person]
    // arrays according to the prediction value.

    // At one point I included a header line to indicate the maxMovie, maxPerson
    // values. I don't set this to true anymore
    
    static boolean expect_header = false;
    
    double area = 0.0;  // This variable global across the class instance will
    // keep track of the area under the curve.

    
    /* *************ATTENTION********************** 
       This _jump_ variable lets you control the number of points plotted.
       Sorry to make u alter the source code to get at it. 
       
       jump acts as the increment i.e. make (previous + jump) recommendations
       to each user and plot a point on the CROC curve.

    */

    private int jump = 15;
    
    

    private void fill_array() {
	
	//Here is where we read in the data
	
	
	File inFile = new File(infile);
	
	try {
	FileReader fs = new FileReader(inFile);
	
	int size,count  = 0;
	String line;
	BufferedReader in = new BufferedReader(fs);
	java.util.StringTokenizer st;
	
	double temp;
	int tempInt;

	if (expect_header) {
	    // If there is a header line of some sort, we have to skip a line
	    if ((line = in.readLine() ) != null) {
		
	    }
	    else {throw new RuntimeException("Touble reading , delimted header");}    
	}
	

	//We read the data twice.  The first time we just compute statistics
	//We read the data once below.
	size = 0;
	System.out.print("Making Pass 1... ");
	while ((line = in .readLine()) != null) {
	    st = new java.util.StringTokenizer(line);
	    tempInt = new Integer(st.nextToken()).intValue();
	    if (tempInt >= maxPerson) {
		maxPerson = tempInt + 1;
	    }
	    tempInt = new Integer(st.nextToken()).intValue();
	    if (tempInt >= maxMovie) 
		maxMovie = tempInt + 1;
	    
	    size++;
	}
	System.out.println("complete.");
	
	System.out.println("There are " + size + " rows. \n");
      	System.out.println("The largest movie id is: " + maxMovie + "\nThe largest person id is: " + maxPerson);
	System.out.println("Making Pass 2... Progress:");
	System.out.println("START----------COMPLETE");
	System.out.print("     ");
	
	in.close();
	
	// Now we reinit things to read from the file again
	fs = new FileReader(inFile);
	in = new BufferedReader(fs);
	
	prediction = new double[maxPerson][maxMovie];  
	answer = new boolean[maxPerson][maxMovie];
	active = new boolean[maxPerson][maxMovie];
	
	for (int row= 0; row < maxPerson; row++) 
	    for (int col = 0; col < maxMovie; col++) {
		answer[row][col] = false;
		active[row][col] = false;
	    }
	    


	movies = new int[maxPerson][maxMovie]; // will keep track of order 
	// after sorting.
	counters = new int[maxPerson];
	// each person gets their own counter

	//	count = 0;
	
	//Once again, we have to get read of the header line if its ther.
	if (expect_header)
	    if ((line = in.readLine()) != null) {} // Get rid of header line.
	
	//Now we read and store the contents of infile
	
	while ((line = in.readLine()) != null) {
	  
	    grandTotal++;
	    if (grandTotal % (size / 10) == 0) {
		System.out.print("*");
	    }
	    
	    st = new java.util.StringTokenizer(line);
	    int person = new Integer(st.nextToken()).intValue();
	    int movie  = new Integer(st.nextToken()).intValue();
	    
	    active[person][movie] = true;
	    prediction[person][movie] = new Double(st.nextToken()).doubleValue();
	    
	    if (new Double(st.nextToken()).doubleValue() > 0.5) {
		answer[person][movie] = true;
	    
	    } else {
		answer[person][movie] = false;
	    }	
	    

	    
	    // Keep track of how many negatives 'bad' and positives 'good'
	    // are in the data set.   We will need this later for computing
	    // sensitivity and specificity.
	    
	    if (!answer[person][movie]) {
		totalBad++;
	    }
	    else {totalGood++; }
	}
	
	System.out.println("\nDone reading data.");
	
	in.close();
	}
	catch (java.io.FileNotFoundException e ) {
	    System.out.println(e);
	    throw new RuntimeException();
	}
	catch (IOException e) {
	    System.out.println(e);
	    throw new RuntimeException();
	}
	
	
	// now we initialize the movies array
	for (int person = 0; person < maxPerson; person++) 
	    for (int movie = 0; movie < maxMovie; movie++) 
		movies[person][movie] = movie;
	
	// Later when we sort movies by a prediction, we will 
	// need to keep an index of where they come from. movies
	// helps us do that.

	 Debug.assertm(totalGood + totalBad == grandTotal);
	System.out.println("The number of positive examples: " + totalGood);
	System.out.println("The number of negative examples: " + totalBad);
	System.out.println("The number of observations:      " + grandTotal);
    } 
    
    
    private void process_list() {
	
	try {
	    
	    File outFile = new File(outfile);
	    
	    FileOutputStream OS = new FileOutputStream(outFile);
	    PrintStream out = new PrintStream(OS);
	    
	    int person = 0; 
	 	
	    int count = 0;
	    int countPos = 0;
	    int countNeg = 0;
	    boolean truth;
	    double sens, spec, oldfp;   // 
	    double fp = 0.5; // false pos rate
	    double length,width;
	    
	    oldfp = 0;
	    
	
	    sens = 0;  
	    spec = 0;
	

	    if (Debug.status) {
		// This is piece of code was put in at one point to track down counts of 
		// tps and fps.
		
		
		int total = 0;
		int badtotal = 0;
		for (person = 1; person < answer.length; person++) {
		    for (int movie = 0; movie < maxMovie; movie++) 
			if (active[person][movies[person][movie]]) {
			    
			    if (answer[person][movies[person][movie]]) {
				total++;
			    }
			    else { badtotal++; }
			}
		}
		// Now total contains the total positive examples.
		
		//Debug.println("check1 goodtotal is: " + total + " badtotal is: " + badtotal);
	}
	    
	    
	    System.out.println("Calculating Points...");
	    
	    // ROC curves start with a point at 0,0
	    
	    out.println("0" + sep + "0");
	
	// The loop conditions specify that we recommend at most maxMovie items
	// and that we stop when fp and sens reach 1
	
	    while (count < maxMovie && (fp < 1 || sens < 1)) {
		//while (count < maxMovie ) {
		// Let count range over "ranks" of movies in the ranked list.
		
		person = 0;  
		
		// Let person range over the set of people
		//while (person < answer.length) {   
		while (person < answer.length) {   
		    if (count != 0) {
			counters[person]++;
		    }
		    
		    // just for clarification: movies[person][counters[person]]
		    // contains the movie id of the movie in the 
		    // answer [person][counters[person]] slot
		    
		    while (counters[person] < answer[person].length && !active[person][movies[person][counters[person]]]) {
		    // This loop skips over inactive person,movie pairs
		    // for instance, if (user=andy,movie=total recall) never appears 
		    // in the input file we don't make a recommendation over this movie
		    // so we increment the counter in search of a movie that _is_ active
		    // in the test set for andy.
			counters[person]++;
		}
		    
		if (counters[person] < answer[person].length) {
		    // So here we check to make sure we aren't "done" recommending 
		    // movies to this user. 
		    // We finish early for users who have fewer observations in the 
		    // test set than other users.
		    
		     Debug.assertm(active[person][movies[person][counters[person]]]); 
		    // The while loop and if statement above should ensure that if we get
		    // to this point, we are about to make a prediction for a person,movie
		    // pair that is represented in the test set.
		    
		     Debug.assertm(answer[person].length == prediction[person].length);
		    // This check is just pure paranoia, I guess.
		    
		    truth = answer[person][counters[person]]; 
			// Here it is: we predict person has seen/rated favorably 
			// movie[person][counters[person]]
			// now are we right? ...

			if (truth) 
			    countPos++;
			else
			    countNeg++;
			
		}
		person++;
		}
		count++;
	    
		sens = (double) countPos / (double) totalGood;
		spec = (double) ( totalBad - countNeg ) / ((double) totalBad  )  ;
		/*
		  Debug.println("totalBad: " + totalBad + " countNeg " + countNeg);
		  Debug.println("totalGood: " + totalGood + " and countPos is: " + countPos) ;
	    
		  Debug.println("spec is: " + spec);
		  Debug.println("sens is: " + sens);
		  
		  Debug.println("count is: " + count);
		*/
		 Debug.assertm(spec >= 0 && spec <= 1);
	     Debug.assertm(sens >= 0 && sens <= 1);
	    
	     Debug.assertm(totalBad >= countNeg);
	     Debug.assertm(totalGood >= countPos);
	    
	    fp = 1 - spec;
	    width = fp - oldfp;  // For computing area
	    
	    area += width * sens;
	    
	    if (count % jump == 0) {
		out.println(fp + sep + sens); // Here is where we write a point
	    }
	     Debug.assertm(spec >= 0 && spec <= 1);
	     Debug.assertm(sens >= 0 && sens <= 1);
	    
	    oldfp = fp;
	    
	    }
       	
	    if (spec > 0 || sens < 1) {

		// Last point to plot is 1,1
		out.println("1.0" + sep + "1.0");
	    }	

	    //close file buffer.
	out.close();
	
	// We run some checks:

	 Debug.assertm(totalGood == countPos);
	//System.out.println(totalBad + " " + countNeg);
	 Debug.assertm(totalBad  == countNeg);
	
	}
	catch (java.io.FileNotFoundException e ) {
	    System.out.println(e);
	    throw new RuntimeException();
	}
	catch (IOException e) {
	    System.out.println(e);
	    throw new RuntimeException();
	}
	

    }
    
    
    public PerfectCroc(String infile, String outfile) {
	// We will read predictions from infile
	// and write CROC curve points to outfile
	// Then use your favorite tool (Splus, R, gnuplot)
	// to plot the points.
 	
	int progress;


	this.infile = infile;
	this.outfile = outfile;
	
	// Here is where we read the data into various arrays.
	fill_array();
	
	 Debug.assertm(answer.length == prediction.length);
	
	progress = answer.length / 10;
	System.out.println("Sorting Predictions... Progress:");
	System.out.println("START----------COMPLETE");
	System.out.print("     ");
	
	
	// For each person we sort the movies by prediction values
	for (int person = 0; person < answer.length; person++) {
	    
	    IntComparator myCompare = (IntComparator) new MyComparator(answer[person]);
	    
	    // In the swapper we also pass movies, which is an index telling us
	    // which movie got movied to what position in the ordering.
	    
	    Swapper mySwapper = (Swapper) new MySwapper(answer[person],prediction[person],movies[person]);
	    
	    // We use the cern.colt sorting libraries.

	    cern.colt.GenericSorting.quickSort(0, answer[person].length ,myCompare, mySwapper);
	    
	    if ((person + 1) % progress == 0) {
		System.out.print("*");

	    }
 
	}
	System.out.println();
	
	// debug_write() is just for debugging purposes.
	// I don't think I'll need it anymore.
	//debug_write();

	// Here is where we compute the points and calculate the area.
	process_list();
	
	//Output the results
	System.out.println("Curve constructed using " + jump + " observations per CROC point.");
	
	System.out.println("Area for " + outfile + ": " + area + "\n");
	// all done
    } 
    

    public void debug_write() {
	 
	try {
	    
	    FileOutputStream myFile = new FileOutputStream("debugout.txt");
	    PrintStream out = new PrintStream(myFile);
	    
	    out.println("Rows: " + maxPerson + " Columns: " + maxMovie);
	    int count = 0;
	    int value = 0;
	    for (int i = 1; i < maxPerson; i++) {
		for (int j = 1; j < maxMovie; j++) { 
		    out.println(i + "\t" + j + "\t" + prediction[i][j] + "\t" + active[i][j]);
		}}
		
	
	    
	    out.close();
	    
	}
	catch (IOException e) {
	    
	    System.out.println(e);
	    throw new RuntimeException();

	}
	
	

}


    public static void main(String argv[]) {
	
	//System.out.println("ctesting");
	if (argv.length < 2) {
	    throw new RuntimeException("Usage java EmpiricalROCMaker infile outfile");
	}
	
	PerfectCroc myObj = new PerfectCroc(argv[0],argv[1]);
	
	

    }


    // The classes below allow us to use Colt's Sorting routines. 
    // See the Colt documentation for more info.

    class MySwapper implements cern.colt.Swapper {
    private boolean[] answer;
    private double[] prediction;
    private int[] movies;

    public MySwapper(boolean[] answer, double[] prediction, int[] movies) {
	this.answer = answer;
	this.prediction = prediction;
	this.movies = movies;
    }
    
    public void swap(int a, int b) {
	
	boolean temp;
	temp = answer[a];
	answer[a] = answer[b];
	answer[b] = temp;
	

	double tempdouble = prediction[a];
	prediction[a] = prediction[b];
	prediction[b] = tempdouble;

	int tempint = movies[a];
	movies[a]= movies[b];
	movies[b] = tempint;

    }
}

 



    class MyComparator implements cern.colt.function.IntComparator {
	
	
	private boolean[] prediction;
       

	public MyComparator(boolean[] prediction) {
	    
	    this.prediction = prediction;
	
	}


	public int compare(int i, int j) {
	    
	    if (prediction[i]) {
		if (prediction[j]) { // they are same
		    return 0;
		}
		return -1;  // prediction[i] > prediction[j]
	    }
	    else {  // we have ! prediction[i]
		if (!prediction[j]) { // they are same
		    return 0;
		}	
		return 1; // prediction[i] < prediction[j]
	    }
	    
	    // Debug.assertm(false); // We shouldn't get here!
	    
	    /*
	    if (prediction[i] < prediction[j]) {
	    
		return 1;
	    }
	    else {if (prediction[i] > prediction[j]) {
		return -1;

	    } else {
		return 0;
	    }}
	    
	    */
	    }
	    
    }

    
}
