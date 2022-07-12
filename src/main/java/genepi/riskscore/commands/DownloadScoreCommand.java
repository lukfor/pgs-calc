package genepi.riskscore.commands;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "download", version = App.VERSION)
public class DownloadScoreCommand implements Callable<Integer> {

	@Parameters(description = "PGS IDs")
	List<String> scores;

	@Option(names = { "--out" }, description = "Output filename or folder when mutiple IDs", required = false)
	String out = null;

	public Integer call() throws Exception {

		for (String score : scores) {

			String url = PGSCatalog.getUrl(score);

			String filename = "";
			if (out == null) {
				filename = score + ".txt.gz";
			} else {
				if (scores.size() == 1) {
					filename = out;
				} else {
					FileUtil.createDirectory(out);
					filename = FileUtil.path(out, score + ".txt.gz");
				}
			}

			System.out.println("Downloading score " + score + " from " + url + "...");
			
			InputStream in = new URL(url).openStream();
			Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);

			System.out.println("Downloaded score from PGS-Catalog to file '" + filename + "'.");
		}

		return 0;
	}

}
