package test;

import java.util.Random;
import java.util.Vector;

import l3s.curves.PrecisionRecallCurve;
import l3s.curves.ValidationInformationPrecRec;

public class ROCtest {
	public static void main(String[] args) {

		Vector<ValidationInformationPrecRec> info = new Vector<ValidationInformationPrecRec>();

		int procentfalsepositive=30;
		for (int i = 0; i < 100-procentfalsepositive; i++) {
			Random r = new Random();
				info.add(new ValidationInformationPrecRec("true", r
						.nextDouble()));
				info.add(new ValidationInformationPrecRec("false", 0 - r
						.nextDouble()));
		}

		for(int i=0;i<procentfalsepositive;i++)
		{ Random r = new Random();
			info.add(new ValidationInformationPrecRec("true", 0 - r
					.nextDouble()));
			info.add(new ValidationInformationPrecRec("false", r.nextDouble()));
		}
		
		PrecisionRecallCurve prc = new PrecisionRecallCurve(info, true);

		System.out.println("\n\nP/R curve");
		prc.print();
		
		System.out.println("\n\nROC curve");
		prc.printAsROC();
		
		System.out.println("\n\nP/R in Gnuplotformat");
		System.out.println(prc.asGnuplotData());
	}
}
