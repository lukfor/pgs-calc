package genepi.riskscore.io;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import genepi.riskscore.model.RiskScoreSummary;

public class ReportFile {

	private List<RiskScoreSummary> summaries;

	public ReportFile() {
		this.summaries = new Vector<RiskScoreSummary>();
	}

	public ReportFile(RiskScoreSummary[] summaries) {
		this.summaries = new Vector<RiskScoreSummary>();
		for (RiskScoreSummary summary : summaries) {
			this.summaries.add(summary);
		}
	}

	public void save(String filename) throws JsonIOException, IOException {

		Gson gson = new Gson();
		Type type = new TypeToken<List<RiskScoreSummary>>() {
		}.getType();
		FileWriter writer = new FileWriter(filename);
		gson.toJson(summaries, type, writer);
		writer.close();

	}

	public static ReportFile loadFromFile(String filename) throws IOException {
		ReportFile reportFile = new ReportFile();
		reportFile.load(filename);
		return reportFile;
	}

	public void load(String filename) throws IOException {

		Gson gson = new Gson();
		Type type = new TypeToken<List<RiskScoreSummary>>() {
		}.getType();
		this.summaries = gson.fromJson(new FileReader(filename), type);

	}

	public void merge(ReportFile file) throws Exception {

		summaries.size();
		file.summaries.size();

		if (summaries.size() != file.summaries.size()) {
			throw new Exception("Different number of scores. Expected " + summaries.size() + " samples but found "
					+ file.summaries.size() + " scores.");
		}

		// sum up all statistics
		for (int i = 0; i < summaries.size(); i++) {
			summaries.get(i).merge(file.summaries.get(i));
		}

	}

	public void mergeWithMeta(MetaFile meta) {
		for (RiskScoreSummary summary : summaries) {
			Object data = meta.getDataById(summary.getName());
			if (data != null) {
				summary.setMeta(data);
			}
		}
	}

	public List<RiskScoreSummary> getSummaries() {
		return summaries;
	}

	@Override
	public String toString() {
		return "scores: " + summaries.size();
	}

}
