package genepi.riskscore.io;

import java.io.File;
import java.util.List;
import java.util.Vector;

import genepi.io.text.LineReader;

public class PGSCatalogIDFile {

	private String filename;

	public static final String COLUMN_ID = "PGSID";

	public PGSCatalogIDFile(String filename) {

		this.filename = filename;

	}

	public String[] getIds() throws Exception {

		if (!new File(filename).exists()) {
			throw new Exception("File '" + filename + "' not found.");
		}

		LineReader reader = new LineReader(filename);
		if (!reader.next()) {
			throw new Exception("File '" + filename + "' is empty.");
		}

		String header = reader.get();
		if (!header.equalsIgnoreCase(COLUMN_ID)) {
			throw new Exception("Column '" + COLUMN_ID + "' not found in '" + filename + "'");
		}

		List<String> ids = new Vector<String>();
		while (reader.next()) {
			String id = reader.get();
			if (!id.trim().isEmpty() && !ids.contains(id)) {
				ids.add(id);
			}
		}
		reader.close();

		String[] result = new String[ids.size()];
		return ids.toArray(result);
	}

}
