package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.OutputFile;
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

	@Before
	public void beforeTest() {
		System.out.println("Clean up output directory");
		FileUtil.deleteDirectory("test-data-output");
		FileUtil.createDirectory("test-data-output");
	}
	
	@Test
	public void testPerformance() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(63480, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
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
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(4, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(1, output.getCountScores());
		assertEquals(EXPECTED_SAMPLES, output.getCountSamples());
	}

	@Test
	public void testMultipleScores() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv", "test-data/chr20.scores.csv",
				"test-data/chr20.scores.csv");
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(63480, task.getCountVariants());

		assertEquals(3, task.getSummaries().length);

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

		OutputFile output = new OutputFile(task.getOutput());
		assertEquals(3, output.getCountScores());

	}

	@Test
	public void testScore() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());
		assertEquals(1, task.getCountSamples());

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(1, output.getCountScores());
		assertEquals("LF001", output.getSamples().get(0));
		assertEquals(-0.4, output.getValue(0, 0), 0.00001);

	}

	@Test
	public void testScoreSwitchEffectAllele() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(3, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(1, summary.getAlleleMissmatch());
		assertEquals(1, task.getCountSamples());

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(1, output.getCountScores());
		assertEquals("LF001", output.getSamples().get(0));
		assertEquals(-0.6, output.getValue(0, 0), 0.00001);

	}

	@Test
	public void testMinR2_06() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.setMinR2(0.6f);
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(1, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(3, summary.getVariantsNotUsed());
		assertEquals(3, summary.getR2Filtered());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(1, output.getCountScores());
		assertEquals("LF001", output.getSamples().get(0));
		assertEquals(-0.2, output.getValue(0, 0), 0.00001);

	}

	@Test
	public void testMinR2_05() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(0.5f);
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(5, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(2, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
		assertEquals(2, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(2, task.getCountSamples());
		assertEquals(2, summary.getR2Filtered());

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(1, output.getCountScores());
		assertEquals("LF001", output.getSamples().get(0));
		assertEquals(-0.3, output.getValue(0, 0), 0.00001);

	}

	@Test
	public void testMinR2_1() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/two.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.setOutput("test-data-output/output.txt");
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

		OutputFile output = new OutputFile(task.getOutput());

		assertEquals(2, output.getCountSamples());
		assertEquals(1, output.getCountScores());
		assertEquals("LF001", output.getSamples().get(0));
		assertEquals(0.0, output.getValue(0, 0), 0.00001);

	}

	@Test(expected = Exception.class)
	public void testWrongChromosome() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/single.wrong_chr.vcf");
		task.setRiskScoreFilenames("test-data/chr20.scores.2.csv");
		task.setMinR2(1f);
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

	}

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
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		// assertEquals(63480, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(2, summary.getVariantsUsed());
		assertEquals(1, summary.getSwitched());
		assertEquals(2, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

	@Test
	public void testWithEffectsFile() throws Exception {

		ApplyScoreTask task = new ApplyScoreTask();
		task.setDefaultRiskScoreFormat(new RiskScoreFormat());
		task.setVcfFilename("test-data/chr20.dose.vcf.gz");
		task.setRiskScoreFilenames("test-data/chr20.scores.csv");
		task.setOutputEffectsFilename("test-data-output/output.effects.txt");
		task.setOutput("test-data-output/output.txt");
		task.run(new TaskMonitorMock());

		assertEquals(63480, task.getCountVariants());

		RiskScoreSummary summary = task.getSummaries()[0];
		assertEquals(3, summary.getVariantsUsed());
		assertEquals(2, summary.getSwitched());
		assertEquals(1, summary.getVariantsNotUsed());
		assertEquals(0, summary.getMultiAllelic());
		assertEquals(0, summary.getAlleleMissmatch());
		assertEquals(EXPECTED_SAMPLES, task.getCountSamples());

	}

}
