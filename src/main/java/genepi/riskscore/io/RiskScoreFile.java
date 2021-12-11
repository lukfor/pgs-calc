package genepi.riskscore.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.io.formats.RiskScoreFormatFactory;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import genepi.riskscore.model.ReferenceVariant;

public class RiskScoreFile {

	private String filename;

	private Map<Integer, ReferenceVariant> variants;

	private int totalVariants = 0;

	private RiskScoreFormatImpl format;

	public RiskScoreFile(String filename, String dbsnp) throws Exception {
		this(filename, RiskScoreFormat.PGS_CATALOG, dbsnp);
	}

	public RiskScoreFile(String filename, RiskScoreFormat format, String dbsnp) throws Exception {

		this.filename = filename;

		variants = new HashMap<Integer, ReferenceVariant>();

		if (!new File(filename).exists()) {

			// check if filename is a PGS id
			if (PGSCatalog.isValidId(filename)) {
				String id = filename;
				this.filename = PGSCatalog.getFilenameById(id, dbsnp);
			} else {

				throw new Exception("File '" + filename + "' not found.");

			}
		}

		this.format = RiskScoreFormatFactory.buildFormat(this.filename, format);

		
		DataInputStream in = openTxtOrGzipStream(this.filename);
		ITableReader reader = new CsvTableReader(in, RiskScoreFormatImpl.SEPARATOR);
		checkFileFormat(reader, this.filename);
		reader.close();

	}

	private void checkFileFormat(ITableReader reader, String filename) throws Exception {
		if (!reader.hasColumn(format.getChromosome())) {
			throw new Exception("Column '" + format.getChromosome() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getPosition())) {
			throw new Exception("Column '" + format.getPosition() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getEffectWeight())) {
			throw new Exception("Column '" + format.getEffectWeight() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getOtherAllele())) {
			throw new Exception("Column '" + format.getOtherAllele() + "' not found in '" + filename + "'");
		}
		if (!reader.hasColumn(format.getEffectAllele())) {
			throw new Exception("Column '" + format.getEffectAllele() + "' not found in '" + filename + "'");
		}

	}

	public void buildIndex(String chromosome) throws IOException {
		buildIndex(chromosome, new Chunk());
	}

	public void buildIndex(String chromosome, Chunk chunk) throws IOException {

		assert (chromosome != null);

		try {
			DataInputStream in = openTxtOrGzipStream(filename);

			ITableReader reader = new CsvTableReader(in, RiskScoreFormatImpl.SEPARATOR);
			int row = 0;
			while (reader.next()) {
				row++;
				String chromsomeVariant = reader.getString(format.getChromosome());
				if (chromsomeVariant.equals(chromosome)) {
					if (reader.getString(format.getPosition()).isEmpty()) {
						throw new Exception("Row " + row + ": Position is empty");
					}

					int position = 0;
					try {
						position = reader.getInteger(format.getPosition());

					} catch (NumberFormatException e) {
						throw new Exception("Row " + row + ": '" + reader.getString(format.getPosition())
								+ "' is an invalid position");
					}

					if (position >= chunk.getStart() && position <= chunk.getEnd()) {

						float effectWeight = 0;
						try {

							effectWeight = ((Double) (reader.getDouble(format.getEffectWeight()))).floatValue();

						} catch (NumberFormatException e) {
							throw new Exception("Row " + row + ": '" + reader.getString(format.getEffectWeight())
									+ "' is an invalid weight");
						}

						String rawOtherA = reader.getString(format.getOtherAllele());
						if (rawOtherA.isEmpty()) {
							throw new Exception("Row " + row + ": Other allele is empty");
						}
						char alleleA = rawOtherA.charAt(0);

						String rawEffectAllele = reader.getString(format.getEffectAllele());
						if (rawEffectAllele.isEmpty()) {
							throw new Exception("Row " + row + ": Effect allele is empty");
						}
						char effectAllele = rawEffectAllele.charAt(0);

						ReferenceVariant variant = new ReferenceVariant(alleleA, effectAllele, effectWeight);
						variants.put(position, variant);

					}
				}
				totalVariants++;
			}
			reader.close();
		} catch (

		Exception e) {
			e.printStackTrace();
			throw new IOException(
					"Build Index for '" + filename + "' and chr '" + chromosome + "' failed: " + e.getMessage(), e);
		}
	}

	public boolean contains(int position) {
		return variants.containsKey(position);
	}

	public ReferenceVariant getVariant(int position) {
		return variants.get(position);
	}

	public static String getName(String filename) throws Exception {

		if (PGSCatalog.isValidId(filename)) {
			// use PGS ID as score name
			return filename;
		}

		// Cleanup filename and use it as name (remove extension etc..)
		String name = FileUtil.getFilename(filename);
		name = name.replaceAll(".txt.gz", "");
		name = name.replaceAll(".txt", "");
		name = name.replaceAll(".csv.gz", "");
		name = name.replaceAll(".csv", "");
		return name;
	}

	public int getCacheSize() {
		return variants.size();
	}

	public int getTotalVariants() {
		return totalVariants;
	}

	private DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}

	public String toString() {
		return format.toString();
	}

	public RiskScoreFormatImpl getFormat() {
		return format;
	}

}
