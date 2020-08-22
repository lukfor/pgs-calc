package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.List;

import genepi.riskscore.io.OutputFile;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeScoreTask implements ITaskRunnable {

	private String output;

	private OutputFile[] inputs;

	private OutputFile result;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... filenames) throws IOException {
		this.inputs = new OutputFile[filenames.length];
		for (int i = 0; i < inputs.length; i++) {
			String filename = filenames[i];
			this.inputs[i] = OutputFile.loadFromFile(filename);
		}
	}

	public void setInputs(List<ApplyScoreTask> tasks) {
		this.inputs = new OutputFile[tasks.size()];
		for (int i = 0; i < inputs.length; i++) {
			ApplyScoreTask task = tasks.get(i);
			this.inputs[i] = new OutputFile(task.getRiskScores(), task.getSummaries());
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge score files");

		assert (inputs != null);
		assert (inputs.length > 0);

		result = inputs[0];

		for (int i = 1; i < inputs.length; i++) {
			result.merge(inputs[i]);
		}

		if (output != null) {
			result.save(output);
			monitor.update("Score files merged and written to '" + output + "'");
		} else {
			monitor.update("Score files merged");
		}

		monitor.done();

		inputs = null;

	}

	public OutputFile getResult() {
		return result;
	}

}
