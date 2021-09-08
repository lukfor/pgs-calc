package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.commands.ApplyScoreCommand;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.OutputFileWriter;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.monitors.TaskMonitorMock;
import picocli.CommandLine;

public class MergeScoreTaskTest {

	@BeforeClass
	public static void setup() {
		TaskService.setAnsiSupport(false);
	}

	@Test
	public void testMerge() throws Exception {

		MergeScoreTask task = new MergeScoreTask();
		task.setInputs("test-data/scores.chunk1.txt", "test-data/scores.chunk2.txt");
		task.setOutput("merged.task.txt");
		task.run(new TaskMonitorMock());

		assertEquals(FileUtil.readFileAsString("test-data/merged.expected.txt"),
				FileUtil.readFileAsString("merged.task.txt"));
	}

	@Test
	public void testMergingChunks() throws Exception {

		// Whole file
		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000018,PGS000027", "--out", "output.csv",
				"--report-json", "report.json" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		// chunks

		int lengthChr20 = 64444167;
		int chunkSize = 10000000;
		int chunks = (lengthChr20 / chunkSize);
		if (lengthChr20 % chunkSize > 0) {
			chunks++;
		}

		String[] chunkFiles = new String[chunks];
		String[] reportFiles = new String[chunks];

		int count = 0;
		for (int i = 1; i <= lengthChr20; i += chunkSize) {
			int start = i;
			int end = i + chunkSize - 1;
			String chunk = "output" + start + "_" + end + ".csv";
			String report = "output" + start + "_" + end + ".json";
			args = new String[] { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000018,PGS000027", "--start", start + "",
					"--end", end + "", "--out", chunk, "--report-json", report };
			result = new CommandLine(new ApplyScoreCommand()).execute(args);
			assertEquals(0, result);

			chunkFiles[count] = chunk;
			reportFiles[count] = report;
			count++;
		}

		MergeScoreTask task = new MergeScoreTask();
		task.setInputs(chunkFiles);
		task.setOutput("output.merged.txt");
		task.run(new TaskMonitorMock());

		assertEqualsScoreFiles("output.csv", "output.merged.txt", 0.0000001);

		MergeReportTask task2 = new MergeReportTask();
		task2.setInputs(reportFiles);
		task2.setOutput("report.merged.json");
		task2.run(new TaskMonitorMock());

		assertEqualsScoreFiles("report.json", "report.merged.json", 0.0000001);

	}

	@Test
	public void testMergeHugeFiles() throws Exception {

		int chunks = 3;
		int samples = 1000;
		int scores = 1000;

		// simulate huge files
		String[] files = new String[chunks];
		for (int i = 0; i < chunks; i++) {
			files[i] = "huge.chunk." + i + ".txt";
			CsvTableWriter writer = new CsvTableWriter(files[i], OutputFileWriter.SEPARATOR);
			String[] columns = new String[scores + 1];
			columns[0] = OutputFileWriter.COLUMN_SAMPLE;
			for (int j = 1; j <= scores; j++) {
				columns[j] = "score_" + j;
			}
			writer.setColumns(columns);
			for (int j = 1; j <= samples; j++) {
				writer.setString(OutputFileWriter.COLUMN_SAMPLE, "sample_" + j);
				for (int k = 1; k <= scores; k++) {
					writer.setDouble("score_" + k, Math.random());
				}
				writer.next();
			}
			writer.close();
		}

		MergeScoreTask task = new MergeScoreTask();
		task.setInputs(files);
		task.setOutput("merged.huge.task.txt");
		task.run(new TaskMonitorMock());

	}

	public void assertEqualsScoreFiles(String filename1, String filename2, double delta) throws IOException {

		OutputFile file1 = new OutputFile(filename1);

		OutputFile file2 = new OutputFile(filename2);

		assertEquals(file1.getSamples().size(), file2.getSamples().size());
		assertEquals(file1.getScores().size(), file2.getScores().size());
		for (int i = 0; i < file1.getScores().size(); i++) {
			assertEquals(file1.getScores().get(i), file2.getScores().get(i));
			double[] values1 = file1.getValuesByScore(i);
			double[] values2 = file2.getValuesByScore(i);
			assertEquals(values1.length, values2.length);
			assertEquals(values1.length, file1.getSamples().size());
			for (int j = 0; j < values1.length; j++) {
				assertEquals(values1[j], values2[j], delta);
			}
		}
	}

}
