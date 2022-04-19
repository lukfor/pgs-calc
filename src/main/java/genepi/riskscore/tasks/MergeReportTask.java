package genepi.riskscore.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.riskscore.io.ReportFile;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeReportTask implements ITaskRunnable {

	private String output;

	private List<ReportFile> inputs = new Vector<ReportFile>();

	private ReportFile result;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... filenames) throws IOException {
		for (String filename : filenames) {
			if (new File(filename).exists()) {
				inputs.add(ReportFile.loadFromFile(filename));
			}
		}
	}

	public void setInputs(List<ApplyScoreTask> tasks) {
		for (int i = 0; i < tasks.size(); i++) {
			ApplyScoreTask task = tasks.get(i);
			String filename = task.getOutput();
			if (new File(filename).exists()) {
				inputs.add(new ReportFile(task.getRiskScores()));
			}
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge report files");

		if (inputs.isEmpty()) {
			throw new Exception("No chunks found to merge.");
		}
		result = inputs.get(0);

		for (int i = 1; i < inputs.size(); i++) {
			result.merge(inputs.get(i));
		}

		// update statistics
		for (RiskScoreSummary summary : result.getSummaries()) {
			summary.updateStatistics();
		}

		if (output != null) {
			result.save(output);
			monitor.update("Report files merged and written to '" + output + "'");
		} else {
			monitor.update("Report files merged");
		}
		monitor.done();
		
		inputs = null;

	}

	public ReportFile getResult() {
		return result;
	}

}
