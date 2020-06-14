package genepi.riskscore.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import genepi.io.FileUtil;

public class MergeScoreTaskTest {

	@Test
	public void testMerge() throws Exception {
		
		MergeScoreTask task = new MergeScoreTask();
		task.setInputs("test-data/scores.chunk1.txt", "test-data/scores.chunk2.txt");
		task.setOutput("merged.task.txt");
		task.run();
		
		assertEquals(FileUtil.readFileAsString("test-data/merged.expected.txt"),
				FileUtil.readFileAsString("merged.task.txt"));
	}
	
}
