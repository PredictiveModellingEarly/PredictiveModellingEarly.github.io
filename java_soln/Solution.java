import java.util.Arrays;

/*
 * Data taken from https://github.com/propublica/compas-analysis/raw/master/compas-scores-two-years.csv
 */

/**
 * Solution guide to Predictive Modeling assignment
 */
public class Solution {
	public static void main(String[] args) throws Exception {
/*
 * Part 0: Reading in data
 */
		String url = "https://github.com/propublica/compas-analysis/raw/master/compas-scores-two-years.csv";
		Data d = new Data(url);

/*
 * Part 1: Comparing the Scores of Black and White Defendants
 * We first explore whether white and black defendants get the same COMPAS scores.
 */ 
		
		int raceIndex = Arrays.asList(d.headers).indexOf("race");
		// data for black defendants
		String[][] trainB = d.getRows(d.train, raceIndex, "African-American");
		String[][] validB = d.getRows(d.valid, raceIndex, "African-American");
		// data for white defendants
		String[][] trainW = d.getRows(d.train, raceIndex, "Caucasian");
		String[][] validW = d.getRows(d.valid, raceIndex, "Caucasian");
		
		int decileScoreIndex = Arrays.asList(d.headers).indexOf("decile_score");
		// list of decile scores for black defendants in training data
		double[] decileScoresB = d.convertToDouble(trainB, decileScoreIndex);
		// list of decile scores for white defendants in training data
		double[] decileScoresW = d.convertToDouble(trainW, decileScoreIndex);

		Plot.plotHistogram(new double[][] {decileScoresB, decileScoresW},  new String[] {"Black", "White"}, "ScoreHist.png", 
				"Score Distributions of Black and White Defendants", "Decile Score", "Frequency");
		/*
		 * As can be seen in ScoreHist.png:
		 * For African-American defendants, the distribution of the scores is approximately uniform. 
		 * For Caucasian defendants, many more get low scores than high scores.
		 */
	
/*
 *  Part 2: Initial Evaluation of the COMPAS Scores
 *  Here, we are computing the FP, FN, and correct classification rate for different populations.
 *   First, we'll use functions written in LearningMachine to compute the quantities needed.
 */
		LearningMachineSolution lm = new LearningMachineSolution(); // no classifier yet
		double thr = 5;
		
		double[] decileValidB = d.convertToDouble(validB, decileScoreIndex);
		double[] decileValidW = d.convertToDouble(validW, decileScoreIndex);
		double[] decileValidAll = d.convertToDouble(d.valid, decileScoreIndex);

		int recidIndex = Arrays.asList(d.headers).indexOf("is_recid");
		double[] compasValidBY = d.convertToDouble(validB, recidIndex);
		double[] compasValidWY = d.convertToDouble(validW, recidIndex);
		double[] compasValidY = d.convertToDouble(d.valid, recidIndex);
		
		double[] fps = {lm.getFPR(decileValidB, compasValidBY, thr), lm.getFPR(decileValidW, compasValidWY, thr)
				, lm.getFPR(decileValidAll, compasValidY, thr)};
		// FPR for black, white, and all defendants: [0.46808510638297873, 0.22772277227722773, 0.3208955223880597]

		double[] fns = {lm.getFNR(decileValidB, compasValidBY, thr), lm.getFNR(decileValidW, compasValidWY, thr)
				, lm.getFNR(decileValidAll, compasValidY, thr)};
		// FNR for black, white, and all defendants: [0.2744479495268139, 0.43478260869565216, 0.358974358974359]
		
		double[] ccr = {lm.getCCR(decileValidB, compasValidBY, thr), lm.getCCR(decileValidW, compasValidWY, thr)
				, lm.getCCR(decileValidAll, compasValidY, thr)};
		// CCR for black, white, and all defendants: [0.6431159420289855, 0.6804407713498623, 0.6598890942698706]
	
		/*
		 * We can see that the scores do not satisfy false positive parity and do not satisfy false negative parity. 
		 * The scores do satisfy classification parity. Demographic parity is also not satisfied.
		 */
		
/*
 *  Part 3: Altering the Threshold
 *  We will now see how changing the threshold influences 
 *  the false positive, false negative, and correct classification rates.
 */
		
		// List of thresholds
		double[] thrs = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		
		double[][] ratesB = new double[3][thrs.length];
		double[][] ratesW = new double[3][thrs.length];
		double[][] ratesAll = new double[3][thrs.length];
		
		// Make lists of rates for white defendants, black defendants, and all defendants
		for (int i = 0; i < thrs.length; i++) {
			ratesB[0][i] = lm.getFPR(decileValidB, compasValidBY, thrs[i]);
			ratesB[1][i] = lm.getFNR(decileValidB, compasValidBY, thrs[i]);
			ratesB[2][i] = lm.getCCR(decileValidB, compasValidBY, thrs[i]);
			
			ratesW[0][i] = lm.getFPR(decileValidW, compasValidWY, thrs[i]);
			ratesW[1][i] = lm.getFNR(decileValidW, compasValidWY, thrs[i]);
			ratesW[2][i] = lm.getCCR(decileValidW, compasValidWY, thrs[i]);
			
			ratesAll[0][i] = lm.getFPR(decileValidAll, compasValidY, thrs[i]);
			ratesAll[1][i] = lm.getFNR(decileValidAll, compasValidY, thrs[i]);
			ratesAll[2][i] = lm.getCCR(decileValidAll, compasValidY, thrs[i]);
		}
		
		// We can now get three figures for the different demographics,
		String[] rateNames = {"FPR", "FNR", "CCR"};	
		Plot.plotRates(thrs, ratesB, rateNames, "black_rates.png", "Black Defendants", "Threshold", "Rate");
		Plot.plotRates(thrs, ratesW, rateNames, "white_rates.png", "White Defendants", "Threshold", "Rate");
		Plot.plotRates(thrs, ratesAll, rateNames, "all_rates.png", "All Defendants", "Threshold", "Rate");
		
/*
 *  Part 4: Trying to reproduce the score
 */
		
		/*
		 * Let's fit the model:
		 * We first include two possible predictors: age and priors_count
		 */
		String[] preds = {"age", "priors_count", "is_recid"};
		LearningMachine model = new LearningMachine(preds, d, d.train); 
		/*
		 * Our model's coefficients...
			                  Class
			Variable              1
			=======================
			age             -0.0471
			priors_count     0.1698
			Intercept        0.9811
		 * Odds Ratios...
			                  Class
			Variable              1
			=======================
			age               0.954
			priors_count      1.185
		 */

		/*
		 * An increase of 1 in the number of priors is associated with
		 * an increase of 0.17 in the log-odds of recidivism, all other things being equal
		 * An increase in age by one year corresponds to a decrease of 0.047 in the log-odds of recidivism
		 * (If we're being a bit silly and decide to extrapolate) - according to the model,
		 * a newborn with no priors would have a probability of sigma(0.98) = 0.727  of being re-arrested.
		 */
		
		// Let's now obtain the FPR, FNR, and CCR for our model, using the threshold 0.5.
		thr = 0.5;
		
		double[] modelPredsB = model.predict(validB, preds);
		double[] modelPredsW = model.predict(validW, preds);
		double[] modelPredsAll = model.predict(d.valid, preds);
		
		double[] fprFit = {lm.getFPR(modelPredsB, compasValidBY, thr), lm.getFPR(modelPredsW, compasValidWY, thr),
				lm.getFPR(modelPredsAll, compasValidY, thr)};
		// FPR for black, white, and all defendants: [0.32340425531914896, 0.20297029702970298, 0.24253731343283583]
		
		double[] fnrFit = {lm.getFNR(modelPredsB, compasValidBY, thr), lm.getFNR(modelPredsW, compasValidWY, thr),
				lm.getFNR(modelPredsAll, compasValidY, thr)};
		// FNR for black, white, and all defendants: [0.3438485804416404, 0.5217391304347826, 0.4175824175824176]

		double[] ccrFit = {lm.getCCR(modelPredsB, compasValidBY, thr), lm.getCCR(modelPredsW, compasValidWY, thr),
				lm.getCCR(modelPredsAll, compasValidY, thr)};
		// CCR for black, white, and all defendants: [0.6648550724637681, 0.6556473829201102, 0.6691312384473198]

		// It appears that there is basically no overfitting.
		
		// Let's create two hypothetical people and predict the probability of re-arrest for both.
		// person 1: age 30, priors_count 2
		String[] p1 = {"30", "2"};
		// Probability rearrest for person 1: 0.47707520044362595

		// person 2: age 30, priors_count 3
		String[] p2 = {"30", "3"};
		// Probability rearrest for person 1: 0.5194875764966644
		
/*
 * Part 5: Adjusting Thresholds
 * We basically want to find the thresholds for which the false positive rates are at parity. 
 * Let's see what the rates are for different thresholds.
 */
		
		double[] thresholds = {0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
		double[] FPB = new double[thresholds.length];
		double[] FPW = new double[thresholds.length];
		
		for (int i = 0; i < thresholds.length; i++) {
			FPB[i] = lm.getFPR(modelPredsB, compasValidBY, thresholds[i]);
			FPW[i] = lm.getFPR(modelPredsW, compasValidWY, thresholds[i]);
		}
		// FPR for black defendants for each threshold: 
		// (0.3, 0.8723404255319149), (0.4, 0.7021276595744681), (0.5, 0.32340425531914896), 
		// (0.6, 0.17872340425531916), (0.7, 0.10638297872340426), (0.8, 0.02553191489361702)

		// FPR for white defendants for each threshold: 
		// (0.3, 0.6237623762376238), (0.4, 0.4158415841584158), (0.5, 0.20297029702970298),
		// (0.6, 0.0594059405940594), (0.7, 0.034653465346534656), (0.8, 0.01485148514851485)

		// We need to tweak the threshold for black defendants just a little:
		double[] thresholds2 = {0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59};
		double[] newFPB = new double[thresholds2.length];
		for (int i = 0; i < thresholds2.length; i++) {
			newFPB[i] = lm.getFPR(modelPredsB, compasValidBY, thresholds2[i]);
		}
		// FPR for black defendants for tweaked thresholds: 
		// (0.51, 0.2978723404255319), (0.52, 0.26382978723404255), (0.53, 0.2553191489361702), 
		// (0.54, 0.251063829787234), (0.55, 0.225531914893617), (0.56, 0.22127659574468084), 
		// (0.57, 0.20851063829787234), (0.58, 0.2), (0.59, 0.18723404255319148), 
		
		
		// 'thr = 0.575' seems about right
		// Now the white and black demographic would be at parity. 
		// We'll compute the correct classification rate on the validation set.
		// System.out.println("CCR with 0.575: " + lm.getCCR(modelPredsAll, compasValidY, 0.575));
		// CCR with 0.575: 0.6303142329020333

		
		// (Note that we ignored everyone who wasn't white or black. 
		// That's OK to do, but including other demographics (in any way you like) is OK too).
	}
}
