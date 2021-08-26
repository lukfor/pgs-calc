package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.List;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.io.OutputFileReader;
import genepi.riskscore.io.OutputFileWriter;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeScoreTask implements ITaskRunnable {

	private String output;

	private String[] inputs;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... filenames) throws IOException {
		this.inputs = filenames;
	}

	public void setInputs(List<ApplyScoreTask> tasks) {
		this.inputs = new String[tasks.size()];
		for (int i = 0; i < inputs.length; i++) {
			ApplyScoreTask task = tasks.get(i);
			this.inputs[i] = task.getOutput();
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge score files");

		assert (output != null);
		assert (inputs != null);
		assert (inputs.length > 0);

		OutputFileReader[] files = new OutputFileReader[inputs.length];

		for (int i = 0; i < inputs.length; i++) {
			files[i] = new OutputFileReader(inputs[i]);
		}

		List<String> scores = files[0].getScores();

		String[] columns = new String[scores.size() + 1];
		columns[0] = OutputFileWriter.COLUMN_SAMPLE;
		for (int i = 0; i < scores.size(); i++) {
			columns[i + 1] = scores.get(i);
		}

		ITableWriter writer = new CsvTableWriter(output, OutputFileWriter.SEPARATOR);
		writer.setColumns(columns);

		while (files[0].next()) {

			double[] values = files[0].getValues();
			for (int i = 1; i < files.length; i++) {
				files[i].next();
				double[] data = files[i].getValues();
				for (int j = 0; j < data.length; j++) {
					values[j] += data[j];
				}
			}

			writer.setString(OutputFileWriter.COLUMN_SAMPLE, files[0].getSample());
			for (int i = 0; i < scores.size(); i++) {
				writer.setDouble(scores.get(i), values[i]);
			}
			writer.next();

		}

		for (int i = 0; i < files.length; i++) {
			files[i].close();
		}

		writer.close();

		monitor.done();

		inputs = null;

	}

}
