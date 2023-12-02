package genepi.riskscore.io.formats;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;

public class RiskScoreFormatFactory {

	public enum RiskScoreFormat {
		DEFAULT, AUTO_DETECT, MAPPING_FILE
	}

	public static RiskScoreFormatImpl buildFormat(String filename, RiskScoreFormat format) throws IOException {
		switch (format) {
		case AUTO_DETECT:

			if (PGSCatalogHarmonizedFormat.acceptFile(filename)){
				return new PGSCatalogHarmonizedFormat();
			}

			String headerLine = readHeader(filename);
			if (headerLine.startsWith("## PRSweb")) {
				return new PRSwebFormat();
			} else {
				// PGSCatalog as default
				return new PGSCatalogFormat(filename, false);
			}

		case DEFAULT:
			return new RiskScoreFormatImpl();
		case MAPPING_FILE:
			return RiskScoreFormatImpl.load(filename + ".format");
		default:
			return new RiskScoreFormatImpl();
		}
	}
	
	
	public static String readHeader(String filename) throws IOException {
		DataInputStream in = openTxtOrGzipStream(filename);
		String line = in.readLine();
		in.close();
		return line;
	}

	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}

}
