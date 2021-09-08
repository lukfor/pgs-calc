package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.List;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeEffectsTask implements ITaskRunnable {

	public static final char EFFECTS_FILE_SEPARATOR = ',';

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
			this.inputs[i] = task.getOutputEffectsFilename();
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge effect files");

		assert (output != null);
		assert (inputs != null);
		assert (inputs.length > 0);

		CsvTableReader reader = new CsvTableReader(inputs[0], EFFECTS_FILE_SEPARATOR);

		CsvTableWriter writer = new CsvTableWriter(output, EFFECTS_FILE_SEPARATOR);
		writer.setColumns(reader.getColumns());
		reader.close();

		for (int i = 0; i < inputs.length; i++) {
			reader = new CsvTableReader(inputs[i], EFFECTS_FILE_SEPARATOR);
			while (reader.next()) {
				writer.setRow(reader.getRow());
				writer.next();
			}
			reader.close();
		}

		writer.close();

		monitor.done();

		inputs = null;

	}

}
