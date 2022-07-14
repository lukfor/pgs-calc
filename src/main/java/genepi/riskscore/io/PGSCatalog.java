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

	public static boolean ENABLE_CACHE = true;

	public static String CACHE_DIR = FileUtil.path(USER_HOME, ".pgs-calc", "pgs-catalog");

	public static String FILE_URL = "http://ftp.ebi.ac.uk/pub/databases/spot/pgs/scores/{0}/ScoringFiles/{0}.txt.gz";

	public static boolean VERBOSE = false;

	public static String getFilenameById(String id) throws IOException {

		String filename = FileUtil.path(CACHE_DIR, id + ".txt.gz");

		if ((new File(filename)).exists()) {
			debug("Score '" + id + "' found in local cache " + filename);
			if (ENABLE_CACHE) {
				return filename;
			} else
				new File(filename).delete();
		}

		FileUtil.createDirectory(CACHE_DIR);

		String url = getUrl(id);

		debug("Downloading score '" + id + "' from " + url + "...");

		InputStream in = new URL(url).openStream();
		Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);

		return filename;

	}

	public static boolean isValidId(String id) {
		return (id.startsWith("PGS") && id.length() == 9 && !id.endsWith(".txt.gz"));
	}

	public static String getUrl(String id) {
		MessageFormat format = new MessageFormat(FILE_URL);
		return format.format(new Object[] { id });

	}

	public static void debug(String message) {
		if (VERBOSE) {
			System.out.println(message);
		}
	}

}
