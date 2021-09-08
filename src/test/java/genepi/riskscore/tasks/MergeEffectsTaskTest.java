package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.commands.ApplyScoreCommand;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.monitors.TaskMonitorMock;
import picocli.CommandLine;

public class MergeEffectsTaskTest {

	@BeforeClass
	public static void setup() {
		TaskService.setAnsiSupport(false);
	}

	@Before
	public void beforeTest() {
		System.out.println("Clean up output directory");
		FileUtil.deleteDirectory("test-data-output");
		FileUtil.createDirectory("test-data-output");
	}
	
	@Test
	public void testMerge() throws Exception {

		MergeEffectsTask task = new MergeEffectsTask();
		task.setInputs("test-data/effects.chunk1.txt", "test-data/effects.chunk2.txt");
		task.setOutput("test-data-output/effects.task.txt");
		task.run(new TaskMonitorMock());

		assertEquals(FileUtil.readFileAsString("test-data/effects.expected.txt"),
				FileUtil.readFileAsString("test-data-output/effects.task.txt"));
	}

	@Test
	public void testMergingChunks() throws Exception {

		// Whole file
		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000018,PGS000027", "--out", "test-data-output/output.csv",
				"--report-json", "test-data-output/report.json", "--writeEffects", "test-data-output/effects.txt" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		// chunks

		int lengthChr20 = 64444167;
		int chunkSize = 10000000;
		int chunks = (lengthChr20 / chunkSize);
		if (lengthChr20 % chunkSize > 0) {
			chunks++;
		}

		String[] effectsFiles = new String[chunks];
		String[] chunkFiles = new String[chunks];
		String[] reportFiles = new String[chunks];

		int count = 0;
		for (int i = 1; i <= lengthChr20; i += chunkSize) {
			int start = i;
			int end = i + chunkSize - 1;
			String chunk = "test-data-output/output" + start + "_" + end + ".csv";
			String report = "test-data-output/output" + start + "_" + end + ".json";
			String effects = "test-data-output/effects" + start + "_" + end + ".txt";
			args = new String[] { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000018,PGS000027", "--start", start + "",
					"--end", end + "", "--out", chunk, "--report-json", report, "--writeEffects", effects };
			result = new CommandLine(new ApplyScoreCommand()).execute(args);
			assertEquals(0, result);

			chunkFiles[count] = chunk;
			reportFiles[count] = report;
			effectsFiles[count] = effects;
			count++;
		}

		MergeEffectsTask task = new MergeEffectsTask();
		task.setInputs(effectsFiles);
		task.setOutput("test-data-output/effects.merged.txt");
		task.run(new TaskMonitorMock());

		assertEquals(FileUtil.readFileAsString("test-data-output/effects.txt"), FileUtil.readFileAsString("test-data-output/effects.merged.txt"));

	}

}
