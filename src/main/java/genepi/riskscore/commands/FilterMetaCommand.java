package genepi.riskscore.commands;

import genepi.io.text.LineWriter;
import genepi.riskscore.App;
import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.io.SamplesFile;
import genepi.riskscore.tasks.CreateHtmlReportTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "filter", version = App.VERSION)
public class FilterMetaCommand implements Callable<Integer> {

	@Option(names = { "--meta" }, description = "JSON file with meta data about scores", required = true)
	String meta = null;

	@Option(names = { "--category" }, description = "category", required = true, split = "")
	String category;

	@Option(names = { "--population" }, description = "population", required = false, split = "")
	String population;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	public Integer call() throws Exception {

		LineWriter writer = new LineWriter(out);

		int accepted= 0 ;
		MetaFile metaFile = MetaFile.load(meta);
		for (MetaFile.MetaScore score: metaFile.getAll()){
			if (accept(score, category, population)){
				writer.write(score.getId());
				accepted++;
			}
		}

		writer.close();

		System.out.println("Done. Wrote " + accepted + " scores to file '" + out + "'.");

		return 0;

	}

	private boolean accept(MetaFile.MetaScore score, String category, String population) {
		if (category != null) {
			if (!score.getCategories().contains(category)){
				return false;
			}
		}

		if (population != null){
			if ( score.getPopulations() == null){
				return false;
			}
			if (!score.getPopulations().supports(population)){
				return false;
			}
		}

		return true;
	}


}
