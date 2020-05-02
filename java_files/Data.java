import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import com.opencsv.CSVReader;

/**
 * Class for reading and storing data, as well as train/test/validation splitting
 */
public class Data {
	
	public String[] headers; 	// Variable names - first row of csv file
	public String[][] data;		// All data
	public String[][] train;	
	public String[][] test;
	public String[][] valid;
	
	/**
	 * Read in data
	 * @param file			The link to a csv file, or the file location
	 * @throws IOException
	 */
	public Data(String file) throws IOException {
		URL url;
		CSVReader read;
		CSVReader counter;
		try {
			url = new URL(file);
			read = new CSVReader(new BufferedReader(new InputStreamReader(url.openStream())));
			counter = new CSVReader(new BufferedReader(new InputStreamReader(url.openStream())));
		} catch (MalformedURLException e) {
			read = new CSVReader(new FileReader(file));
			counter = new CSVReader(new FileReader(file));
		}
		headers = read.readNext();
		int numObs = countLines(counter);
		data = new String[numObs][headers.length];
		
		String[] line;
		int index = 0;
		while ((line = read.readNext()) != null) {
			data[index] = line;
			index++;
		}
		
		splitTrainTest();
		read.close();
	}
	
	/**
	 * Creates training, testing, and validation datasets from entire dataset
	 * Uses a 70:15:15 split ratio
	 */
	public void splitTrainTest() {
		ArrayList<Integer> idx = new ArrayList<Integer>();
		for (int i = 0; i < data.length; i++) {
			idx.add(i);
		}
		Collections.shuffle(idx, new Random(0));
		
		int trainSize = (int)(0.7 * data.length);
		int testSize = (int)(0.15 * data.length);
		int validSize = (int)(0.15 * data.length);

		train = new String[trainSize][headers.length];
		test = new String[testSize][headers.length];
		valid = new String[validSize][headers.length];
		
		for (int i = 0; i < trainSize; i++) {
			train[i] = data[idx.get(i)];
		}
		for (int i = 0; i < testSize; i++) {
			test[i] = data[idx.get(trainSize + i)];
		}
		for (int i = 0; i < validSize; i++) {
			valid[i] = data[idx.get(trainSize + testSize + i)];
		}
	}
	
	/**
	 * Count the number of lines in a csv
	 * @param 	Teader a CSVReader with a loaded file
	 * @return 	The number of lines, assuming a header row
	 */
	private int countLines(CSVReader reader) throws IOException {
		Iterator<String[]> it = reader.iterator();
		int count = -1; // ignore header
		while (it.hasNext()) {
			count++;
			it.next();
		}
		return count;
	}
	
	/**
	 * Slice data horizontally, return only certain rows
	 * @param data		Original data (all)
	 * @param col 		The column index to filter from
	 * @param filter	The text that must match
	 * @return 			2D subset of rows of data that match the filter
	 */
	public String[][] getRows(String[][] data, int col, String filter) {
		int count = 0;
		// count number of matches
		for (int i = 0; i < data.length; i++) {
			if (data[i][col].equals(filter)) {
				count++;
			}
		}
		String[][] slice = new String[count][data[0].length];
		int index = 0;
		
		for (int i = 0; i < data.length; i++) {
			if (data[i][col].equals(filter)) {
				slice[index] = data[i];
				index++;
			}
		}
		return slice;
	}
	
	/**
	 * Slice data vertically, return only certain columns
	 * @param data	Original data (all)
	 * @param cols	The column indices to slice out
	 * @return 		2D subset of only certain columns of data
	 */
	public String[][] getCols(String[][] data, int[] cols) {
		String[][] slice = new String[data.length][cols.length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < cols.length; j++) {
				slice[i][j] = data[i][cols[j]];
			}
		}
		return slice;
	}
	
	public double[] convertToDouble(String[][] data, int col) {
		double[] conv = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			conv[i] = Double.parseDouble(data[i][col]);
		}
		return conv;
	}
	
}
