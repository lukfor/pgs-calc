package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class RiskScoreFile {

	private String filename;

	private Map<Integer, Float> weights;

	public static final char SEPARATOR = ',';

	public static final String CHROMOSOME = "chr";

	public static final String POSITION = "position";

	public static final String WEIGHT = "weight";

	public RiskScoreFile(String filename) throws Exception {

		this.filename = filename;

		weights = new HashMap<>();

		if (!new File(filename).exists()) {
			throw new Exception("File '" + filename + "' not found.");
		}

		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		checkFileFormat(reader, filename);
		reader.close();
	}

	private void checkFileFormat(ITableReader reader, String filename) throws Exception {
		if (!reader.hasColumn(CHROMOSOME)) {
			throw new Exception("Column '" + CHROMOSOME + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(POSITION)) {
			throw new Exception("Column '" + POSITION + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(WEIGHT)) {
			throw new Exception("Column '" + WEIGHT + "' not found in '" + filename + "'");
		}

	}

	public void buildIndex(String chromosome) throws IOException {
		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		while (reader.next()) {
			String chromsomeVariant = reader.getString(CHROMOSOME);
			if (chromsomeVariant.equals(chromosome)) {
				int position = reader.getInteger(POSITION);
				float weight = new Float(reader.getDouble(WEIGHT));
				weights.put(position, weight);
			}
		}
		reader.close();
	}

	public boolean contains(int position) {
		return weights.containsKey(position);
	}

	public float getWeight(int position) {
		return weights.get(position);
	}

	public int getCountVariants() {
		return weights.size();
	}

}
