package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApplyScoreTaskTest {

	public static final int EXPECTED_SAMPLES = 51;

	@Test
	public void testRun() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.run("20", "test-data/chr20.dose.vcf.gz", "test-data/chr20.scores.csv");

		assertEquals(63480, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(1, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

}
