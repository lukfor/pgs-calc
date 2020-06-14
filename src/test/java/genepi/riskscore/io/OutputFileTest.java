package genepi.riskscore.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import genepi.io.FileUtil;

public class OutputFileTest {

	@Test
	public void testLoadFromFile() throws Exception {
		OutputFile file = new OutputFile();
		file.load("test-data/scores.chunk1.txt");
		assertEquals(4, file.getSamples().size());
		assertEquals(3, file.getScores().size());
	}

	@Test
	public void testMerge() throws Exception {
		OutputFile file1 = new OutputFile();
		file1.load("test-data/scores.chunk1.txt");
		OutputFile file2 = new OutputFile();
		file2.load("test-data/scores.chunk2.txt");
		file1.merge(file2);
		assertEquals(4, file1.getSamples().size());
		assertEquals(3, file1.getScores().size());
		file1.save("merged.txt");
		assertEquals(FileUtil.readFileAsString("test-data/merged.expected.txt"),
				FileUtil.readFileAsString("merged.txt"));
	}

}
