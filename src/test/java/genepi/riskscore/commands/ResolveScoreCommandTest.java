package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.io.PGSCatalog;
import lukfor.progress.TaskService;
import picocli.CommandLine;

public class ResolveScoreCommandTest {

	public static String DBSNP_INDEX = "test-data/dbsnp-index.small.txt.gz";

	public static final int EXPECTED_SAMPLES = 51;

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
	public void testCall() {

		String[] args = { "--in", "test-data/PGS000001.txt.gz", "--out", "test-data-output/PGS000001.converted.txt",
				"--dbsnp", DBSNP_INDEX };
		int result = new CommandLine(new ResolveScoreCommand()).execute(args);
		assertEquals(0, result);

		int variants = 0;
		ITableReader reader = new CsvTableReader("test-data-output/PGS000001.converted.txt", '\t');
		while (reader.next()) {
			variants++;

		}
		assertEquals(77, variants);
		reader.close();
	}

	@Test
	public void testResolveofPGSId() {

		String[] args = { "--in", "PGS000001", "--out", "test-data-output/PGS000001.converted.txt", "--dbsnp",
				DBSNP_INDEX };
		int result = new CommandLine(new ResolveScoreCommand()).execute(args);
		assertEquals(0, result);

		int variants = 0;
		ITableReader reader = new CsvTableReader("test-data-output/PGS000001.converted.txt", '\t');
		while (reader.next()) {
			variants++;

		}
		assertEquals(77, variants);
		reader.close();
	}

	@Test
	public void testResolveofFileWithPositions() {

		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt", "--dbsnp",
				DBSNP_INDEX };
		int result = new CommandLine(new ResolveScoreCommand()).execute(args);
		assertEquals(0, result);

		int variants = 0;
		ITableReader reader = new CsvTableReader("test-data-output/PGS000899.converted.txt", '\t');
		while (reader.next()) {
			variants++;

		}
		assertEquals(11276, variants);
		reader.close();
	}

	@Test
	public void testResolveofFileWithPositionsAndChain() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt", "--dbsnp",
				DBSNP_INDEX, "--chain", "test-data/chains/hg19ToHg38.over.chain.gz" };
		int result = new CommandLine(new ResolveScoreCommand()).execute(args);
		assertEquals(0, result);

		int variants = 0;
		ITableReader reader = new CsvTableReader("test-data-output/PGS000899.converted.txt", '\t');
		while (reader.next()) {
			variants++;

		}
		assertEquals(11276, variants);
		reader.close();
	}
}
