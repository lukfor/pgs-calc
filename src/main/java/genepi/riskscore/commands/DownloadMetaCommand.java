package genepi.riskscore.commands;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.MetaFile;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "download-meta", version = App.VERSION)
public class DownloadMetaCommand implements Callable<Integer> {

	@Option(names = { "--url" }, description = "PGS-Catalog Url", required = false)
	String url = "https://www.pgscatalog.org/rest/score/all?format=json&limit=250";

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	public Integer call() throws Exception {

		String next = url;

		String chunkFilename = "pgs_catalog_chunk.json";

		MetaFile file = null;
		while (next != null) {

			System.out.println("Download " + next);
			InputStream in = new URL(next).openStream();
			Files.copy(in, Paths.get(chunkFilename), StandardCopyOption.REPLACE_EXISTING);

			MetaFile file1 = MetaFile.load(chunkFilename);
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

}
