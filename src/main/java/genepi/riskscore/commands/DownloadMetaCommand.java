package genepi.riskscore.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.meta.PGSCatalogCategoryFile;
import genepi.riskscore.io.meta.PGSCatalogMetaFile;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "download-meta", version = App.VERSION)
public class DownloadMetaCommand implements Callable<Integer> {

	@Option(names = { "--url-scores" }, description = "PGS-Catalog Url", required = false)
	String urlScores = "https://www.pgscatalog.org/rest/score/all?format=json&limit=250";

	@Option(names = { "--url-trait-catgefory" }, description = "PGS-Catalog Url for category", required = false)
	String urlTraits = "https://www.pgscatalog.org/rest/trait_category/all?format=json&limit=250";

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	public Integer call() throws Exception {

		String traitsFilename = "pgs_catalog_categories.json";
		download(urlTraits, traitsFilename);
		PGSCatalogCategoryFile categoryFile =  PGSCatalogCategoryFile.load(traitsFilename);
		//categoryFile.getCategories();

		String next = urlScores;

		String chunkFilename = "pgs_catalog_chunk.json";

		PGSCatalogMetaFile file = null;
		while (next != null) {

			System.out.println("Download " + next);
			download(next, chunkFilename);

			PGSCatalogMetaFile file1 = PGSCatalogMetaFile.load(chunkFilename, categoryFile);
			if (file == null) {
				file = file1;
			} else {
				file.merge(file1);
			}
			next = file1.getNext();
		}
		file.save(out);

		FileUtil.deleteFile(chunkFilename);

		System.out.println("Downloaded meta data from PGS-Catalog to file '" + out + "'.");

		return 0;
	}

	private void download(String url, String filename) throws IOException {
		InputStream in2 = new URL(url).openStream();
		Files.copy(in2, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
	}

}
