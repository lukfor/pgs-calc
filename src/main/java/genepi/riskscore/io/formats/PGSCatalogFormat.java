package genepi.riskscore.io.formats;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class PGSCatalogFormat extends RiskScoreFormatImpl {

	public int VERSION_1 = 1;

	public int VERSION_2 = 2;

	private int version = VERSION_1;

	private PGSCatalogVariantsFormat format;

	public PGSCatalogFormat(String filename) throws IOException {
		this.format = detectVersionAndFormat(filename);
		// format unknown or rsIds --> let try version two --> maybe we find coordinates
		if (format == PGSCatalogVariantsFormat.UNKNOWN || format == PGSCatalogVariantsFormat.RS_ID) {
			this.version = VERSION_2;
			this.format = detectVersionAndFormat(filename);
		}
	}

	@Override
	public String getChromosome() {
		return "chr_name";
	}

	@Override
	public String getPosition() {
		return "chr_position";
	}

	@Override
	public String getEffectAllele() {
		return "effect_allele";
	}

	@Override
	public String getEffectWeight() {
		return "effect_weight";
	}

	@Override
	public String getOtherAllele() {
		if (version == VERSION_2) {
			return "other_allele";
		} else {
			return "reference_allele";
		}
	}

	protected PGSCatalogVariantsFormat detectVersionAndFormat(String filename) throws IOException {
		DataInputStream in = openTxtOrGzipStream(filename);
		ITableReader reader = new CsvTableReader(in, RiskScoreFormatImpl.SEPARATOR);
		reader.close();

		if (!reader.hasColumn("rsID")) {

			if (!reader.hasColumn(getChromosome())) {
				return PGSCatalogVariantsFormat.UNKNOWN;
			}
			if (!reader.hasColumn(getPosition())) {
				return PGSCatalogVariantsFormat.UNKNOWN;
			}

			if (!reader.hasColumn(getOtherAllele())) {
				return PGSCatalogVariantsFormat.UNKNOWN;
			}
			return PGSCatalogVariantsFormat.COORDINATES;
		}

		if (!reader.hasColumn(getEffectWeight())) {
			return PGSCatalogVariantsFormat.UNKNOWN;
		}
		if (!reader.hasColumn(getEffectAllele())) {
			return PGSCatalogVariantsFormat.UNKNOWN;
		}

		if (reader.hasColumn(getChromosome()) && reader.hasColumn(getPosition())
				&& reader.hasColumn(getOtherAllele())) {
			return PGSCatalogVariantsFormat.COORDINATES;
		}

		return PGSCatalogVariantsFormat.RS_ID;
	}

	public PGSCatalogVariantsFormat getFormat() {
		return format;
	}

	public String toString() {
		return "PGS-Catalog v" + version + " (" + format + ")";
	}

	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}
	
	@Override
	public boolean hasRsIds() {
		return format == PGSCatalogVariantsFormat.RS_ID;
	}
}
