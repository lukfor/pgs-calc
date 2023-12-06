package genepi.riskscore.io.formats;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PGSCatalogHarmonizedFormat extends RiskScoreFormatImpl {

	@Override
	public String getChromosome() {
		return "hm_chr";
	}

	@Override
	public String getPosition() {
		return "hm_pos";
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
		return "other_allele";
	}

	@Override
	public boolean hasRsIds() {
		return false;
	}
	
	public String toString() {
		return "PGS Catalog Harmonized format";
	}

	protected static boolean acceptFile(String filename) throws IOException {
		DataInputStream in = openTxtOrGzipStream(filename);
		ITableReader reader = new CsvTableReader(in, '\t');
		reader.close();
		return reader.hasColumn("hm_chr");
	}

	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}

}
