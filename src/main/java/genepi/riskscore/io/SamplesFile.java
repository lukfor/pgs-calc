package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class SamplesFile {

	private String filename;

	private Set<String> samples;

	private int totalSamples = 0;

	public static final char SEPARATOR = '\t';

	public SamplesFile(String filename) throws Exception {

		this.filename = filename;
		samples = new HashSet<String>();

		if (!new File(filename).exists()) {
			throw new Exception("File '" + filename + "' not found.");
		}

		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		checkFileFormat(reader, filename);
		reader.close();
	}

	private void checkFileFormat(ITableReader reader, String filename) throws Exception {
		if (!reader.hasColumn(OutputFileWriter.COLUMN_SAMPLE)) {
			throw new Exception("Column '" + OutputFileWriter.COLUMN_SAMPLE + "' not found in '" + filename + "'");
		}
	}

	public void buildIndex() throws IOException {
		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		while (reader.next()) {
			String sample = reader.getString(OutputFileWriter.COLUMN_SAMPLE);
			samples.add(sample);
			totalSamples++;
		}
		reader.close();
	}

	public boolean contains(String sample) {
		return samples.contains(sample);
	}

	public int getTotalSamples() {
		return totalSamples;
	}

}
