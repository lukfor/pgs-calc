package genepi.riskscore.io.vcf;

import genepi.io.FileUtil;
import genepi.io.reader.IReader;
import htsjdk.tribble.readers.TabixReader;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.*;
import java.util.*;

public class FastRegionVCFFileReader implements IVCFFileReader {

	private List<String> samples;
	private int snpsCount = 0;
	private int samplesCount = 0;
	private MinimalVariantContext variantContext;
	private VCFLineParser parser;
	private String filename;
	private TabixReader tabixReader;
	private TabixReader.Iterator tabixIterator;
	private List<Integer> variants;
	private int currentVariantIndex = 0;
	private String contig;

	public FastRegionVCFFileReader(String vcfFilename, String contig, List<Integer> variants) throws IOException {

		// load header
		VCFFileReader reader = new VCFFileReader(new File(vcfFilename), false);
		VCFHeader header = reader.getFileHeader();
		samples = header.getGenotypeSamples();
		samplesCount = samples.size();
		variantContext = new MinimalVariantContext(samplesCount);
		reader.close();

		this.filename = vcfFilename;
		this.variants = variants;
		this.contig = contig;
		this.parser = new VCFLineParser(samplesCount);

		// initialize TabixReader
		this.tabixReader = new TabixReader(vcfFilename);
	}

	public List<String> getGenotypedSamples() {
		return samples;
	}

	public MinimalVariantContext get() {
		return variantContext;
	}

	public int getSnpsCount() {
		return snpsCount;
	}

	public int getSamplesCount() {
		return samplesCount;
	}

	@Override
	public boolean next() throws IOException {

		while (currentVariantIndex < variants.size()) {
			int position = variants.get(currentVariantIndex);

			String region = contig + ":" + position + "-" + position;
			tabixIterator = tabixReader.query(region);

			String line;
			boolean found = false;

			while (tabixIterator != null && (line = tabixIterator.next()) != null) {
				if (line.charAt(0) == '#') continue; // skip header lines

				variantContext = parser.parseLine(line);
				snpsCount++;
				found = true;
				currentVariantIndex++;
				return true;
			}

			// if not found, skip to next requested position
			currentVariantIndex++;
			if (!found) {
				continue;
			}
		}

		return false;
	}

	@Override
	public void close() {
		if (tabixReader != null) {
			tabixReader.close();
		}
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public Iterator<MinimalVariantContext> iterator() {
		return null;
	}

	@Override
	public void reset() {
		currentVariantIndex = 0;
	}
}
