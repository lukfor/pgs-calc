package genepi.riskscore;

import java.io.IOException;

import genepi.io.text.LineWriter;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.formats.PGSCatalogFormat;

public class DownloadPGSCatalog {

	public static void main(String[] args) throws IOException {
		int max = 200;
		String formatId = "PGS%06d";

		LineWriter writer = new LineWriter("pgs-catalog.txt");
		writer.write("PGSID");

		for (int i = 1; i <= max; i++) {
			String id = String.format(formatId, i);
			String filename = PGSCatalog.getFilenameById(id);
			try {
				RiskScoreFile file = new RiskScoreFile(filename, new PGSCatalogFormat());
				writer.write(id);
			} catch (Exception e) {
			}
		}
		writer.close();
	}

}
