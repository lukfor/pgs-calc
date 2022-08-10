package genepi.riskscore.io.proxy;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import genepi.riskscore.io.proxy.ProxyReader.ProxySnp;

public class ProxyReaderTest {

	public static String PROXY_INDEX = "test-data/lpa.snps.sorted.txt.gz";

	@BeforeClass
	public static void setup() {

	}

	@Test
	public void testGetProxiesForEntry() throws Exception {
		ProxyReader reader = new ProxyReader(PROXY_INDEX);
		ProxySnp[] proxies = reader.getByPosition("6", 160447669, "G", "A");
		assertEquals(3, proxies.length);
		reader.close();
	}

	@Test
	public void testGetProxiesForNonExistsingEntry() throws Exception {
		ProxyReader reader = new ProxyReader(PROXY_INDEX);
		ProxySnp[] proxies = reader.getByPosition("6", 123, "G", "A");
		assertEquals(0, proxies.length);
		reader.close();
	}

	@Test
	public void testGetProxiesForEntryWithWrongAlleles() throws Exception {
		ProxyReader reader = new ProxyReader(PROXY_INDEX);
		ProxySnp[] proxies = reader.getByPosition("6", 160447669, "G", "C");
		assertEquals(0, proxies.length);
		reader.close();
	}

	@Test
	public void testAlleleMapping() throws Exception {
		ProxyReader reader = new ProxyReader(PROXY_INDEX);
		ProxySnp[] proxies = reader.getByPosition("6", 160447669, "G", "A");
		assertEquals("A", proxies[0].mapAllele("G"));
		assertEquals("T", proxies[0].mapAllele("A"));
		assertEquals("C", proxies[1].mapAllele("G"));
		assertEquals("G", proxies[1].mapAllele("A"));
		assertEquals("G", proxies[2].mapAllele("G"));
		assertEquals("A", proxies[2].mapAllele("A"));
		reader.close();
	}

	@Test(expected = IOException.class)
	public void testWrongAlleleMapping() throws IOException {
		ProxyReader reader = new ProxyReader(PROXY_INDEX);
		ProxySnp[] proxies = reader.getByPosition("6", 160447669, "G", "A");
		proxies[0].mapAllele("C");
		reader.close();
	}

}
