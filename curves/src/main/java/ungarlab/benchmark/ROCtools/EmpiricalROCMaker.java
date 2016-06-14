/* c 2002 Andrew I. Schein


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


 This code makes an ROC curve using the empirical distribution.

 This code can be used to make the GROC curves of:

  Andrew I. Schein, Alexandrin Popescul, Lyle H. Ungar, and David M. Pennock. Methods and Metrics for Cold-Start Recommendations. Submitted. 

 The code can handle as much data as can fit in memory.  


There is a paramter jump that determines the number of points drawn.  Feel free to set this to any positive integer.

Expected input format is:  
pred answer
.
.
.
*/

package ungarlab.benchmark.ROCtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import l3s.curves.ValidationInformationPrecRec;
import ungarlab.util.Debug;

public class EmpiricalROCMaker {

    String infile;
    String outfile;
    int totalGood,totalBad,grandTotal = 0;
    int numberofpoints;
    
    double[] prediction;
    double[] answer;
    

    double area = 0.0;

    private int jump =  1;  // This parameter (to a positive value) to determine number of points it is set by number of points
                           // plotted.
    final String sep = "\t";

    
    private void process_list_data() {

    

    	 
    	  int count = 0;
    	  int countPos = 0;
    	  int countNeg = 0;
    	  double guess, truth;
    	  double sens, spec, reverse;
    	double length,width;
    	reverse = 0;
    		
    	sens = 0;
    	spec = 0;

    	System.out.println("ROC");
    	System.out.println("0" + sep + "0");
    	while (count < grandTotal) {
    	    
    	     Debug.assertm(totalGood + totalBad == grandTotal);
    	     Debug.assertm(countPos + countNeg <= grandTotal);
    	    
    	    
    	    guess = prediction[count];
    	    truth = answer[count];
    	    
    	    if (truth > .999) {
    		countPos++;
    	    }
    	    else {countNeg++;}
    	    
    	    // Time to draw a point
    	    if (count % jump == 0 && count > 0) {
    		
    		
    		width  = reverse; // set width to old spec-- this is just an intermediate value in computing the width.
    		
    		sens = (double) countPos  / (double) totalGood;
    		spec = (double) (totalBad - countNeg) /  (double) totalBad;
    		reverse = 1 - spec;  // calculate the fp rate
    		
    		length = sens;
    		width = reverse - width; // get difference between old fp  and current for the real width.
    		
    		area += length * width;
    	     
    		 Debug.assertm(!Double.isNaN(area));
    		
    		
    		System.out.println(reverse + sep + sens);
    		 Debug.assertm(sens <= 1.0);
    		
    	    } 
    	    
    	    count++;
    	    
    	}
    	
    	System.out.println(1 + sep + 1);

    
    	

    	
    	
    	

        }
    
