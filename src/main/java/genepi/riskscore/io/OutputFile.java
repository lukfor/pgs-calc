package genepi.riskscore.io;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreSummary;

public class OutputFile {

	public static final String COLUMN_SAMPLE = "sample";

	public static final String COLUMN_SCORE = "score";

	public static final char SEPARATOR = ',';

	private List<String> samples;

	private List<Float>[] data;

	private List<String> scores;

	public OutputFile() {

	}

	public OutputFile(RiskScore[] finalScores, RiskScoreSummary[] summaries) {

		scores = new Vector<String>();
		for (RiskScoreSummary summary : summaries) {
			scores.add(summary.getName());
		}

		samples = new Vector<String>();
		data = new Vector[scores.size()];
		for (int i = 0; i < scores.size(); i++) {
			data[i] = new Vector<Float>();
		}

		for (RiskScore riskScore : finalScores) {
			samples.add(riskScore.getSample());
			for (int i = 0; i < scores.size(); i++) {
				data[i].add(riskScore.getScore(i));
			}
		}

	}

	public void save(String filename) {

		String[] columns = new String[scores.size() + 1];
		columns[0] = COLUMN_SAMPLE;
		for (int i = 0; i < scores.size(); i++) {
			columns[i + 1] = scores.get(i);
		}

		ITableWriter writer = new CsvTableWriter(filename, SEPARATOR);
		writer.setColumns(columns);

		for (int i = 0; i < samples.size(); i++) {
			writer.setString(COLUMN_SAMPLE, samples.get(i));
			for (int j = 0; j < scores.size(); j++) {
				writer.setString(scores.get(j), data[j].get(i) + "");
			}
			writer.next();
		}

		writer.close();

	}

	public void load(String filename) throws IOException {

		ITableReader reader = new CsvTableReader(filename, SEPARATOR);

		scores = new Vector<String>();
		String[] columns = reader.getColumns();
		for (String column : columns) {
			if (!column.equals(COLUMN_SAMPLE)) {
				scores.add(column);
			}
		}

		data = new Vector[scores.size()];
		for (int i = 0; i < scores.size(); i++) {
			data[i] = new Vector<Float>();
		}

		samples = new Vector<String>();

		while (reader.next()) {
			String sample = reader.getString(COLUMN_SAMPLE);
			samples.add(sample);
			for (int i = 0; i < scores.size(); i++) {
				Double value = reader.getDouble(scores.get(i));
				data[i].add(value.floatValue());
			}
		}
		reader.close();

	}

	public void merge(OutputFile file) throws IOException {

		if (samples.size() != file.getSamples().size()) {
			throw new IOException("Different number of samples. Expected " + samples.size() + " samples but found "
					+ file.getSamples().size() + " samples.");
		}

		if (scores.size() != file.getScores().size()) {
			throw new IOException("Different number of scores. Expected " + scores.size() + " scores but found "
					+ file.getScores().size() + " scores.");
		}

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].size(); j++) {
				float sum = data[i].get(j) + file.getData()[i].get(j);
				data[i].set(j, sum);
			}
		}
	}

	public List<String> getScores() {
		return scores;
	}

	public List<String> getSamples() {
		return samples;
	}

	public List<Float>[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return "samples: " + samples.size() + ", scores: " + scores.size();
	}

}
