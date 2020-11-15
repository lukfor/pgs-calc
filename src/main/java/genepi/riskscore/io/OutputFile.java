package genepi.riskscore.io;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreSummary;

public class OutputFile {

	public static final String COLUMN_SAMPLE = "sample";

	public static final char SEPARATOR = ',';

	private List<String> samples;

	private List<Double>[] data;

	private List<String> scores;

	private String filename;

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
			data[i] = new Vector<Double>();
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
				writer.setDouble(scores.get(j), data[j].get(i));
			}
			writer.next();
		}

		writer.close();

	}

	public static OutputFile loadFromFile(String filename) throws IOException {
		OutputFile file = new OutputFile();
		file.load(filename);
		return file;
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
			data[i] = new Vector<Double>();
		}

		samples = new Vector<String>();

		while (reader.next()) {
			String sample = reader.getString(COLUMN_SAMPLE);
			samples.add(sample);
			for (int i = 0; i < scores.size(); i++) {
				Double value = reader.getDouble(scores.get(i));
				data[i].add(value);
			}
		}
		reader.close();

		this.filename = filename;

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
				double sum = data[i].get(j) + file.getData(i).get(j);
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

	public List<Double> getData(int index) {
		return data[index];
	}

	public List<Double> getData(String score) {
		int index = scores.indexOf(score);
		return data[index];
	}

	public String getName() {

		if (filename == null) {
			return "unknown";
		}

		// Cleanup filename and use it as name (remove extension etc..)
		String name = FileUtil.getFilename(filename);
		name = name.replaceAll(".txt.gz", "");
		name = name.replaceAll(".txt", "");
		name = name.replaceAll(".csv.gz", "");
		name = name.replaceAll(".csv", "");
		return name;

	}

	@Override
	public String toString() {
		return "samples: " + samples.size() + ", scores: " + scores.size();
	}

}
