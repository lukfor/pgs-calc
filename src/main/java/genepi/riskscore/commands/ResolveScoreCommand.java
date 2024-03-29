package genepi.riskscore.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.io.text.GzipLineWriter;
import genepi.io.text.LineReader;
import genepi.riskscore.App;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.formats.RiskScoreFormatFactory;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import genepi.riskscore.tasks.ApplyScoreTask;
import genepi.riskscore.tasks.LiftOverScoreTask;
import genepi.riskscore.tasks.ResolveScoreTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command(name = "resolve", version = App.VERSION)
public class ResolveScoreCommand implements Callable<Integer> {

	@Option(names = "--in", description = "input score file", required = true)
	private String input;

	@Option(names = "--out", description = "output score file", required = true)
	private String output;

	@Option(names = "--dbsnp", description = "dbsnp index file", required = true)
	private String dbsnp;

	@Option(names = "--chain", description = "dbsnp index file", required = false)
	private String chain;

	@Option(names = {
			"--force-rsids" }, description = "Resolve rsIds also when chromosomal positions are available", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean forceRsIds = false;

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

		long start = System.currentTimeMillis();

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

			RiskScoreFormatImpl format = RiskScoreFormatFactory.buildFormat(input, RiskScoreFormat.AUTO_DETECT);
			System.out.println("Input File Format: " + format);

			System.out.println("--------------------------------------");

			if (format.hasRsIds() || forceRsIds) {

				System.out.println("Resolve rsIDs using index file '" + dbsnp + "'...");

				ResolveScoreTask task = new ResolveScoreTask(input, output + ".raw", dbsnp);
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
				System.out.println("  Other Allele: ");
				System.out.println("    Reference Allele dbSNP: " + task.getOtherAlleleReference());
				System.out.println("    Alternate Allele dbSNP: " + task.getOtherAlleleAlternate());
				System.out.println("    From Source File: " + task.getOtherAlleleSource());
				System.out.println("Ignored Variants: ");
				System.out.println("  Not found in dbSNP: " + task.getIgnoredNotInDbSnp());
				System.out.println("  Multiple Alternate Alleles: " + task.getIgnoredMulAlternateAlleles());

				compress(output + ".raw", output);

			} else {

				if (chain != null) {

					System.out.println("Liftover using chain file '" + chain + "'...");

					LiftOverScoreTask task = new LiftOverScoreTask(input, output + ".raw", chain);
					TaskService.setAnsiSupport(false);
					TaskService.setAnimated(false);
					List<Task> result = TaskService.run(task);
					if (!result.get(0).getStatus().isSuccess()) {
						result.get(0).getStatus().getThrowable().printStackTrace();
						System.out.println("*** ERROR ***  " + result.get(0).getStatus().getThrowable());
						return 1;
					}

					System.out.println("Number Variants Input: " + task.getTotal());
					System.out.println("Number Variants lifted: " + task.getResolved());
					System.out.println("Number Variants not lifted: " + task.getFailed());
					System.out.println("Ignored Variants (no position): " + task.getIgnored());

					compress(output + ".raw", output);

				} else {
					FileUtil.copy(input, output);
				}

			}

			System.out.println("--------------------------------------");

			// test output file format
			RiskScoreFile score = null;
			int loaded = 0;
			for (int i = 1; i <= 22; i++) {
				System.out.println("Validate chromosome " + i + "...");
				score = new RiskScoreFile(output, dbsnp, null);
				score.buildIndex(i + "");
				loaded += score.getLoadedVariants();
			}

			// chr X
			System.out.println("Validate chromosome X...");
			score = new RiskScoreFile(output, dbsnp, null);
			score.buildIndex("X");
			loaded += score.getLoadedVariants();

			long end = System.currentTimeMillis();

			System.out.println("--------------------------------------");
			System.out.println("Output File: " + output);
			System.out.println("Output File Format: " + score.getFormat());
			System.out.println("Number Variants Output: " + loaded + "/" + score.getTotalVariants());
			System.out.println("--------------------------------------");

			System.out.println("Execution Time: " + ((end - start) / 1000) + " sec");

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("*** ERROR ***  " + e);
			new File(output).delete();
			return 1;
		}

	}

	private void compress(String input, String output) throws IOException {
		LineReader reader = new LineReader(input);
		GzipLineWriter writer = new GzipLineWriter(output);
		while (reader.next()) {
			writer.write(reader.get());
		}
		writer.close();
		reader.close();

		FileUtil.deleteFile(input);
	}

}
