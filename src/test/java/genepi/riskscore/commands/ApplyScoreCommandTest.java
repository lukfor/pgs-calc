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

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithMultipleScores() {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref",
				"test-data/test.scores.csv,test-data/test.scores.csv,test-data/test.scores.csv", "--out",
				"output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(4, reader.getColumns().length);
		assertEquals(2, samples);
		reader.close();

		reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1 + 3), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallAndCheckOutputFile() throws IOException {

		String[] args = { "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-0.4, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-1, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallMultipleFilesAndCheckOutputFile() throws IOException {

		String[] args = { "test-data/test.chr1.vcf", "test-data/test.chr2.vcf", "--ref", "test-data/test.scores.csv",
				"--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		assertEquals("LF001", sample);
		assertEquals(-(1 + 3), score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-(3 + 7), score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();
	}

	@Test
	public void testCallWithMinR2() throws IOException {

		String[] args = { "test-data/two.vcf", "--ref", "test-data/chr20.scores.csv", "--out", "output.csv", "--minR2",
				"0.5" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		ITableReader reader = new CsvTableReader("output.csv", ',');

		assertEquals(true, reader.next());

		double score = reader.getDouble("score");
		String sample = reader.getString("sample");
		assertEquals(-0.1, score, 0.0000001);

		assertEquals(true, reader.next());

		score = reader.getDouble("score");
		sample = reader.getString("sample");
		assertEquals("LF002", sample);
		assertEquals(-0.2, score, 0.0000001);

		assertEquals(false, reader.next());
		reader.close();

	}

	@Test
	public void testCallWithMissingVcf() {

		String[] args = { "--ref", "test-data/chr20.scores.csv", "--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);

	}

	@Test
	public void testCallWithChunk() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out", "output.csv",
				"--start", "61795", "--end", "63231" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("output.csv", ',');
		while (reader.next()) {
			samples++;

		}
		assertEquals(EXPECTED_SAMPLES, samples);
		reader.close();
	}

	@Test
	public void testCallWithStartOnly() {

		String[] args = { "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv", "--out", "output.csv",
				"--start", "61795" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(2, result);

	}

}
