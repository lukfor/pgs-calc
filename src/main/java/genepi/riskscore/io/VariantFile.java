package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class VariantFile {

	private String filename;

	private Set<Integer> variants;

	private int totalVariants = 0;

	public static final char SEPARATOR = '\t';

	public static final String SCORE = "score";
	
	public static final String CHROMOSOME = "chr_name";

	public static final String POSITION = "chr_position";

	public static final String R2 = "r2";

	
	public VariantFile(String filename) throws Exception {

		this.filename = filename;
		variants = new HashSet<Integer>();

		if (!new File(filename).exists()) {
			throw new Exception("File '" + filename + "' not found.");
		}

		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		checkFileFormat(reader, filename);
		reader.close();
	}

	private void checkFileFormat(ITableReader reader, String filename) throws Exception {
		if (!reader.hasColumn(SCORE)) {
			throw new Exception("Column '" + SCORE + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(CHROMOSOME)) {
			throw new Exception("Column '" + CHROMOSOME + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(POSITION)) {
			throw new Exception("Column '" + POSITION + "' not found in '" + filename + "'");
		}		
	}

	public void buildIndex(String chromosome) throws IOException {
		ITableReader reader = new CsvTableReader(filename, SEPARATOR);
		while (reader.next()) {
			String chromsomeVariant = reader.getString(CHROMOSOME);
			if (chromsomeVariant.equals(chromosome)) {
				int position = reader.getInteger(POSITION);
				variants.add(position);
			}
			totalVariants++;
		}
		reader.close();
	}

	public boolean contains(int position) {
		return variants.contains(position);
	}

	public int getCacheSize() {
		return variants.size();
	}

	public int getTotalVariants() {
		return totalVariants;
	}

}
