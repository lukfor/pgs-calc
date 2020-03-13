package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApplyScoreTaskTest {

	public static final int EXPECTED_SAMPLES = 51;

	@Test
	public void testPerformance() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.run("20", "test-data/chr20.dose.vcf.gz", "test-data/chr20.scores.csv");

		assertEquals(63480, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(1, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(0, task.getCountVariantsAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

	
	@Test
	public void testMultiPostion() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.run("20", "test-data/small.vcf", "test-data/chr20.scores.csv");

		assertEquals(4, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(1, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(1, task.getCountVariantsAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());
		assertEquals(EXPECTED_SAMPLES, task.getRiskScores().length);
	}
	
	@Test
	public void testScore() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.run("20", "test-data/single.vcf", "test-data/chr20.scores.csv");

		assertEquals(5, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(1, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(1, task.getCountVariantsAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);		
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(0.4, task.getRiskScores()[0].getScore(), 0.00001);
		
		task = new ApplyScoreTask();
		task.run("20", "test-data/single.vcf", "test-data/chr20.scores.2.csv");

		assertEquals(5, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(1, task.getCountVariantsAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(0.6, task.getRiskScores()[0].getScore(), 0.00001);
	}

	
}
