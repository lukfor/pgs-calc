package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import lukfor.progress.TaskService;
import picocli.CommandLine;

public class ApplyScoreCommandTest {

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

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out",
				"test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithPGSID() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000028", "--out", "test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(2, reader.getColumns().length);
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithMultiplePGSIDs() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "PGS000028,PGS000027", "--out",
				"test-data-output/output.csv", "--report-html", "test-data-output/output.html", "--meta",
				"test-data/pgs-catalog-small.json" };
		App.ARGS = args;
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(3, reader.getColumns().length);
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithPGSCatalogIDFile() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/pgs-ids.txt", "--out",
				"test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(3, reader.getColumns().length);
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithPGSCatalogIDAndRsIDs() {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "PGS000001", "--out",
				"test-data-output/output.csv", "--dbsnp", DBSNP_INDEX };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;
		}
		assertEquals(2, reader.getColumns().length);
		assertEquals(2, samples);
		reader.close();
	}

	@Test
	public void testCallWithMultipleScores() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref",
				"test-data/test.scores.csv,test-data/test.scores.csv,test-data/test.scores.csv", "--out",
				"test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(4, reader.getColumns().length);
		assertEquals(2, samples);
		reader.close();

		reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("test.scores");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1 + 3), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("test.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallAndCheckOutputFile() throws IOException {

		String[] args = { "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out",
				"test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("chr20.scores");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-0.4, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("chr20.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-1, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallMultipleFilesAndCheckOutputFile() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores.csv",
				"--out", "test-data-output/output.csv","--report-html","test.html" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("test.scores");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1 + 3), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("test.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallMultipleFilesAndEmptyChromosomesAndCheckOutputFile() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores2.csv",
				"--out", "test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("test.scores2");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("test.scores2");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallWithSampleFilter() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores.csv",
				"--out", "test-data-output/output.csv", "--samples", "test-data/samples.txt" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("test.scores");
		String sample = reader.getString("sample");
		score = reader.getDouble("test.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}
	
	@Test
	public void testCallSamplePopulation() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores.csv",
				"--out", "test-data-output/output.csv", "--samples", "test-data/samples-population.txt", "--report-html", "population.html" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("test.scores");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1 + 3), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("test.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallWithSampleFilterMissingFile() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores.csv",
				"--out", "test-data-output/output.csv", "--samples", "test-data/samples2.txt" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);
	}

	@Test
	public void testCallWithMinR2() throws IOException {

		String[] args = { "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out",
				"test-data-output/output.csv", "--minR2", "0.5" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("chr20.scores");
		String sample = reader.getString("sample");
		assertEquals(-0.1, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("chr20.scores");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-0.2, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();

	}

	@Test
	public void testCallWithMissingVcf() {

		String[] args = { "--ref", "test-data/chr20.scores.csv", "--out", "test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);

	}

	@Test
	public void testCallWithChunk() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out",
				"test-data-output/output.csv", "--start", "61795", "--end", "63231" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("test-data-output/output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithStartOnly() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out",
				"test-data-output/output.csv", "--start", "61795" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(2, result);

	}

	@Test
	public void testDifferentSamples() throws Exception {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.wrong.vcf", "--ref",
				"test-data/chr20.scores.csv", "--out", "test-data-output/output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);
	}

	@Test
	public void testIncludeVariants() {
		// Whole file
		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref",
				"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--out", "test-data-output/output2.csv",
				"--report-json", "test-data-output/report.json", "--writeVariants", "test-data-output/variants2.txt" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		args = new String[] { "test-data/chr20.dose.vcf.gz", "--ref",
				"test-data/PGS000957.txt.gz,test-data/PGS000958.txt.gz", "--out", "test-data-output/output3.csv",
				"--report-json", "test-data-output/report.json", "--includeVariants",
				"test-data-output/variants2.txt" };
		result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

	}

	@Test
	public void testDuplicate() {
		// Whole file
		String[] args = { "/Users/lukfor/Data/metsim/chr1.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr2.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr3.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr4.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr5.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr6.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr7.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr8.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr9.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr10.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr11.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr12.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr13.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr14.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr15.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr16.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr17.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr18.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr19.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr20.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chr21.clean.vcf.gz", "/Users/lukfor/Data/metsim/chr22.clean.vcf.gz",
				"/Users/lukfor/Data/metsim/chrX.clean.vcf.gz", "--ref",
				"/Users/lukfor/Data/pgs-catalog/pgs-catalog-20220712-hg38/scores/PGS002585.txt.gz", "--out",
				//"/Users/lukfor/Data/pgs-catalog/pgs-catalog-20220731/scores/PGS002585.txt.gz", "--out",
				"test-data-output/output2.csv", "--report-html", "test-data-output/report.html", "--report-json",
				"test-data-output/report.json", "--write-variants", "test-data-output/variants.txt"};
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

	}
	
	
	@Test
	public void testDuplicate5() {
		// Whole file
		
		String pattern = "/Users/lukfor/Data/metsim/topmed/chr${chr}.vcf.gz";
		String regex = "\\$\\{chr\\}";
		
		String[] args = {
				pattern.replaceAll(regex, "1"),
				pattern.replaceAll(regex, "2"),
				pattern.replaceAll(regex, "3"),
				pattern.replaceAll(regex, "4"),
				pattern.replaceAll(regex, "5"),
				pattern.replaceAll(regex, "6"),
				pattern.replaceAll(regex, "7"),
				pattern.replaceAll(regex, "8"),
				pattern.replaceAll(regex, "9"),
				pattern.replaceAll(regex, "10"),
				pattern.replaceAll(regex, "11"),
				pattern.replaceAll(regex, "12"),
				pattern.replaceAll(regex, "13"),
				pattern.replaceAll(regex, "14"),
				pattern.replaceAll(regex, "15"),
				pattern.replaceAll(regex, "16"),
				pattern.replaceAll(regex, "17"),
				pattern.replaceAll(regex, "18"),
				pattern.replaceAll(regex, "19"),
				pattern.replaceAll(regex, "20"),
				pattern.replaceAll(regex, "21"),
				pattern.replaceAll(regex, "22"),
				pattern.replaceAll(regex, "X"),
				"--ref",
				"/Users/lukfor/Data/pgs-catalog/pgs-catalog-20220712-hg38/scores/PGS000013.txt.gz", "--out",
				//"/Users/lukfor/Data/pgs-catalog/pgs-catalog-20220731/scores/PGS000630.txt.gz", "--out",
				"test-data-output/output2.csv", "--report-html", "test-data-output/report.html", "--report-json",
				"test-data-output/report.json", "--write-variants", "test-data-output/variants.txt", "--verbose","--proxies", "/Users/lukfor/Data/proxy-map/proxy-map-eur.txt.gz", "--fix-strand-flips" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

	}

}
