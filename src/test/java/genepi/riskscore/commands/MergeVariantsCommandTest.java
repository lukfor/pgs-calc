package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.VariantFile;
import lukfor.progress.TaskService;
import picocli.CommandLine;

public class MergeVariantsCommandTest {

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

		args = new String[variantFiles.length + 2];
		for (int i = 0; i < variantFiles.length; i++) {
			args[i] = variantFiles[i];
		}
		args[variantFiles.length] = "--out";
		args[variantFiles.length + 1] = "test-data-output/variants.merged.txt";

		result = new CommandLine(new MergeVariantsCommand()).execute(args);
		assertEquals(0, result);

		// compare files (order difference of not included variants)
		ITableReader reader = new CsvTableReader("test-data-output/variants.txt", VariantFile.SEPARATOR);
		HashMap<Integer, Integer> variants = new HashMap<Integer, Integer>();
		while (reader.next()) {
			int position = reader.getInteger(VariantFile.POSITION);
			int include = reader.getInteger(VariantFile.INCLUDE);
			variants.put(position, include);
		}
		reader.close();

		ITableReader reader2 = new CsvTableReader("test-data-output/variants.merged.txt", VariantFile.SEPARATOR);
		while (reader2.next()) {
			int position = reader2.getInteger(VariantFile.POSITION);
			Integer include = reader2.getInteger(VariantFile.INCLUDE);
			Integer expectIncluded = variants.get(position);
			assertNotNull(expectIncluded);
			assertEquals(expectIncluded, include);
		}
		reader2.close();

	}

}
