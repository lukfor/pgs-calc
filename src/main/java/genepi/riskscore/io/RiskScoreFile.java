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
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreFormat;

public class RiskScoreFile {

	private String filename;

	private Map<Integer, ReferenceVariant> variants;

	private int totalVariants = 0;

	private RiskScoreFormat format;

	public RiskScoreFile(String filename) throws Exception {
		this(filename, new RiskScoreFormat());
	}

	public RiskScoreFile(String filename, RiskScoreFormat format) throws Exception {

		this.filename = filename;
		this.format = format;

		variants = new HashMap<Integer, ReferenceVariant>();

		if (!new File(filename).exists()) {

			// check if filename is a PGS id
			if (PGSCatalog.isValidId(filename)) {
				String id = filename;
				this.filename = PGSCatalog.getFilenameById(id);
			} else {

				throw new Exception("File '" + filename + "' not found.");

			}
		}

		DataInputStream in = openTxtOrGzipStream(this.filename);

		ITableReader reader = new CsvTableReader(in, RiskScoreFormat.SEPARATOR);
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
		buildIndex(chromosome, new Chunk());
	}

	public void buildIndex(String chromosome, Chunk chunk) throws IOException {

		assert (chromosome != null);

		try {
			DataInputStream in = openTxtOrGzipStream(filename);

			ITableReader reader = new CsvTableReader(in, RiskScoreFormat.SEPARATOR);
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

							effectWeight = ((Double) (reader.getDouble(format.getEffect_weight()))).floatValue();

						} catch (NumberFormatException e) {
							throw new Exception("Row " + row + ": '" + reader.getString(format.getEffect_weight())
									+ "' is an invalid weight");
						}

						String rawAlleleA = reader.getString(format.getAllele_a());
						if (rawAlleleA.isEmpty()) {
							throw new Exception("Row " + row + ": Allele A is empty");
						}
						char alleleA = rawAlleleA.charAt(0);

						String rawAlleleB = reader.getString(format.getAllele_b());
						if (rawAlleleB.isEmpty()) {
							throw new Exception("Row " + row + ": Allele B is empty");
						}
						char alleleB = rawAlleleB.charAt(0);

						String rawEffectAllele = reader.getString(format.getEffect_allele());
						if (rawEffectAllele.isEmpty()) {
							throw new Exception("Row " + row + ": Effect allele is empty");
						}
						char effectAllele = rawEffectAllele.charAt(0);

						ReferenceVariant variant = new ReferenceVariant(alleleA, alleleB, effectAllele, effectWeight);
						variants.put(position, variant);

					}
				}
				totalVariants++;
			}
			reader.close();
		} catch (

		Exception e) {
			e.printStackTrace();
			throw new IOException("Build Index for '" + filename + "' and chr '" + chromosome + "' failed: " + e.getMessage(), e);
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

}
