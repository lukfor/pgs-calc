package genepi.riskscore.commands;

import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import picocli.CommandLine.Command;

@Command(name = "clear-cache", version = App.VERSION)
public class ClearCacheCommand implements Callable<Integer> {

	public Integer call() throws Exception {

		PGSCatalog.VERBOSE = true;
		PGSCatalog.clearCache();

		return 0;
	}

}
