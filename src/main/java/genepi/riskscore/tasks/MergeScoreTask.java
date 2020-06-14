package genepi.riskscore.tasks;

import java.io.IOException;

import genepi.riskscore.io.OutputFile;

public class MergeScoreTask {

	private String output;

	private String[] inputs;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... inputs) {
		this.inputs = inputs;
	}

	public void run() throws IOException {
		assert (inputs != null);
		assert (output != null);
		assert (inputs.length > 0);

		OutputFile first = new OutputFile();
		first.load(inputs[0]);

		for (int i = 1; i < inputs.length; i++) {
			OutputFile next = new OutputFile();
			next.load(inputs[i]);
			first.merge(next);
		}

		first.save(output);
	}

}
