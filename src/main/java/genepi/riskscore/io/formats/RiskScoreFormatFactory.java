package genepi.riskscore.io.formats;

import java.io.IOException;

public class RiskScoreFormatFactory {

	public enum RiskScoreFormat {
		DEFAULT, PGS_CATALOG, MAPPING_FILE
	}

	public static RiskScoreFormatImpl buildFormat(String filename, RiskScoreFormat format) throws IOException {
		switch (format) {
		case PGS_CATALOG:
			return new PGSCatalogFormat(filename);
		case DEFAULT:
			return new RiskScoreFormatImpl();
		case MAPPING_FILE:
			return RiskScoreFormatImpl.load(filename + ".format");
		default:
			return new RiskScoreFormatImpl();
		}
	}

}
