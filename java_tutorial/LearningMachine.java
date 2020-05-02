import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *	Class for building a predictive classifier using the weka library
 */
public class LearningMachine {
	
	private Instances dataX;	// data type to input to weka model
	public Classifier fit;		// weka classifier
	public String[] headers; 	// variable names of data used to create model
	public String[][] allData; 	// original csv file of all data
	
	/**
	 * Default constructor
	 */
	public LearningMachine() {
		dataX = null;
		fit = null;
		headers = null;
	}
	
	/**
	 * Builds a classifier from given data
	 * @param preds			column names
	 * @param d				Data object that all data was from
	 * @param trainData			data that will be used to build the model
	 * @throws Exception
	 */
	public LearningMachine(String[] preds, Data d, String[][] trainData) throws Exception {
		headers = d.headers;
		allData = d.data;
		
		fit = new weka.classifiers.functions.Logistic();
		dataX = createInstX(trainData, preds);
		fit.buildClassifier(dataX);
	}
	
	/**
	 * With an already built model, predicts the probabilities of being in class 1 for 
	 * a given set of datapoints
	 * @param data		data that the model will predict probabilities for
	 * @param preds		column names
	 * @return			double array of probabilities, equal to length of data
	 * @throws Exception
	 */
	public double[] predict(String[][] data, String[] preds) throws Exception {
		Instances instData = createInstX(data, preds);
		double[] predictions = new double[instData.size()];
		for (int i = 0; i < instData.size(); i++) {
			predictions[i] = fit.distributionForInstance(instData.get(i))[0];
		}
		return predictions;
	}
	
	/**
	 * With an already built model, returns the probability of one data point being in class 1
	 * @param data		one data point
	 * @param preds		variable names
	 * @return			probability
	 * @throws Exception
	 */
	public double predict(String[] data, String[] preds) throws Exception {
		int[] predIndexes = new int[preds.length - 1];
		for (int i = 0; i < preds.length - 1; i++) {
			predIndexes[i] = Arrays.asList(headers).indexOf(preds[i]);
		}
		
		String[][] data2D = new String[1][headers.length];
		for (int i = 0; i < headers.length; i++) {
			data2D[0][i] = "";
		}
		for (int i = 0; i < preds.length - 1; i++) {
			data2D[0][predIndexes[i]] = data[i];
		}

		Instance inst = createInstX(data2D, preds).get(0);
		inst.setDataset(dataX);
		return fit.distributionForInstance(inst)[0];
		}
	
	/**
	 * Converts a 2D string array to an Instances object for processing in weka
	 * @param data	Original data array
	 * @param preds	Column names of desired predictor variables
	 * @return		Instances object containing column variables data
	 */
	private Instances createInstX(String[][] data, String[] preds) {
		int[] predIndexes = new int[preds.length];
		for (int i = 0; i < preds.length; i++) {
			predIndexes[i] = Arrays.asList(headers).indexOf(preds[i]);
		}
		
		boolean[] numerical = isNumerical(preds);
		numerical[numerical.length - 1] = false; // classifier, not numerical
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (int i = 0; i < preds.length; i++) {
			if (numerical[i]) {
				attributes.add(new Attribute(preds[i]));								
			}
			else {
				ArrayList<String> values = new ArrayList<String>();
				for (int j = 0; j < data.length; j++) {
					if (!values.contains(data[j][predIndexes[i]])) {
						values.add(data[j][predIndexes[i]]);
					}
				}
				attributes.add(new Attribute(preds[i], values));
			}
		}
		
		Instances dataX = new Instances("", attributes, data.length);
		dataX.setClassIndex(dataX.numAttributes() - 1); // last column in preds
		
		for (int i = 0; i < data.length; i++) {
			Instance inst = new DenseInstance(preds.length);
			for (int j = 0; j < preds.length; j++) {
				if (numerical[j]) {
					inst.setValue(attributes.get(j), Double.parseDouble(data[i][predIndexes[j]]));					
				}
				else {
					inst.setValue(attributes.get(j), data[i][predIndexes[j]]);
				}
			}
			dataX.add(inst);
		}
		return dataX;
	}
	
	/**
	 * Determines which columns are numerical
	 * @param preds	Column names 
	 * @return		Boolean array for which columns are numerical
	 */
	private boolean[] isNumerical(String[] preds) {
		boolean[] isNum = new boolean[preds.length];
		for (int i = 0; i < preds.length; i++) {
			int col = Arrays.asList(headers).indexOf(preds[i]);
			isNum[i] = isNumerical(col, allData);
		}
		
		return isNum;
	}
	
	/**
	 * Determines if a column of data is numerical
	 * @param col	Column number of data
	 * @param data	Array of data, currently stored as Strings
	 * @return		true if data is numerical, false if not
	 */
	private boolean isNumerical(int col, String[][] data) {
		for (int i = 0; i < data.length; i++) {
			try {
				double d = Double.parseDouble(data[i][col]);
			}	catch (NumberFormatException e) {
				return false;
			}
		}
		
		return true;
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
}
