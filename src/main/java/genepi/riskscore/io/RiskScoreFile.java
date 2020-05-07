package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreFormat;

public class RiskScoreFile {

	private String filename;

	private Map<Integer, ReferenceVariant> variants;

	private int totalVariants = 0;
	
	public static final char SEPARATOR = '\t';

	private RiskScoreFormat format;
	
	public RiskScoreFile(String filename) throws Exception {
		this(filename, new RiskScoreFormat());
	}

	
	public RiskScoreFile(String filename, RiskScoreFormat format) throws Exception {

		this.filename = filename;
		this.format = format;

		variants = new HashMap<Integer, ReferenceVariant>();

		if (!new File(filename).exists()) {
			throw new Exception("File '" + filename + "' not found.");
		}

		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		checkFileFormat(reader, filename);
		reader.close();
	}

	private void checkFileFormat(ITableReader reader, String filename) throws Exception {
		if (!reader.hasColumn(format.getChromosome())) {
			throw new Exception("Column '" + format.getChromosome() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getPosition())) {
			throw new Exception("Column '" + format.getPosition() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getEffect_weight())) {
			throw new Exception("Column '" + format.getEffect_weight() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getAllele_a())) {
			throw new Exception("Column '" + format.getAllele_a() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getAllele_b())) {
			throw new Exception("Column '" + format.getAllele_b() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getEffect_allele())) {
			throw new Exception("Column '" + format.getEffect_allele() + "' not found in '" + filename + "'");
		}

	}

	public void buildIndex(String chromosome) throws IOException {
		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		while (reader.next()) {
			String chromsomeVariant = reader.getString(format.getChromosome());
			if (chromsomeVariant.equals(chromosome)) {
				int position = reader.getInteger(format.getPosition());
				float effectWeight = new Float(reader.getDouble(format.getEffect_weight()));
				char alleleA = reader.getString(format.getAllele_a()).charAt(0);
				char alleleB = reader.getString(format.getAllele_b()).charAt(0);
				char effectAllele = reader.getString(format.getEffect_allele()).charAt(0);

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
