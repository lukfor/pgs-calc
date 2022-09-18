package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.model.Population;
import genepi.riskscore.model.PopulationMap;
import genepi.riskscore.model.Sample;

public class SamplesFile {

	private String filename;

	private Map<String, Sample> samples;

	private int totalSamples = 0;

	public static final char SEPARATOR = '\t';

	public static final String COLUMN_POPULATION = "population";

	public SamplesFile(String filename) throws Exception {

		this.filename = filename;
		samples = new HashMap<String, Sample>();

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
			String population = Sample.UNKNOWN_POPULATION;
			if (reader.hasColumn(COLUMN_POPULATION)) {
				population = reader.getString(COLUMN_POPULATION);
			}
			samples.put(sample, new Sample(sample, population));
			totalSamples++;
		}
		reader.close();
	}

	public boolean contains(String sample) {
		return samples.containsKey(sample);
	}

	public int getTotalSamples() {
		return totalSamples;
	}

	public PopulationMap getPopulations() {
		PopulationMap populations = new PopulationMap();
		for (Sample sample : samples.values()) {
			populations.addSample(sample.getPopulation());
		}
		return populations;
	}

	public List<String> getSamples(Population population) {
		List<String> result = new Vector<String>();
		for (Sample sample : samples.values()) {
			if (sample.getPopulation().equals(population.getName())) {
				result.add(sample.getId());
			}
		}
		return result;
	}

}
