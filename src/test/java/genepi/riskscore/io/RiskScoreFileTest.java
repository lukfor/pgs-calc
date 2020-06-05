package genepi.riskscore.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
	
	
}
