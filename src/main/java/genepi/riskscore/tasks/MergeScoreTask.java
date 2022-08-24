package genepi.riskscore.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.io.OutputFileReader;
import genepi.riskscore.io.OutputFileWriter;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeScoreTask implements ITaskRunnable {

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
			String filename = task.getOutput();
			if (new File(filename).exists()) {
				inputs.add(filename);
			}
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge score files");

		assert (output != null);

		if (inputs.isEmpty()) {
			throw new Exception("No chunks found to merge.");
		}

		OutputFileReader[] files = new OutputFileReader[inputs.size()];

		for (int i = 0; i < inputs.size(); i++) {
			files[i] = new OutputFileReader(inputs.get(i));
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
				if (!files[i].next()) {
					throw new Exception("Not all vcf files have the same number of samples.");
				}
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

		for (int i = 1; i < files.length; i++) {
			if (files[i].next()) {
				throw new Exception("Not all vcf files have the same number of samples.");
			}
			files[i].close();
		}

		writer.close();

		monitor.done();

		inputs = null;

	}

}
