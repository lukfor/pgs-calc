package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import picocli.CommandLine;

public class LiftOverScoreCommandTest {

	@Test
	public void testWithChainFile() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt",
				"--chain", "test-data/chains/hg19ToHg38.over.chain.gz" };
		int result = new CommandLine(new LiftOverScoreCommand()).execute(args);
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
	public void testWithBuilds() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt",
				"--source", "hg19", "--target", "hg38" };
		int result = new CommandLine(new LiftOverScoreCommand()).execute(args);
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
	public void testWithMissingBuilds() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt" };
		int result = new CommandLine(new LiftOverScoreCommand()).execute(args);
		assertEquals(2, result);
	}

	@Test
	public void testWithMissingBuilds2() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt",
				"--source", "hg19" };
		int result = new CommandLine(new LiftOverScoreCommand()).execute(args);
		assertEquals(1, result);
	}

	@Test
	public void testWithMissingBuilds3() {
		String[] args = { "--in", "test-data/PGS000957.txt.gz", "--out", "test-data-output/PGS000899.converted.txt",
				"--source", "hg19", "--target", "hg55" };
		int result = new CommandLine(new LiftOverScoreCommand()).execute(args);
		assertEquals(1, result);
	}

}
