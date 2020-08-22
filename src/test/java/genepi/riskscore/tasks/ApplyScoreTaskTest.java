package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.VariantFile;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.monitors.TaskMonitorMock;

public class ApplyScoreTaskTest {

	public static final int EXPECTED_SAMPLES = 51;

	@BeforeClass
	public static void setup() {
		TaskService.setAnsiSupport(false);
	}
	
	@Test
	public void testPerformance() throws Exception {
		
		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.run(new TaskMonitorMock());

		assertEquals(63480, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

	@Test
	public void testMultiPostion() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/small.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.run(new TaskMonitorMock());

		assertEquals(4, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());
		assertEquals(EXPECTED_SAMPLES, task.getRiskScores().length);
	}

	@Test
	public void testMultipleScores() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv", "test-data/chr20.scores.csv",
				"test-data/chr20.scores.csv");
		task.run(new TaskMonitorMock());

		assertEquals(63480, task.getCountVariants());

		assertEquals(3, task.getSummaries().length);

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

	@Test
	public void testScore() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.4, task.getRiskScores()[0].getScore(0), 0.00001);

	}

	@Test
	public void testScoreSwitchEffectAllele() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());
		assertEquals(1, task.getCountSamples());
		assertEquals(1, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.6, task.getRiskScores()[0].getScore(0), 0.00001);
	}

	@Test
	public void testMinR2_06() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.setMinR2(0.6f);
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(1, summary.getVariantsUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(3, summary.getVariantsNotUsed());
		assertEquals(3, summary.getR2Filtered());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.2, task.getRiskScores()[0].getScore(0), 0.00001);

	}

	@Test
	public void testMinR2_05() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(0.5f);
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(2, summary.getVariantsUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(2, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, summary.getR2Filtered());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(-0.3, task.getRiskScores()[0].getScore(0), 0.00001);

	}

	@Test
	public void testMinR2_1() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(0, summary.getVariantsUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(4, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(4, summary.getR2Filtered());
		assertEquals(2, task.getRiskScores().length);
		assertEquals("LF001", task.getRiskScores()[0].getSample());
		assertEquals(0.0, task.getRiskScores()[0].getScore(0), 0.00001);

	}

	/*@Test
	public void testMultipleFiles() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.vcf");
		task.setRiskScoreFilenames("test-data/test.scores.csv");
		task.run(new TaskMonitorMock());

		assertEquals(10, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(11, summary.getVariants());
		assertEquals(7, summary.getVariantsUsed());
		assertEquals(4, summary.getVariantsNotUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(0, summary.getR2Filtered());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());

		assertEquals(2, task.getRiskScores().length);

		RiskScore first = task.getRiskScores()[0];
		assertEquals("LF001", first.getSample());
		assertEquals(-(1 + 3), first.getScore(0), 0.0000001);

		RiskScore second = task.getRiskScores()[1];
		assertEquals("LF002", second.getSample());
		assertEquals(-(3 + 7), second.getScore(0), 0.0000001);

	}

	@Test
	public void testWriteVariantFile() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.vcf");
		task.setRiskScoreFilenames("test-data/test.scores.csv");
		task.setOutputVariantFilename("variants.txt");
		task.run(new TaskMonitorMock());

		assertEquals(10, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(11, summary.getVariants());
		assertEquals(7, summary.getVariantsUsed());

		VariantFile variants = new VariantFile("variants.txt");
		variants.buildIndex("1");
		assertEquals(4, variants.getCacheSize());

		variants = new VariantFile("variants.txt");
		variants.buildIndex("2");
		variants.getCacheSize();
		assertEquals(3, variants.getCacheSize());

	}

	@Test
	public void testReadVariantsFile() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.vcf");
		task.setRiskScoreFilenames("test-data/test.scores.csv");
		task.setIncludeVariantFilename("test-data/variants.txt");
		task.run(new TaskMonitorMock());

		assertEquals(10, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(11, summary.getVariants());
		assertEquals(5, summary.getVariantsUsed());
		assertEquals(0, summary.getSwitched());
		assertEquals(0, summary.getR2Filtered());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, task.getRiskScores().length);

	}*/

	@Test(expected = Exception.class)
	public void testWrongChromosome() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.wrong_chr.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.run(new TaskMonitorMock());

	}

	/*@Test(expected = Exception.class)
	public void testDifferentSamples() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilenames("test-data/test.chr1.vcf", "test-data/test.chr2.wrong.vcf");
		task.setRiskScoreFilenames("test-data/test.scores.csv");
		task.setMinR2(1f);
		task.run(new TaskMonitorMock());

	}*/

	@Test
	public void testWithChunk() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		Chunk chunk = new Chunk();
		chunk.setStart(61795);
		chunk.setEnd(63231);
		task.setChunk(chunk);
		task.run(new TaskMonitorMock());

		//assertEquals(63480, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(2, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(2, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

}
