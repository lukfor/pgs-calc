package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.commands.ApplyScoreCommand;
import genepi.riskscore.io.PGSCatalog;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.monitors.TaskMonitorMock;
import picocli.CommandLine;

public class MergeVariantsTaskTest {

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
	public void testMergingChunks() throws Exception {

		// Whole file
		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref",
				"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--out", "test-data-output/output.csv",
				"--report-json", "test-data-output/report.json", "--writeVariants", "test-data-output/variants.txt" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		// chunks

		int lengthChr20 = 64444167;
		int chunkSize = 10000000;
		int chunks = (lengthChr20 / chunkSize);
		if (lengthChr20 % chunkSize > 0) {
			chunks++;
		}

		String[] variantFiles = new String[chunks];
		String[] chunkFiles = new String[chunks];
		String[] reportFiles = new String[chunks];

		int count = 0;
		for (int i = 1; i <= lengthChr20; i += chunkSize) {
			int start = i;
			int end = i + chunkSize - 1;
			String chunk = "test-data-output/output" + start + "_" + end + ".csv";
			String report = "test-data-output/output" + start + "_" + end + ".json";
			String variants = "test-data-output/variants" + start + "_" + end + ".txt";
			args = new String[] { "test-data/chr20.dose.vcf.gz", "--ref",
					"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--start", start + "", "--end", end + "",
					"--out", chunk, "--report-json", report, "--writeVariants", variants };
			result = new CommandLine(new ApplyScoreCommand()).execute(args);
			assertEquals(0, result);

			chunkFiles[count] = chunk;
			reportFiles[count] = report;
			variantFiles[count] = variants;
			count++;
		}

		MergeVariantsTask task = new MergeVariantsTask();
		task.setInputs(variantFiles);
		task.setOutput("test-data-output/variants.merged.txt");
		task.run(new TaskMonitorMock());

		assertEquals(FileUtil.readFileAsString("test-data-output/variants.txt"),
				FileUtil.readFileAsString("test-data-output/variants.merged.txt"));

	}

}
