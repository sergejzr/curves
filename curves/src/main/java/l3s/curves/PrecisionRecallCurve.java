package l3s.curves;


import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import jnisvmlight.LabeledFeatureVector;
import ungarlab.benchmark.ROCtools.EmpiricalROCMaker;

public class PrecisionRecallCurve {
	private Vector<ValidationInformationPrecRec> sortedValidationVector;
	private int cnttrue = 0;
	
	//private LabeledFeatureVector[] test;
	
	public PrecisionRecallCurve(LabeledFeatureVector[] test, double[] classificationvalues,boolean balance)
	{

		//sortTrain(test,classificationvalues);
		Vector<ValidationInformationPrecRec> validationset = createValidationInfo(test,classificationvalues);
		Collections.shuffle(validationset);
		countRight(validationset);
		if(balance==true){
			validationset=obtainBalancedSet(validationset);
		}
		
		sortedValidationVector=sort(validationset);
	}
	public PrecisionRecallCurve(Vector<ValidationInformationPrecRec> info, boolean balance) {
		countRight(info);
		if(balance)
		{
			info=obtainBalancedSet(info);
		}
		
		sortedValidationVector=sort(info);
	}
	
	
	
	
	
	public Double[] getPrecision(double recall) {
		Vector<ValidationInformationPrecRec> classificationResultsPreRec = sortedValidationVector;//sortTrain();

		int rPos = 0;
		int fPos = 0;
		final int n = (int) ((cnttrue) * recall);
		for (int i = 0; rPos < n; i++) {
			final ValidationInformationPrecRec vi = (ValidationInformationPrecRec) classificationResultsPreRec
					.get(i);
			// System.out.println(vi);
			final String className = vi.getClassLabel();
			// double dist = vi.getDistance();
			if (className.equals("true")) {
				rPos++;
			} else {
				fPos++;
			}
		}
		final double prec = (double) rPos / (double) (rPos + fPos);
		
		return new Double[]{prec,1.*rPos,1.*fPos};
	}

	
	private void countRight(Vector<ValidationInformationPrecRec> in)
	{
		int cntright=0;
		for(ValidationInformationPrecRec record:in)
		{
			if(record.getClassLabel().equals("true"))
			{
				cntright++;
			}
		}
		
		cnttrue=cntright;
	}
	private Vector<ValidationInformationPrecRec> obtainBalancedSet(Vector<ValidationInformationPrecRec> in)
	{
		Vector<ValidationInformationPrecRec> ret=new Vector<ValidationInformationPrecRec>();
		
		int cntright=0,cntwrong=0;
		/*
		for(ValidationInformationPrecRec record:in)
		{
			if(record.getClassLabel().equals("true"))
			{
				cntright++;
			}else
			{
				cntwrong++;
			}
		}
		int max=Math.min(cntright,cntwrong);
		cnttrue=cntright;*/
		
		int max=cnttrue;
		cntright=0;cntwrong=0;
		
		for(ValidationInformationPrecRec record:in)
		{
			if(cntright<max&&record.getClassLabel().equals("true")){ret.add(record);cntright++;}
			else if(cntwrong<max&&record.getClassLabel().equals("false")){{ret.add(record);cntwrong++;}}
			
		}
		return ret;
	}
	private Vector<ValidationInformationPrecRec> createValidationInfo(LabeledFeatureVector[] test, double[] classificationvalues)
	{
		Vector<ValidationInformationPrecRec> ret=new Vector<ValidationInformationPrecRec>();
		for (int i = 0; i < test.length; i++)
		{
			double value = classificationvalues[i];//classify(test[i]);
			String lable = "false";
			if (test[i].getLabel() > 0) {	
				lable = "true";
			}
			ret.add(new ValidationInformationPrecRec(test[i].getId(),lable, value));
		}
		return ret;
	}
	
	private Vector<ValidationInformationPrecRec> sort(Vector<ValidationInformationPrecRec> in) {
		Collections.shuffle(in);
		Collections.sort(in);
		return in;
	}
	public double getBEP() {
		return getPrecRecBEP(sortedValidationVector);
		
	}
	public double getNegBEP() {
		return getNegPrecRecBEP(sortedValidationVector);
		
	}
	public double getPrecRecBEP(
			Vector<ValidationInformationPrecRec> classificationResultsPreRec) {
		// System.out.println(classificationResultsPreRec);
		int rPos = 0;
		int fPos = 0;
		final int n = cnttrue;
		for (int i = 0; rPos < n; i++) {
			final ValidationInformationPrecRec vi = (ValidationInformationPrecRec) classificationResultsPreRec
					.get(i);
			// System.out.println(vi);
			final String className = vi.getClassLabel();
			// double dist = vi.getDistance();
			if (className.equals("true")) {
				rPos++;
			} else {
				fPos++;
			}
			final double prec = (double) rPos / (double) (rPos + fPos);
			final double rec = (double) rPos / (double) n;
			// System.out.println(prec + " " + rec);
			if (Math.abs(prec - rec) < 0.001 && rPos != 0) {
				return prec;
			}
		}
		// double prec = (double)rPos/(double)(rPos+fPos);
		return 0;

	}
	
