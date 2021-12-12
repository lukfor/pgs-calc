package genepi.riskscore.commands;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.formats.RiskScoreFormatFactory;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import genepi.riskscore.tasks.ResolveScoreTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "resolve", version = App.VERSION)
public class ResolveScoreCommand implements Callable<Integer> {

	@Option(names = "--in", description = "input score file", required = true)
	private String input;

	@Option(names = "--out", description = "output score file", required = true)
	private String output;

	@Option(names = "--dbsnp", description = "dbsnp index file", required = true)
	private String dbsnp;

	@Override
	public Integer call() throws Exception {

		System.out.println("Input File: " + input);
		try {

			if (!new File(input).exists()) {

				// check if filename is a PGS id
				if (PGSCatalog.isValidId(input)) {
					String id = input;
					input = PGSCatalog.getFilenameById(id);
				} else {
					System.out.println("*** ERROR *** File '" + input + "' not found.");
					return 1;
				}
			}

			RiskScoreFormatImpl format = RiskScoreFormatFactory.buildFormat(input, RiskScoreFormat.PGS_CATALOG);
			System.out.println("Input File Format: " + format);

			if (format.hasRsIds()) {

				ResolveScoreTask task = new ResolveScoreTask(input, output, dbsnp);
				TaskService.setAnsiSupport(false);
				TaskService.setAnimated(false);
				List<Task> result = TaskService.run(task);
				if (!result.get(0).getStatus().isSuccess()) {
					result.get(0).getStatus().getThrowable().printStackTrace();
					System.out.println("*** ERROR ***  " + result.get(0).getStatus().getThrowable());
					return 1;
				}

				System.out.println("Number Variants Input: " + task.getTotal());
				System.out.println("Number Variants Resolved: " + task.getResolved());

			} else {

				FileUtil.copy(input, output);

			}

			// test output file format
			RiskScoreFile score = null;
			for (int i = 1; i <= 22; i++) {
				score = new RiskScoreFile(output, dbsnp);
				score.buildIndex(i + "");
			}

			System.out.println("--------------------------------------");
			System.out.println("Output File: " + output);
			System.out.println("Output File Format: " + score.getFormat());
			System.out.println("Number Variants Output: " + score.getTotalVariants());

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("*** ERROR ***  " + e);
			new File(output).delete();
			return 1;
		}

	}

}
