package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import picocli.CommandLine;

public class ApplyScoreCommandTest {

	public static final int EXPECTED_SAMPLES = 51;

	@Test
	public void testCall() {

		/*String[] args = { "--chr", "20", "--vcf", "test-data/chr20.dose.vcf.gz", "--ref", "/Users/lukas/Downloads/Khera.et.al_GPS_BMI_Cell_2019.txt",
				"--out", "output.csv" };*/
		String[] args = { "--chr", "20", "--vcf", "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv",
				"--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(0, result);

		int samples = 0;
		ITableReader reader = new CsvTableReader("output.csv", ',');
		while (reader.next()) {
			samples++;
			// TODO: compare with expected results

		}
		assertEquals(EXPECTED_SAMPLES, samples);

	}

	@Test
	public void testCallWithWrongChromosome() {

		String[] args = { "--chr", "21", "--vcf", "test-data/chr20.dose.vcf.gz", "--ref", "test-data/chr20.scores.csv",
				"--out", "output.csv" };
		int result = new CommandLine(new ApplyScoreCommand()).execute(args);
		assertEquals(1, result);

	}

}