	public double getNegPrecRecBEP(
			Vector<ValidationInformationPrecRec> classificationResultsPreRec) {
		// System.out.println(classificationResultsPreRec);
		
		Vector<ValidationInformationPrecRec> modified = new Vector<ValidationInformationPrecRec>();
		modified.addAll(classificationResultsPreRec);
				;
		Collections.reverse(modified);
		int rPos = 0;
		int fPos = 0;
		final int n = classificationResultsPreRec.size()-cnttrue;
		for (int i = 0; rPos < n; i++) {
			final ValidationInformationPrecRec vi = (ValidationInformationPrecRec) modified
					.get(i);
			// System.out.println(vi);
			final String className = vi.getClassLabel();
			// double dist = vi.getDistance();
			if (className.equals("false")) {
				rPos++;
			} else {
				fPos++;
			}
			final double prec = (double) rPos / (double) (rPos + fPos);
			final double rec = (double) rPos / (double) n;
			// System.out.println(prec + " " + rec);
			if (Math.abs(prec - rec) < 0.001 && rPos != 0) {
				return prec;
			}
		}
		// double prec = (double)rPos/(double)(rPos+fPos);
		return 0;

	}
	
	public void print() {print(false,false);}
	public void print(boolean graphically, boolean showlevels) {

		
		
		
	if(!graphically){
		System.out.print("recall\tprecision\t#Positives\t#Negatives\tMIN(distance)\tMAX(distance)\tAVG(distance)\n");
			for(int i=0;i<100;i++)
			{
				Double[] precision = getPrecision((i/100.));
				System.out.print((i/100.)+"\t"+precision[0]);
				if(showlevels)
				{	
					System.out.print("\t"+precision[1]+"\t"+precision[2]);
					Double[] scores=getAveragescore((i/100.));
					System.out.print("\t"+scores[0]+"\t"+scores[1]+"\t"+scores[2]);
				}
				System.out.println();
			}
		
			
		
		System.out.println(getBEP());
	}else
	{
		
	}

		
	}
	
	
	public void printNegative(boolean graphically, boolean showlevels) {

		
		
		
		if(!graphically){
			System.out.print("recall\tprecision\t#Positives\t#Negatives\tMIN(distance)\tMAX(distance)\tAVG(distance)\n");
				for(int i=0;i<100;i++)
				{
					Double[] precision = getBackPrecision((i/100.));
					System.out.print((i/100.)+"\t"+precision[0]);
					if(showlevels)
					{	
						System.out.print("\t"+precision[1]+"\t"+precision[2]);
						Double[] scores=getAveragescore((i/100.));
						System.out.print("\t"+scores[0]+"\t"+scores[1]+"\t"+scores[2]);
					}
					System.out.println();
				}
			
				
			
			System.out.println(getBEP());
		}else
		{
			
		}

			
		}
	
