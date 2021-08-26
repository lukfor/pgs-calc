package genepi.riskscore;

import java.io.IOException;

import genepi.io.text.LineWriter;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.formats.PGSCatalogFormat;

public class DownloadPGSCatalog {

	public static void main(String[] args) throws IOException {
		int max = 320;
		String formatId = "PGS%06d";

		LineWriter writer = new LineWriter("pgs-catalog.txt");
		writer.write("PGSID");

		int count = 0;
		for (int i = 320; i <= max; i++) {
			String id = String.format(formatId, i);
			try {
				String filename = PGSCatalog.getFilenameById(id);
				new RiskScoreFile(filename, new PGSCatalogFormat());
				System.out.println(id);
				writer.write(id);
				count++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writer.close();
		System.out.println("Done. Downloaded " + count + " scores.");
	}

}
