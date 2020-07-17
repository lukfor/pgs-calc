package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.List;

import genepi.riskscore.io.ReportFile;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeReportTask implements ITaskRunnable {

	private String output;

	private ReportFile[] inputs;

	private ReportFile result;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... filenames) throws IOException {
		this.inputs = new ReportFile[filenames.length];
		for (int i = 0; i < inputs.length; i++) {
			String filename = filenames[i];
			this.inputs[i] = ReportFile.loadFromFile(filename);
		}
	}

	public void setInputs(List<ApplyScoreTask> tasks) {
		this.inputs = new ReportFile[tasks.size()];
		for (int i = 0; i < inputs.length; i++) {
			ApplyScoreTask task = tasks.get(i);
			this.inputs[i] = new ReportFile(task.getSummaries());
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.beginTask("Merge report files");

		assert (inputs != null);
		assert (inputs.length > 0);

		result = inputs[0];
		
		for (int i = 1; i < inputs.length; i++) {
			result.merge(inputs[i]);
		}

		// update statistics
		for (RiskScoreSummary summary : result.getSummaries()) {
			summary.updateStatistics();
		}

		if (output != null) {
			result.save(output);
			monitor.setTaskName("Report files merged and written to '" + output + "'");
		} else {
			monitor.setTaskName("Report files merged");
		}
		monitor.done();

	}

	public ReportFile getResult() {
		return result;
	}

}
