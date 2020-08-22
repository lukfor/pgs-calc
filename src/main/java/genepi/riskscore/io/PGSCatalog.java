package genepi.riskscore.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;

import genepi.io.FileUtil;

public class PGSCatalog {

	public static String USER_HOME = System.getProperty("user.home");

	public static String CACHE_DIR = FileUtil.path(USER_HOME, ".pgs-calc", "pgs-catalog");

	public static String FILE_URL = "http://ftp.ebi.ac.uk/pub/databases/spot/pgs/scores/{0}/ScoringFiles/{0}.txt.gz";

	public static String getFilenameById(String id) throws IOException {

		String filename = FileUtil.path(CACHE_DIR, id + ".txt.gz");

		if ((new File(filename)).exists()) {
			//System.out.println("Score '" + id + "' found in local cache " + filename);
			return filename;
		}

		FileUtil.createDirectory(CACHE_DIR);

		MessageFormat format = new MessageFormat(FILE_URL);
		String url = format.format(new Object[] { id });

		//System.out.println("Downloading score '" + id + "' from " + url + "...");

		InputStream in = new URL(url).openStream();
		Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);

		return filename;

	}

	public static boolean isValidId(String id) {
		return (id.startsWith("PGS") && id.length() == 9 && !id.endsWith(".txt.gz"));
	}

}
