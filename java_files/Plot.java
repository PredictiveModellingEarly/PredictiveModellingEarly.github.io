import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** 
 *	Class for plotting histograms and line graphs, using the JFreeChart Java library
 */
public class Plot {
	// colors for histogram plotting, alpha set to 127
	public static final Color[] colors = {new Color(0, 0, 255, 127), new Color(255, 0, 0, 127), new Color(0, 255, 0, 127)};
	
	/**
	 * Plot a frequency histogram of one dataset
	 * @param data			Double array to be plotted
	 * @param seriesName	Name of histogram
	 * @param fileName		The file name that the histogram will be saved to
	 * @param title			The title of the graph
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @throws IOException
	 */
	public static void plotHistogram(double[] data, String seriesName, 
			String fileName, String title, String xAxisLabel, String yAxisLabel) throws IOException {
		HistogramDataset hist = new HistogramDataset();
		hist.setType(HistogramType.RELATIVE_FREQUENCY);
		hist.addSeries(seriesName, data, 10);
		
		JFreeChart chart = ChartFactory.createHistogram(title, xAxisLabel, yAxisLabel, hist, PlotOrientation.VERTICAL
				, true, false, false);
		for (int i = 0; i < data.length; i++) {
			chart.getXYPlot().getRenderer().setSeriesPaint(i, colors[i % colors.length]);
		}

		ChartUtilities.saveChartAsPNG(new File(fileName), chart, 1000, 600);
		System.out.println("saved histogram to " + fileName + ".");	
	}
	
	/**
	 * Plot a frequency histogram of multiple series (maximum recommended is 3)
	 * @param data			List of double arrays
	 * @param seriesNames	Name of each histogram, same length as data
	 * @param fileName		The file name that the histogram will be saved to
	 * @param title			The title of the graph
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @throws IOException
	 */
	public static void plotHistogram(double[][] data, String[] seriesNames, 
			String fileName, String title, String xAxisLabel, String yAxisLabel) throws IOException {
		HistogramDataset hist = new HistogramDataset();
		hist.setType(HistogramType.RELATIVE_FREQUENCY);
		for (int i = 0; i < data.length; i++) {
			hist.addSeries(seriesNames[i], data[i], 10);
		}
		
		JFreeChart chart = ChartFactory.createHistogram(title, xAxisLabel, yAxisLabel, hist, PlotOrientation.VERTICAL
				, true, false, false);
		for (int i = 0; i < data.length; i++) {
			chart.getXYPlot().getRenderer().setSeriesPaint(i, colors[i % colors.length]);
		}

		ChartUtilities.saveChartAsPNG(new File(fileName), chart, 1000, 600);
		System.out.println("saved histogram to " + fileName + ".");	
	}
	/**
	 * Plot a series of line charts on the same x-axis
	 * @param xData			All y-data is plotted vs same x-data
	 * @param yData			Each row is a different series
	 * @param seriesNames	Name of each line graph, same length as number of series
	 * @param fileName		The file name that the PNG will be saved to
	 * @param title			The title of the graph
	 * @param xAxisLabel	
	 * @param yAxisLabel
	 * @throws IOException
	 */
	public static void plotRates(double[] xData, double[][] yData, String[] seriesNames, String fileName, 
			String title, String xAxisLabel, String yAxisLabel) throws IOException {
		XYSeriesCollection data = new XYSeriesCollection();
		for (int index = 0; index < yData.length; index++) {
			XYSeries ser = new XYSeries(seriesNames[index]);
			for (int i = 0; i < yData[0].length; i++) {
				ser.add(xData[i], yData[index][i]);
			}
			data.addSeries(ser);
		}
		JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, data);
		for (int i = 0; i < yData.length; i++) {
			chart.getXYPlot().getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));			
		}
		ChartUtilities.saveChartAsPNG(new File(fileName), chart, 1000, 600);
		System.out.println("saved line chart to " + fileName + ".");
	}
}