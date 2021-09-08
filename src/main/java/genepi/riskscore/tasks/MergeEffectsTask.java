package genepi.riskscore.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeEffectsTask implements ITaskRunnable {

	public static final char EFFECTS_FILE_SEPARATOR = ',';

	private String output;

	private List<String> inputs = new Vector<>();

	public void setOutput(String output) {
		this.output = output;
	}

	public void setInputs(String... filenames) throws IOException {
		for (String filename : filenames) {
			if (new File(filename).exists()) {
				inputs.add(filename);
			}
		}
	}

	public void setInputs(List<ApplyScoreTask> tasks) {
		for (int i = 0; i < tasks.size(); i++) {
			ApplyScoreTask task = tasks.get(i);
			String filename = task.getOutputEffectsFilename();
			if (new File(filename).exists()) {
				inputs.add(filename);
			}
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge effect files");

		assert (output != null);

		if (inputs.isEmpty()) {
			throw new Exception("No chunks found to merge.");
		}

		CsvTableReader reader = new CsvTableReader(inputs.get(0), EFFECTS_FILE_SEPARATOR);

		CsvTableWriter writer = new CsvTableWriter(output, EFFECTS_FILE_SEPARATOR);
		writer.setColumns(reader.getColumns());
		reader.close();

		for (int i = 0; i < inputs.size(); i++) {
			reader = new CsvTableReader(inputs.get(i), EFFECTS_FILE_SEPARATOR);
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
