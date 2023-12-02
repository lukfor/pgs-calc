package genepi.riskscore.io;

import java.io.File;
import java.util.List;
import java.util.Vector;

import genepi.io.FileUtil;
import genepi.io.text.LineReader;

public class ScoresFile {

	private String filename;

	public static final String COLUMN_ID = "SCORES";

	public ScoresFile(String filename) {

		this.filename = filename;

	}

	public String[] getFilenames() throws Exception {

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

		String path = new File(filename).getAbsoluteFile().getParent();

		List<String> ids = new Vector<String>();
		while (reader.next()) {
			String id = reader.get();
			if (!id.trim().isEmpty() && !ids.contains(id)) {
				if (id.startsWith("/")){
					ids.add(id);
				}else {
					ids.add(FileUtil.path(path, id));
				}
			}
		}
		reader.close();

		String[] result = new String[ids.size()];
		return ids.toArray(result);
	}

}
