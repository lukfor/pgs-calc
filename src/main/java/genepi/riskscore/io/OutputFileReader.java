package genepi.riskscore.io;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class OutputFileReader {

	public static final String COLUMN_SAMPLE = "sample";

	public static final char SEPARATOR = ',';

	private double[] data;

	private List<String> scores;

	private ITableReader reader;

	public OutputFileReader(String filename) throws IOException {
		init(filename);
	}

	public void init(String filename) throws IOException {

		reader = new CsvTableReader(filename, SEPARATOR);

		scores = new Vector<String>();
		String[] columns = reader.getColumns();
		for (String column : columns) {
			if (!column.equals(COLUMN_SAMPLE)) {
				scores.add(column);
			}
		}

		data = new double[scores.size()];

	}

	public boolean next() {
		return reader.next();
	}

	public double[] getValues() {
		for (int i = 0; i < scores.size(); i++) {
			data[i] = reader.getDouble(scores.get(i));
		}
		return data;
	}

	public String getSample() {
		return reader.getString(COLUMN_SAMPLE);
	}

	public void close() {

	}

	public List<String> getScores() {
		return scores;
	}

	@Override
	public String toString() {
		return "scores: " + scores.size();
	}

}
