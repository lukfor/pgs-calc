package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.tasks.ApplyScoreTask;
import genepi.riskscore.tasks.LiftOverScoreTask;
import genepi.riskscore.tasks.ResolveScoreTask;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "validate", version = App.VERSION)
public class ValidateScoreCommand implements Callable<Integer> {

	@Parameters(description = "Score files")
	List<String> scores;

	@Option(names = { "--out" }, description = "Output filename", required = false)
	String out;

	@Option(names = {
			"--verbose" }, description = "Show debug messages", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean verbose = false;

	@Override
	public Integer call() throws Exception {

		if (verbose) {
			RiskScoreFile.VERBOSE = true;
			ResolveScoreTask.VERBOSE = true;
			ApplyScoreTask.VERBOSE = true;
			LiftOverScoreTask.VERBOSE = true;
			PGSCatalog.VERBOSE = true;
		}

		CsvTableWriter writer = null;
		if (out != null) {
			writer = new CsvTableWriter(out);
			writer.setColumns(new String[] { "score", "coverage", "variants", "original_variants", });
		}

		for (String input : scores) {

			try {
				// test file format
				RiskScoreFile score = null;
				int loaded = 0;
				for (int i = 1; i <= 22; i++) {
					System.out.println("Validate chromosome " + i + "...");
					score = new RiskScoreFile(input, null);
					score.buildIndex(i + "");
					loaded += score.getCacheSize();
				}

				// chr X
				System.out.println("Validate chromosome X...");
				score = new RiskScoreFile(input, null);
				score.buildIndex("X");
				loaded += score.getCacheSize();

				String name = RiskScoreFile.getName(input);
				System.out.println("--------------------------------------");
				System.out.println("Score File: " + input);
				System.out.println("Score Name: " + name);
				System.out.println("Output File Format: " + score.getFormat());
				System.out.println("Number Variants Output: " + loaded + "/" + score.getTotalVariants());
				System.out.println("--------------------------------------");

				if (writer != null) {
					writer.setString("score", name);
					writer.setDouble("coverage", (double) loaded / (double) score.getTotalVariants());
					writer.setInteger("variants", loaded);
					writer.setInteger("original_variants", score.getTotalVariants());
					writer.next();
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("*** ERROR ***  " + e);
				return 1;
			}
		}

		if (writer != null) {
			writer.close();
		}

		return 0;

	}

}
