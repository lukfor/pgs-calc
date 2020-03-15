package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.model.ReferenceVariant;

public class RiskScoreFile {

	private String filename;

	private Map<Integer, ReferenceVariant> variants;

	private int totalVariants = 0;
	
	public static final char SEPARATOR = '\t';

	public static final String CHROMOSOME = "chr";

	public static final String POSITION = "position_hg19";

	public static final String EFFECT_WEIGHT = "effect_weight";

	public static final String ALLELE_A = "A1";

	public static final String ALLELE_B = "A2";

	public static final String EFFECT_ALLELE = "effect_allele";

	public RiskScoreFile(String filename) throws Exception {

		this.filename = filename;

		variants = new HashMap<Integer, ReferenceVariant>();

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
		if (!reader.hasColumn(EFFECT_WEIGHT)) {
			throw new Exception("Column '" + EFFECT_WEIGHT + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(ALLELE_A)) {
			throw new Exception("Column '" + ALLELE_A + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(ALLELE_B)) {
			throw new Exception("Column '" + ALLELE_B + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(EFFECT_ALLELE)) {
			throw new Exception("Column '" + EFFECT_ALLELE + "' not found in '" + filename + "'");
		}

	}

	public void buildIndex(String chromosome) throws IOException {
		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		while (reader.next()) {
			String chromsomeVariant = reader.getString(CHROMOSOME);
			if (chromsomeVariant.equals(chromosome)) {
				int position = reader.getInteger(POSITION);
				float effectWeight = new Float(reader.getDouble(EFFECT_WEIGHT));
				char alleleA = reader.getString(ALLELE_A).charAt(0);
				char alleleB = reader.getString(ALLELE_B).charAt(0);
				char effectAllele = reader.getString(EFFECT_ALLELE).charAt(0);

				ReferenceVariant variant = new ReferenceVariant(alleleA, alleleB, effectAllele, effectWeight);
				variants.put(position, variant);
			}
			totalVariants++;
		}
		reader.close();
	}

	public boolean contains(int position) {
		return variants.containsKey(position);
	}

	public ReferenceVariant getVariant(int position) {
		return variants.get(position);
	}

	public int getCacheSize() {
		return variants.size();
	}
	
	public int getTotalVariants() {
		return totalVariants;
	}

}
