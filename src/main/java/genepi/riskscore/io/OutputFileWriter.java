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

public class OutputFileWriter {

	public static final String COLUMN_SAMPLE = "sample";

	public static final char SEPARATOR = ',';

	private List<String> samples;

	private List<Double>[] data;

	private List<String> scores;

	public OutputFileWriter() {

	}

	public OutputFileWriter(List<RiskScore> finalScores, RiskScoreSummary[] summaries) {

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

}
