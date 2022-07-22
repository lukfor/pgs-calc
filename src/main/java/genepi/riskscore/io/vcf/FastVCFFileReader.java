package genepi.riskscore.io.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import genepi.io.FileUtil;
import genepi.io.reader.IReader;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

public class FastVCFFileReader implements IReader<MinimalVariantContext> {

	private List<String> samples;

	private int snpsCount = 0;

	private int samplesCount = 0;

	private MinimalVariantContext variantContext;

	private VCFLineParser parser;

	private String filename;

	protected BufferedReader in;

	private int lineNumber = 0;

	public FastVCFFileReader(InputStream inputStream, String vcfFilename) throws IOException {

		// load header
		VCFFileReader reader = new VCFFileReader(new File(vcfFilename), false);
		VCFHeader header = reader.getFileHeader();
		samples = header.getGenotypeSamples();
		samplesCount = samples.size();
		variantContext = new MinimalVariantContext(samplesCount);
		reader.close();

		filename = vcfFilename;
		InputStream in2 = FileUtil.decompressStream(inputStream);
		in = new BufferedReader(new InputStreamReader(in2));

		parser = new VCFLineParser(samplesCount);

	}

	public FastVCFFileReader(String vcfFilename) throws IOException {
		this(new FileInputStream(vcfFilename), vcfFilename);
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

	public boolean next() throws IOException {
		String line;
		while ((line = in.readLine()) != null) {
			try {
				lineNumber++;
				if (!line.trim().isEmpty() && line.charAt(0) != '#') {
					parseLine(line);
					return true;
				}
			} catch (Exception e) {
				throw new IOException(filename + ": Line " + lineNumber + ": " + e.getMessage());
			}
		}
		return false;
	}

	protected void parseLine(String line) throws IOException {

		variantContext = parser.parseLine(line);

		if (variantContext.getNSamples() != samplesCount) {
			throw new IOException("Line " + lineNumber + ": different number of samples.");
		}

		snpsCount++;

	}

	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
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

	}

}
