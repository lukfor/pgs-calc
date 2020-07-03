package genepi.riskscore.io.vcf;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import genepi.io.text.LineReader;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

public class FastVCFFileReader extends LineReader {

	private List<String> samples;

	private int snpsCount = 0;

	private int samplesCount = 0;

	private MinimalVariantContext variantContext;

	private List<String> header = new Vector<>();

	private VCFLineParser parser;

	public FastVCFFileReader(InputStream stream, String vcfFilename) throws IOException {

		super(new DataInputStream(stream));
		// load header
		VCFFileReader reader = new VCFFileReader(new File(vcfFilename), false);
		VCFHeader header = reader.getFileHeader();
		samples = header.getGenotypeSamples();
		samplesCount = samples.size();
		variantContext = new MinimalVariantContext(samplesCount);
		reader.close();

		parser = new VCFLineParser(samplesCount);

	}

	public List<String> getGenotypedSamples() {
		return samples;
	}

	public MinimalVariantContext getVariantContext() {
		return variantContext;
	}

	public int getSnpsCount() {
		return snpsCount;
	}

	public int getSamplesCount() {
		return samplesCount;
	}

	@Override
	protected void parseLine(String line) throws IOException {

		// not a header line
		if (line.charAt(0) != '#') {

			variantContext = parser.parseLine(line);

			if (variantContext.getNSamples() != samplesCount) {
				throw new IOException("Line " + getLineNumber() + ": different number of samples.");
			}

			snpsCount++;

		} else {
			header.add(line);
			next();
		}

	}

	public List<String> getFileHeader() {
		return header;
	}

}
