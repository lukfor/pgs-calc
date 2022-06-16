package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.io.PGSCatalog;
import lukfor.progress.TaskService;
import picocli.CommandLine;

public class MergeEffectsCommandTest {

	@BeforeClass
	public static void setup() {
		TaskService.setAnsiSupport(false);
		PGSCatalog.ENABLE_CACHE = false;
	}

	@Before
	public void beforeTest() {
		System.out.println("Clean up output directory");
		FileUtil.deleteDirectory("test-data-output");
		FileUtil.createDirectory("test-data-output");
	}

	@Test
	public void testMerge() throws Exception {

		String[] args = { "test-data/effects.chunk1.txt", "test-data/effects.chunk2.txt", "--out",
				"test-data-output/effects.task.txt" };
		int result = new CommandLine(new MergeEffectsCommand()).execute(args);
		assertEquals(0, result);

		assertEquals(FileUtil.readFileAsString("test-data/effects.expected.txt"),
				FileUtil.readFileAsString("test-data-output/effects.task.txt"));
	}

	@Test
	public void testMergingChunks() throws Exception {

		// Whole file
		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref",
				"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--out", "test-data-output/output.csv",
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
			args = new String[] { "test-data/chr20.dose.vcf.gz", "--ref",
					"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--start", start + "", "--end", end + "",
					"--out", chunk, "--report-json", report, "--writeEffects", effects };
			result = new CommandLine(new ApplyScoreCommand()).execute(args);
			assertEquals(0, result);

			chunkFiles[count] = chunk;
			reportFiles[count] = report;
			effectsFiles[count] = effects;
			count++;
		}

		args = new String[effectsFiles.length + 2];
		for (int i = 0; i < effectsFiles.length; i++) {
			args[i] = effectsFiles[i];
		}
		args[effectsFiles.length] = "--out";
		args[effectsFiles.length + 1] = "test-data-output/effects.merged.txt";

		result = new CommandLine(new MergeEffectsCommand()).execute(args);
		assertEquals(0, result);

		assertEquals(FileUtil.readFileAsString("test-data-output/effects.txt"),
				FileUtil.readFileAsString("test-data-output/effects.merged.txt"));

	}

}
