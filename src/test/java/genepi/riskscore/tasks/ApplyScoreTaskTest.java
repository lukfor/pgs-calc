package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import genepi.riskscore.model.RiskScore;

public class ApplyScoreTaskTest {

	public static final int EXPECTED_SAMPLES = 51;

	@Test
	public void testPerformance() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilename("test-data/chr20.scores.csv");
		task.run();

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
		task.setVcfFilenames("test-data/small.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.csv");
		task.run();

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
		task.setVcfFilenames("test-data/single.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.csv");
		task.run();

		assertEquals(5, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(1, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(1, task.getCountVariantsAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.4, task.getRiskScores()[0].getScore(), 0.00001);

	}

	@Test
	public void testScoreSwitchEffectAllele() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/single.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.2.csv");
		task.run();

		assertEquals(5, task.getCountVariants());
		assertEquals(3, task.getCountVariantsUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(1, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(1, task.getCountVariantsAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.6, task.getRiskScores()[0].getScore(), 0.00001);
	}

	@Test
	public void testMinR2_06() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/two.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.csv");
		task.setMinR2(0.6f);
		task.run();

		assertEquals(5, task.getCountVariants());
		assertEquals(1, task.getCountVariantsUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(3, task.getCountVariantsNotUsed());
		assertEquals(3, task.getCountVariantsFilteredR2());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(0, task.getCountVariantsAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.2, task.getRiskScores()[0].getScore(), 0.00001);

	}

	@Test
	public void testMinR2_05() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/two.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.2.csv");
		task.setMinR2(0.5f);
		task.run();

		assertEquals(5, task.getCountVariants());
		assertEquals(2, task.getCountVariantsUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(2, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(0, task.getCountVariantsAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, task.getCountVariantsFilteredR2());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.3, task.getRiskScores()[0].getScore(), 0.00001);

	}

	@Test
	public void testMinR2_1() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/two.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.run();

		assertEquals(5, task.getCountVariants());
		assertEquals(0, task.getCountVariantsUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(4, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(0, task.getCountVariantsAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(4, task.getCountVariantsFilteredR2());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(0.0, task.getRiskScores()[0].getScore(), 0.00001);

	}

	@Test
	public void testMultipleFiles() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.vcf");
		task.setRiskScoreFilename("test-data/test.scores.csv");
		task.run();

		assertEquals(10, task.getCountVariants());
		assertEquals(11, task.getCountVariantsRiskScore());
		assertEquals(7, task.getCountVariantsUsed());
		assertEquals(4, task.getCountVariantsNotUsed());
		assertEquals(0, task.getCountVariantsSwitched());
		assertEquals(0, task.getCountVariantsFilteredR2());
		assertEquals(0, task.getCountVariantsMultiAllelic());
		assertEquals(0, task.getCountVariantsAlleleMissmatch());
		assertEquals(2, task.getCountSamples());

		assertEquals(2, task.getRiskScores().length);

		RiskScore first = task.getRiskScores()[0];
		assertEquals("LF001", first.getSample());
		assertEquals(-(1 + 3), first.getScore(), 0.0000001);

		RiskScore second = task.getRiskScores()[1];
		assertEquals("LF002", second.getSample());
		assertEquals(-(3 + 7), second.getScore(), 0.0000001);

	}

	@Test(expected = Exception.class)
	public void testWrongChromosome() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/single.wrong_chr.vcf");
		task.setRiskScoreFilename("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.run();

	}

	@Test(expected = Exception.class)
	public void testDifferentSamples() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.wrong.vcf");
		task.setRiskScoreFilename("test-data/test.scores.csv");
		task.setMinR2(1f);
		task.run();

	}

}