    private void process_list() {

	try {

	  File outFile = new File(outfile);
	
	  FileOutputStream OS = new FileOutputStream(outFile);
	  PrintStream out = new PrintStream(OS);
	  int count = 0;
	  int countPos = 0;
	  int countNeg = 0;
	  double guess, truth;
	  double sens, spec, reverse;
	double length,width;
	reverse = 0;
		
	sens = 0;
	spec = 0;

	
	out.println("0" + sep + "0");
	while (count < grandTotal) {
	    
	     Debug.assertm(totalGood + totalBad == grandTotal);
	     Debug.assertm(countPos + countNeg <= grandTotal);
	    
	    
	    guess = prediction[count];
	    truth = answer[count];
	    
	    if (truth > .999) {
		countPos++;
	    }
	    else {countNeg++;}
	    
	    // Time to draw a point
	    if (count % jump == 0 && count > 0) {
		
		
		width  = reverse; // set width to old spec-- this is just an intermediate value in computing the width.
		
		sens = (double) countPos  / (double) totalGood;
		spec = (double) (totalBad - countNeg) /  (double) totalBad;
		reverse = 1 - spec;  // calculate the fp rate
		
		length = sens;
		width = reverse - width; // get difference between old fp  and current for the real width.
		
		area += length * width;
	     
		 Debug.assertm(!Double.isNaN(area));
		
		
		out.println(reverse + sep + sens);
		 Debug.assertm(sens <= 1.0);
		
	    } 
	    
	    count++;
	    
	}
	
	out.println(1 + sep + 1);

	out.close();
	

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
    
    private void fill_array(Vector<ValidationInformationPrecRec> info) 
    {
    	
    	
    	if (numberofpoints < 1) {
    	    jump = 1;
    	} else jump = info.size() / numberofpoints;
    	
    	if (jump == 0) jump = 1;
    	
    	prediction = new double[info.size()];
    	answer = new double[info.size()];
    	
    	int cur=0;
    	for(ValidationInformationPrecRec r:info)
    	{
    		
    		prediction[cur]=r.getDistance();
    		answer[cur]=r.getClassLabel().compareTo("true")==0?1:0;
    		
    		
    		if(answer[cur]<1){totalBad++;}else{
    		totalGood++;
    		}
    		cur++;
    	}
    	
    	grandTotal = cur;
    	
    }
    private void fill_array() {

	// Here is where we load up the data in infile.
	
	File inFile = new File(infile);
	//File outFile = new File(outfile);
	try {
	FileReader fs = new FileReader(inFile);
	
	
	int size,count  = 0;
	String line;
	BufferedReader in = new BufferedReader(fs);
	java.util.StringTokenizer st;
	String tool,answerN;
	double temp;

	
	//Step 1: we are find to find out the size of the data
	
	size = 0;
	while ((line = in .readLine()) != null) {
	    size++;
	}
	
	System.out.println("Completed first pass of reading.\nThere are " + size + " rows \n");
      	
	if (numberofpoints < 1) {
	    jump = 1;
	} else jump = size / numberofpoints;
	
	if (jump == 0) jump = 1;
	
	in.close();
	fs = new FileReader(inFile);
	in = new BufferedReader(fs);
	
	// Now that we have size, we can create arrays to store values.
	prediction = new double[size];
	answer = new double[size];
	
	count = 0;
	
       
	while ((line = in.readLine()) != null) {
	    
	    st = new java.util.StringTokenizer(line,sep);
	    temp = new Double(st.nextToken()).doubleValue();
	  
	    prediction[count] = temp; // stroe prediction

	    temp = new Double(st.nextToken()).doubleValue(); // and answer
	    answer[count] = temp; 
		    
	    if (temp <= 0.00001) {  // Keep count of negative count
		totalBad++;
	    }
	    else {totalGood++; }  // and positive count
	  


	    count++;  // increment count
	}
	
	grandTotal = count;
	
	Debug.println("All done loading.");
	
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
	
	
	
	 Debug.assertm(totalGood + totalBad == grandTotal);
	
    }

    public EmpiricalROCMaker(Vector<ValidationInformationPrecRec> info, int numberofpoints) {
    	
    	this.numberofpoints = numberofpoints;
    	
    	//int total;
    	
    	// read data from infile.
    	fill_array(info);
    	
    	
    	// paranoid double check 
    	if (Debug.status) {
    	int total = 0;

    	// count number of positive values
    	for (int count=0; count < answer.length; count++) {
    	    if (answer[count] > .5) {
    		total++;
    	    }
    	    
    	}
    	
    	
    	 Debug.assertm(total == totalGood);
    	}
    	// Need to sort both arrays by the predicted value.
    	
    	System.out.println("Sorting values");

    	IntComparator myCompare = (IntComparator) new MyComparator(answer, prediction);
    	Swapper mySwapper = (Swapper) new MySwapper(answer,prediction);

    	cern.colt.GenericSorting.quickSort(0, answer.length ,myCompare, mySwapper);
    	
    	
    	
    	if (Debug.status) {
    	    // more paranoid checks
    	    int total = 0;
    	    for (int count=0; count < answer.length; count++) {
    		if (answer[count] > .5) {
    		    total++;
    		}
    		
    	    }
    	     Debug.assertm(total == totalGood);
    	}
    	
           
    	System.out.println("Computing ROC curve points...");
    	process_list_data();
    	
    	System.out.println("Curve constructed using " + jump + " observations per ROC point.");
    	System.out.println("Area for " + outfile + ": " + area);

    	}
    
    public EmpiricalROCMaker(String infile, String outfile, int numberofpoints) {
	
	this.numberofpoints = numberofpoints;
	this.infile = infile;
	this.outfile = outfile;
	//int total;
	
	// read data from infile.
	fill_array();
	
	
	// paranoid double check 
	if (Debug.status) {
	int total = 0;

	// count number of positive values
	for (int count=0; count < answer.length; count++) {
	    if (answer[count] > .5) {
		total++;
	    }
	    
	}
	
	
	 Debug.assertm(total == totalGood);
	}
	// Need to sort both arrays by the predicted value.
	
	System.out.println("Sorting values");

	IntComparator myCompare = (IntComparator) new MyComparator(answer, prediction);
	Swapper mySwapper = (Swapper) new MySwapper(answer,prediction);

	cern.colt.GenericSorting.quickSort(0, answer.length ,myCompare, mySwapper);
	
	
	
	if (Debug.status) {
	    // more paranoid checks
	    int total = 0;
	    for (int count=0; count < answer.length; count++) {
		if (answer[count] > .5) {
		    total++;
		}
		
	    }
	     Debug.assertm(total == totalGood);
	}
	
       
	System.out.println("Computing ROC curve points...");
	process_list();
	
	System.out.println("Curve constructed using " + jump + " observations per ROC point.");
	System.out.println("Area for " + outfile + ": " + area);

	}
    

    public static void main(String argv[]) {
	int numberpoints = 1;
	boolean error = false;


	if (argv.length < 2) {
	    error = true;
	    System.err.println("Error: Usage java EmpiricalROCMaker infile outfile [number of points]\n\nThe number of points is approximate.  We divide the number of observations by the number of points to get an approximate increment for each point.");

	}

	if (argv.length >2) {
	    try {
	    numberpoints = new Integer(argv[2]).intValue();
	    
	    } catch (NumberFormatException e) {
		System.err.println("Error: bad argument for number of points");
		error = true;
	    }
	}
    
	if (!error) {
	
	EmpiricalROCMaker myObj = new EmpiricalROCMaker(argv[0],argv[1],numberpoints);
	}
       


    }	

    // These classes below allow us to use of the cern colt classes for sorting.  See my PennAspect web page for a link where you can pick these up.

class MySwapper implements cern.colt.Swapper {
	private double[] answer;
	private double[] prediction;
    

    public MySwapper(double[] answer, double[] prediction) {
	    this.answer = answer;
	    this.prediction = prediction;
	}
    
    public void swap(int a, int b) {
	
	double temp;
	temp = answer[a];
	answer[a] = answer[b];
	answer[b] = temp;
	
	temp = prediction[a];
	prediction[a] = prediction[b];
	prediction[b] = temp;

    }

 

}

    class MyComparator implements cern.colt.function.IntComparator {
	
	private double[] answer;
	private double[] prediction;
	

	public MyComparator(double[] answer, double[] prediction) {
	    this.answer = answer;
	    this.prediction = prediction;
	}


	public int compare(int i, int j) {
	    
	    if (prediction[i] < prediction[j]) {
		return 1;
	    }
	    else {if (prediction[i] > prediction[j]) {
		return -1;

	    } else {
		return 0;
	    }}
	}


    }

    
}
