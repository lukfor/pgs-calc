package genepi.riskscore.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.List;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.reader.ITableReader;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.tasks.ConvertRsIdsTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;

public class PGSCatalog {

	public static String USER_HOME = System.getProperty("user.home");

	public static boolean ENABLE_CACHE = true;

	public static String CACHE_DIR = FileUtil.path(USER_HOME, ".pgs-calc", "pgs-catalog");

	public static String FILE_URL = "http://ftp.ebi.ac.uk/pub/databases/spot/pgs/scores/{0}/ScoringFiles/{0}.txt.gz";

	public static String getFilenameById(String id, String dbsnp) throws IOException {

		String filename = FileUtil.path(CACHE_DIR, id + ".txt.gz");

		if ((new File(filename)).exists()) {
			// System.out.println("Score '" + id + "' found in local cache " + filename);
			if (ENABLE_CACHE) {
				return filename;
			} else
				new File(filename).delete();
		}

		FileUtil.createDirectory(CACHE_DIR);

		MessageFormat format = new MessageFormat(FILE_URL);
		String url = format.format(new Object[] { id });

		// System.out.println("Downloading score '" + id + "' from " + url + "...");

		InputStream in = new URL(url).openStream();
		Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);

		PGSCatalogFileFormat fileFormat = getFileFormat(filename);
		if (fileFormat == PGSCatalogFileFormat.RS_ID) {
			if (dbsnp == null) {
				throw new IOException("Score " + id + " is in RS_ID format. Please specify dbsnp index.");
			}
			String originalFilename = filename.replaceAll(".txt.gz", ".original.txt.gz");
			new File(filename).renameTo(new File(originalFilename));
			ConvertRsIdsTask convertRsIds = new ConvertRsIdsTask(originalFilename, filename, dbsnp);
			TaskService.setAnsiSupport(false);
			TaskService.setAnimated(false);
			List<Task> result = TaskService.run(convertRsIds);
			if (!result.get(0).getStatus().isSuccess()) {
				throw new IOException(result.get(0).getStatus().getThrowable());
			}
			// TODO: delete original score file
		}

		return filename;

	}

	public static boolean isValidId(String id) {
		return (id.startsWith("PGS") && id.length() == 9 && !id.endsWith(".txt.gz"));
	}

	public static PGSCatalogFileFormat getFileFormat(String filename) throws IOException {

		PGSCatalogFormat format = new PGSCatalogFormat();

		DataInputStream in = openTxtOrGzipStream(filename);
		ITableReader reader = new CsvTableReader(in, RiskScoreFormat.SEPARATOR);
		reader.close();

		if (!reader.hasColumn("rsID")) {

			if (!reader.hasColumn(format.getChromosome())) {
				return PGSCatalogFileFormat.UNKNOWN;
			}
			if (!reader.hasColumn(format.getPosition())) {
				return PGSCatalogFileFormat.UNKNOWN;
			}

			if (!reader.hasColumn(format.getOtherAllele())) {
				return PGSCatalogFileFormat.UNKNOWN;
			}
			return PGSCatalogFileFormat.COORDINATES;
		}
		if (!reader.hasColumn(format.getEffectWeight())) {
			return PGSCatalogFileFormat.UNKNOWN;
		}
		if (!reader.hasColumn(format.getEffectAllele())) {
			return PGSCatalogFileFormat.UNKNOWN;
		}

		if (reader.hasColumn(format.getChromosome()) && reader.hasColumn(format.getPosition())) {
			return PGSCatalogFileFormat.COORDINATES;
		}

		return PGSCatalogFileFormat.RS_ID;

	}

	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}

}
