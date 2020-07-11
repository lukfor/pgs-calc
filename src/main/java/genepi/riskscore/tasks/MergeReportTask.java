package genepi.riskscore.tasks;

import genepi.riskscore.io.ReportFile;
import genepi.riskscore.model.RiskScoreSummary;

public class MergeReportTask {

	private String output;

	private String[] inputs;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... inputs) {
		this.inputs = inputs;
	}

	public void run() throws Exception {
		assert (inputs != null);
		assert (output != null);
		assert (inputs.length > 0);

		ReportFile first = new ReportFile();
		first.load(inputs[0]);

		for (int i = 1; i < inputs.length; i++) {
			ReportFile next = new ReportFile();
			next.load(inputs[i]);
			first.merge(next);
		}
		
		//update statistics
		for(RiskScoreSummary summary: first.getSummaries()) {
			summary.updateStatistics();
		}
		
		first.save(output);
	}

}
