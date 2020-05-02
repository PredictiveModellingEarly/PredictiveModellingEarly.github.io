/**
 *	Extends LearningMachine for Solution file
 * 	Contains functions to calculate FPR, FNR, and CCR
 */
public class LearningMachineSolution extends LearningMachine {
	
	/**
	 * Default constructor
	 */
	public LearningMachineSolution() {
		super();
	}
	
	/**
	 * Builds a classifier from given data
	 * @param preds			column names
	 * @param d				Data object that all data was from
	 * @param trainData			data that will be used to build the model
	 * @throws Exception
	 */
	public LearningMachineSolution(String[] preds, Data d, String[][] trainData) throws Exception {
		super(preds, d, trainData);
	}

	/**
	 * Return false positive rate for given data and threshold
	 * @param xData	Column of data to compare to threshold (could be predictions)
	 * @param yData Actual data, 0s or 1s
	 * @param thr	Double between 0.0 and 1.0
	 * @return		False positive rate
	 */
	public double getFPR(double[] xData, double[] yData, double thr) {
		int falsePositives = 0;
		int totalNegatives = 0;
		for (int i = 0; i < xData.length; i++) {
			if (xData[i] >= thr && yData[i] == 0) {
				falsePositives++;
			}
			if (yData[i] == 0) {
				totalNegatives++;
			}
		}
		return ((double) falsePositives) / totalNegatives;
	}
	
	/**
	 * Return false negative rate for given data and threshold
	 * @param xData	Column of data to compare to threshold (could be predictions)
	 * @param yData Actual data, 0s or 1s
	 * @param thr	Double between 0.0 and 1.0
	 * @return		False negative rate
	 */
	public double getFNR(double[] xData, double[] yData, double thr) {
		int falseNegatives = 0;
		int totalPositives = 0;
		for (int i = 0; i < xData.length; i++) {
			if (xData[i] < thr && yData[i] == 1) {
				falseNegatives++;
			}
			if (yData[i] == 1) {
				totalPositives++;
			}
		}
		return ((double) falseNegatives) / totalPositives;
	}

	/**
	 * Return correct classification rate for given data and threshold
	 * @param xData	Column of data to compare to threshold (could be predictions)
	 * @param yData Actual data, 0s or 1s
	 * @param thr	Double between 0.0 and 1.0
	 * @return		Correct classification rate
	 */
	public double getCCR(double[] xData, double[] yData, double thr) {
		int correctlyClassified = 0;
		for (int i = 0; i < xData.length; i++) {
			if (xData[i] < thr && yData[i] == 0.0) {
				correctlyClassified++;
			}
			if (xData[i] >= thr && yData[i] == 1.0) {
				correctlyClassified++;
			}
		}
		return ((double) correctlyClassified) / xData.length;
	}
}
