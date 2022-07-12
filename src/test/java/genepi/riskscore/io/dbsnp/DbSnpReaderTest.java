package genepi.riskscore.io.dbsnp;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class DbSnpReaderTest {

	public static String DBSNP_INDEX = "test-data/dbsnp-index.small.txt.gz";

	@BeforeClass
	public static void setup() {

	}

	@Test
	public void testGetContig() throws Exception {
		assertEquals("rs1000", DbSnpReader.getContig("rs1000140000", 3));
		assertEquals("rs100", DbSnpReader.getContig("rs1001300000", 3));
		assertEquals("rs00", DbSnpReader.getContig("rs001200000", 3));
		assertEquals("rs00", DbSnpReader.getContig("rs00120", 3));
		assertEquals("rs", DbSnpReader.getContig("rs10120", 3));
	}

	@Test
	public void testGetPosition() throws Exception {
		assertEquals(140000, DbSnpReader.getPosition("rs1000140000", 3));
		assertEquals(1300000, DbSnpReader.getPosition("rs1001300000", 3));
		assertEquals(1200000, DbSnpReader.getPosition("rs001200000", 3));
		assertEquals(120, DbSnpReader.getPosition("rs00120", 3));
		assertEquals(10120, DbSnpReader.getPosition("rs10120", 3));
	}

}
