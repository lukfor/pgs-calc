package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import picocli.CommandLine;

public class ApplyScoreCommandTest {

	public static final int EXPECTED_SAMPLES = 51;

	@Test
	public void testCall() {

		/*
		 * String[] args = { "--chr", "20", "--vcf", "test-data/chr20.dose.vcf.gz",
		 * "--ref", "/Users/lukas/Downloads/Khera.et.al_GPS_BMI_Cell_2019.txt", "--out",
		 * "output.csv" };
		 */
		String[] args = { "--chr", "20", "--vcf", "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv",
				"--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(EXPECTED_SAMPLES, samples);

	}

	@Test
	public void testCallAndCheckOutputFile() throws IOException {

		String[] args = { "--chr", "20", "--vcf", "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out",
				"output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		String chr = reader.getString("chr");
		assertEquals("20", chr);
		assertEquals("LF001", sample);
		assertEquals(0.4, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		chr = reader.getString("chr");
		assertEquals("20", chr);
		assertEquals("LF002", sample);
		assertEquals(1, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallWithMinR2() throws IOException {

		String[] args = { "--chr", "20", "--vcf", "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out",
				"output.csv", "--minR2","0.5" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		String chr = reader.getString("chr");
		assertEquals("20", chr);
		assertEquals("LF001", sample);
		assertEquals(0.1, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		chr = reader.getString("chr");
		assertEquals("20", chr);
		assertEquals("LF002", sample);
		assertEquals(0.2, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
		
	}

	@Test
	public void testCallWithWrongChromosome() {

		String[] args = { "--chr", "21", "--vcf", "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv",
				"--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);

	}
	
}
