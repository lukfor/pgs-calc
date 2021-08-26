package genepi.riskscore.io.dbsnp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;

import genepi.io.FileUtil;

public class DbSnp {

	public static String USER_HOME = System.getProperty("user.home");

	public static String CACHE_DIR = FileUtil.path(USER_HOME, ".pgs-calc", "dbsnp");

	public static String FILE_URL = "https://imputationserver.sph.umich.edu/static/dbsnp/dbsnp{0}_{1}.txt.gz";

	public static String getFilename(String version, String build) throws IOException {

		String filename = FileUtil.path(CACHE_DIR, "dbsnp" + version + "_" + build + ".txt.gz");

		if ((new File(filename)).exists()) {
			return filename;
		}

		FileUtil.createDirectory(CACHE_DIR);

		MessageFormat format = new MessageFormat(FILE_URL);
		String url = format.format(new Object[] { version, build });

		InputStream in = new URL(url).openStream();
		Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);

		return filename;

	}

}
