package genepi.riskscore.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.model.RiskScoreFormat;

public class RiskScoreFileTest {

	@Test
	public void testLoadTextFile() throws Exception {
		RiskScoreFormat format = RiskScoreFormat.load("test-data/chr20.scores.csv.format");
		RiskScoreFile file = new RiskScoreFile("test-data/chr20.scores.csv", format);
		file.buildIndex("20");
		assertEquals(4, file.getTotalVariants());
		assertNotNull(file.getVariant(61795));
		assertNull(file.getVariant(55));
	}

	@Test
	public void testLoadGZFile() throws Exception {
		RiskScoreFormat format = RiskScoreFormat.load("test-data/chr20.scores.csv.format");
		RiskScoreFile file = new RiskScoreFile("test-data/chr20.scores.csv.gz", format);
		file.buildIndex("20");
		assertEquals(4, file.getTotalVariants());
		assertNotNull(file.getVariant(61795));
		assertNull(file.getVariant(55));
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("PGS000027", RiskScoreFile.getName("PGS000027"));
		assertEquals("PGS000027", RiskScoreFile.getName("PGS000027.txt.gz"));
		assertEquals("PGS000027", RiskScoreFile.getName("PGS000027.txt"));
		assertEquals("PGS000027", RiskScoreFile.getName("folder/path/PGS000027.txt.gz"));
		assertEquals("PGS000027", RiskScoreFile.getName("folder/path/PGS000027.txt"));
		assertEquals("filename", RiskScoreFile.getName("folder/path/filename.txt.gz"));
		assertEquals("filename", RiskScoreFile.getName("folder/path/filename.txt"));
		assertEquals("filename", RiskScoreFile.getName("folder/path/filename.csv"));
		assertEquals("filename.weights", RiskScoreFile.getName("folder/path/filename.weights"));
	}

	public void testLoadTextFileMissingAlleleInOtherChromosome() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/PGS000899.txt.gz", new PGSCatalogFormat());
		file.buildIndex("1");
		assertEquals(17, file.getCacheSize());
		assertEquals(176, file.getTotalVariants());
	}
	
	@Test(expected = IOException.class)
	public void testLoadTextFileMissingAllele() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/PGS000899.txt.gz", new PGSCatalogFormat());
		file.buildIndex("11");
	}
}