	private Double[] getBackPrecision(double recall) {
		

		Vector<ValidationInformationPrecRec> classificationResultsPreRec = new Vector<ValidationInformationPrecRec>();
				
		classificationResultsPreRec.addAll(sortedValidationVector);	
				
Collections.reverse(classificationResultsPreRec);
		int rPos = 0;
		int fPos = 0;
		final int n = (int) ((sortedValidationVector.size()-cnttrue) * recall);
		for (int i = 0; rPos < n; i++) {
			final ValidationInformationPrecRec vi = (ValidationInformationPrecRec) classificationResultsPreRec
					.get(i);
			// System.out.println(vi);
			final String className = vi.getClassLabel();
			// double dist = vi.getDistance();
			if (className.equals("false")) {
				rPos++;
			} else {
				fPos++;
			}
		}
		final double prec = (double) rPos / (double) (rPos + fPos);
		
		return new Double[]{prec,1.*rPos,1.*fPos};
	}
	private Double[] getAveragescore(double recall) {
		Vector<ValidationInformationPrecRec> classificationResultsPreRec = sortedValidationVector;//sortTrain();

		
		final int n = (int) ((cnttrue) * recall);
		double sum=0;
		int cnt=0;
		double max=Double.NEGATIVE_INFINITY, min=Double.POSITIVE_INFINITY;
		for (int i = Math.max(0, n-5); i < n; i++) {
			
			final ValidationInformationPrecRec vi = (ValidationInformationPrecRec) classificationResultsPreRec
					.get(i);
			double distance=vi.getDistance();
			if(distance>max) max=distance;
			if(distance<min) min=distance;
			sum+=distance;
			cnt++;
		}
		
		return new Double[]{sum/cnt,max,min};
	}
	public void printAsROC() {
		new EmpiricalROCMaker(sortedValidationVector, 100);
		
	}
	public static void main(String[] args) {
		//double values[]=new double[]{};
		Hashtable<Double, Double> data=new Hashtable<Double, Double>();
		data.put(	0.	,	1.	);
		data.put(	0.01	,	0.933333333	);
		data.put(	0.02	,	0.965517241	);
		data.put(	0.03	,	0.976744186	);
		data.put(	0.04	,	0.982758621	);
		data.put(	0.05	,	0.986111111	);
		data.put(	0.06	,	0.988372093	);
		data.put(	0.07	,	0.99	);
		data.put(	0.08	,	0.982758621	);
		data.put(	0.09	,	0.984615385	);
		data.put(	0.1	,	0.986111111	);
		data.put(	0.11	,	0.987341772	);
		data.put(	0.12	,	0.988439306	);
		data.put(	0.13	,	0.989304813	);
		data.put(	0.14	,	0.975490196	);
		data.put(	0.15	,	0.97260274	);
		data.put(	0.16	,	0.970212766	);
		data.put(	0.17	,	0.968	);
		data.put(	0.18	,	0.966037736	);
		data.put(	0.19	,	0.964285714	);
		data.put(	0.2	,	0.962837838	);
		data.put(	0.21	,	0.958333333	);
		data.put(	0.22	,	0.960122699	);
		data.put(	0.23	,	0.958944282	);
		data.put(	0.24	,	0.957983193	);
		data.put(	0.25	,	0.959568733	);
		data.put(	0.26	,	0.958549223	);
		data.put(	0.27	,	0.96	);
		data.put(	0.28	,	0.961445783	);
		data.put(	0.29	,	0.962703963	);
		data.put(	0.3	,	0.959550562	);
		data.put(	0.31	,	0.958695652	);
		data.put(	0.32	,	0.957983193	);
		data.put(	0.33	,	0.957230143	);
		data.put(	0.34	,	0.958415842	);
		data.put(	0.35	,	0.952198853	);
		data.put(	0.36	,	0.95	);
		data.put(	0.37	,	0.947841727	);
		data.put(	0.38	,	0.947460595	);
		data.put(	0.39	,	0.947098976	);
		data.put(	0.4	,	0.94214876	);
		data.put(	0.41	,	0.941935484	);
		data.put(	0.42	,	0.943217666	);
		data.put(	0.43	,	0.941538462	);
		data.put(	0.44	,	0.938622754	);
		data.put(	0.45	,	0.939882698	);
		data.put(	0.46	,	0.937052933	);
		data.put(	0.47	,	0.934357542	);
		data.put(	0.48	,	0.930612245	);
		data.put(	0.49	,	0.920844327	);
		data.put(	0.5	,	0.919896641	);
		data.put(	0.51	,	0.916666667	);
		data.put(	0.52	,	0.88742515	);
		data.put(	0.53	,	0.830583058	);
		data.put(	0.54	,	0.81032666	);
		data.put(	0.55	,	0.813084112	);
		data.put(	0.56	,	0.81595092	);
		data.put(	0.57	,	0.818548387	);
		data.put(	0.58	,	0.819444444	);
		data.put(	0.59	,	0.82111437	);
		data.put(	0.6	,	0.822906641	);
		data.put(	0.61	,	0.825261159	);
		data.put(	0.62	,	0.826779026	);
		data.put(	0.63	,	0.826728111	);
		data.put(	0.64	,	0.829090909	);
		data.put(	0.65	,	0.828264758	);
		data.put(	0.66	,	0.828193833	);
		data.put(	0.67	,	0.828125	);
		data.put(	0.68	,	0.829623288	);
		data.put(	0.69	,	0.830236486	);
		data.put(	0.7	,	0.831526272	);
		data.put(	0.71	,	0.831414474	);
		data.put(	0.72	,	0.833468725	);
		data.put(	0.73	,	0.834001604	);
		data.put(	0.74	,	0.834520982	);
		data.put(	0.75	,	0.835680751	);
		data.put(	0.76	,	0.835648148	);
		data.put(	0.77	,	0.836128049	);
		data.put(	0.78	,	0.834710744	);
		data.put(	0.79	,	0.833951075	);
		data.put(	0.8	,	0.833333333	);
		data.put(	0.81	,	0.831412104	);
		data.put(	0.82	,	0.827781715	);
		data.put(	0.83	,	0.827151854	);
		data.put(	0.84	,	0.81874145	);
		data.put(	0.85	,	0.815488215	);
		data.put(	0.86	,	0.810721377	);
		data.put(	0.87	,	0.805068226	);
		data.put(	0.88	,	0.800255265	);
		data.put(	0.89	,	0.799495586	);
		data.put(	0.9	,	0.796273292	);
		data.put(	0.91	,	0.796558082	);
		data.put(	0.92	,	0.792145015	);
		data.put(	0.93	,	0.786817102	);
		data.put(	0.94	,	0.784417106	);
		data.put(	0.95	,	0.778481013	);
		data.put(	0.96	,	0.764673002	);
		data.put(	0.97	,	0.758923668	);
		data.put(	0.98	,	0.748525469	);
		data.put(	0.99	,	0.702891326	);
		
		int matrix[][]=new int[55][12];
		
		Double prev=null;
		for(int i=0;i<50;i++)
		{
			double x=i/50.;
			double y=data.get(x);
			int idx=(int) (i);
			int scale=(int)(y*10);
			//char character;
			if(y<scale+.5)
			{
				if(prev==null||prev-y<0.4){
			//	 character = '-';
				 matrix[idx][scale]=1;
				}else{//character = '\\';
				 matrix[idx][scale]=3;}
			}else
			{
				//character='_';
				matrix[idx][scale+1]=2;
			}
			prev=y;
			//System.out.println((i/10.)+"\t"+getPrecision((i/10.)));
		}
		/*
for(int y=11;y>0;y--) {
			
			for(int x=0;x<11;x++) 
			{
				

					System.out.print(matrix[x][y]);
			
			}
			System.out.println("\n");
		}*/
		
		
		for(int y=11;y>=0;y--) {
			if(y<=10)
			System.out.print(y/10.+"  ");
			else
				System.out.print("     ");	
			for(int x=0;x<50;x++) 
			{
				switch(matrix[x][y])
				{

				case 1:
					System.out.print("-");
					break;
				case 2:
					System.out.print("_");
					break;
				case 3:
					System.out.print("\\");
					break;
				default:
					System.out.print(" ");
					break;
				}
			}
			System.out.println();
		}
		System.out.print("   ");	
		for(int x=0;x<11;x++) 
		{
			System.out.print(" "+x/10.+" ");	
		}
		

	}
	public int size(){return sortedValidationVector.size();}
	public void printWholeset(String filename)
	{
		printWholeset( filename, 0, size());
	}
	public void printWholeset(String filename,Integer limit)
	{
		printWholeset( filename, 0, limit);
	}
	public void printWholeset(String filename, Integer start, Integer limit) {
		FileWriter fw=null;
		if(filename!=null){
		 try {
			fw=new FileWriter(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		System.out.println("Nr\tprocent\tid\tdistance\tclass");
		
		int totakeevery = (int)(sortedValidationVector.size()/(limit*1.));
		Random r=new Random();
		for(int i=start;i<sortedValidationVector.size();i++)
		{
			if(limit!=null&&r.nextInt(totakeevery)!=5) continue;
			double curprocent = (i*1./sortedValidationVector.size());
			curprocent=((int)(Math.round(curprocent*100)))/100.;
			String line=i+"\t"+curprocent+"\t"+sortedValidationVector.get(i).getId()+"\t"+sortedValidationVector.get(i).getDistance()+"\t"+sortedValidationVector.get(i).getClassLabel();
			if(fw!=null)
			{
				try {
					fw.write(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(line);
		}
		
	}

	public String asGnuplotData()
	{
	return asGnuplotData(null);
	}
	public String asGnuplotData(Double step)
	{
		StringBuilder sb=new StringBuilder();

		sb.append("#recall\tprecision\n");
		
		if(step==null) step=10.;
		
	
			for(double i=step;i<100;i+=step)
			{
				Double[] precision = getPrecision((i/100.));
				sb.append((i/100.)+"\t"+precision[0]+"\n");
			}
			double BEP = getBEP();
		sb.append("e\n"+BEP+"\t"+BEP+"\ne\n");
	return sb.toString();
	}
	
	
	public PrecisionValueCurve getPrecisionValueCurve() {
		return null;
		//TODO: SZ:
	}
}
