package genepi.riskscore.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;

public class RiskScoreFileTest {

	public static String DBSNP_INDEX = "test-data/dbsnp-index.small.txt.gz";

	@BeforeClass
	public static void setup() {
		PGSCatalog.ENABLE_CACHE = false;
	}

	@Test
	public void testLoadTextFile() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/chr20.scores.csv", RiskScoreFormat.MAPPING_FILE, DBSNP_INDEX);
		file.buildIndex("20");
		assertEquals(4, file.getTotalVariants());
		assertNotNull(file.getVariant(61795));
		assertNull(file.getVariant(55));
	}

	@Test
	public void testLoadGZFile() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/chr20.scores.csv.gz", RiskScoreFormat.MAPPING_FILE,
				DBSNP_INDEX);
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

	@Test
	public void testLoadTextFileMissingAlleleInOtherChromosome() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/PGS000899.txt.gz", DBSNP_INDEX);
		file.buildIndex("1");
		assertEquals(17, file.getCacheSize());
		assertEquals(176, file.getTotalVariants());
	}

	@Test
	public void testLoadTextFileMissingAllele() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/PGS000899.txt.gz", DBSNP_INDEX);
		file.buildIndex("11");
		assertEquals(176, file.getTotalVariants());
		assertEquals(1, file.getIgnoredVariants());
	}

	@Test
	public void testLoadRsIdFormatFromPGSCatalog() throws Exception {
		RiskScoreFile file = new RiskScoreFile("PGS000001", DBSNP_INDEX);
		file.buildIndex("1");
		assertEquals(5, file.getCacheSize());
		assertEquals(77, file.getTotalVariants());
	}

	@Test
	public void testLoadRsIdFormatFromFile() throws Exception {
		RiskScoreFile file = new RiskScoreFile("test-data/PGS000001.txt.gz", DBSNP_INDEX);
		file.buildIndex("1");
		assertEquals(5, file.getCacheSize());
		assertEquals(77, file.getTotalVariants());
	}

	@Test
	public void testLoadRsIdFormatV1AndV2() throws Exception {

		for (int i = 1; i <= 22; i++) {
			RiskScoreFile file = new RiskScoreFile("test-data/PGS000957.txt.gz", DBSNP_INDEX);
			file.buildIndex(i + "");
		}

		RiskScoreFile file = new RiskScoreFile("test-data/PGS000957.txt.gz", DBSNP_INDEX);
		file.buildIndex("6");
		assertEquals(11276, file.getTotalVariants());
		assertEquals(1, file.getIgnoredVariants());

		for (int i = 1; i <= 22; i++) {
			RiskScoreFile file2 = new RiskScoreFile("test-data/PGS000958.txt.gz", DBSNP_INDEX);
			file2.buildIndex(i + "");
		}

		RiskScoreFile file2 = new RiskScoreFile("test-data/PGS000958.txt.gz", DBSNP_INDEX);
		file2.buildIndex("6");
		assertEquals(9400, file2.getTotalVariants());
		assertEquals(1, file2.getIgnoredVariants());

	}

}
