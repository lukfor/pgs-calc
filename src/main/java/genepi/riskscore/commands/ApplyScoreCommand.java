package genepi.riskscore.commands;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.PGSCatalog;
import genepi.riskscore.io.PGSCatalogIDFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.ScoresFile;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.tasks.ApplyScoreTask;
import genepi.riskscore.tasks.CreateHtmlReportTask;
import genepi.riskscore.tasks.LiftOverScoreTask;
import genepi.riskscore.tasks.MergeEffectsTask;
import genepi.riskscore.tasks.MergeReportTask;
import genepi.riskscore.tasks.MergeScoreTask;
import genepi.riskscore.tasks.MergeVariantsTask;
import genepi.riskscore.tasks.ResolveScoreTask;
import htsjdk.samtools.util.StopWatch;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "apply", version = App.VERSION)
public class ApplyScoreCommand implements Callable<Integer> {

	@Parameters(description = "VCF files")
	List<String> vcfs;

	@Option(names = { "--ref" }, description = "Reference weights", required = true)
	String ref;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	@Option(names = { "--minR2",
			"--min-r2" }, description = "Minimal imputation quality", required = false, showDefaultValue = Visibility.ALWAYS)
	float minR2 = 0;

	@Option(names = {
			"--genotypes" }, description = "Genotype field (DS or GT)", required = false, showDefaultValue = Visibility.ALWAYS)
	String genotypeFormat = ApplyScoreTask.DOSAGE_FORMAT;

	@Option(names = { "--threads" }, description = "Number of threads", required = false)
	int threads = 1;

	@Option(names = { "--writeVariants",
			"--write-variants" }, description = "Write csv file with all used variants", required = false)
	String outputVariantFilename = null;

	@Option(names = { "--includeVariants",
			"--include-variants" }, description = "Include only variants from this file", required = false)
	String includeVariantFilename = null;

	@Option(names = { "--samples" }, description = "Include only samples from this file", required = false)
	String includeSamplesFilename = null;

	@Option(names = { "--report-json", "--info" }, description = "Write statistics to json file", required = false)
	String reportJson = null;

	@Option(names = { "--report-csv", }, description = "Write statistics to csv file", required = false)
	String reportCsv = null;

	@Option(names = { "--report-html" }, description = "Write statistics to html file", required = false)
	String reportHtml = null;

	@Option(names = { "--meta" }, description = "JSON file with meta data about scores", required = false)
	String meta = null;

	@Option(names = { "--writeEffects",
			"--write-effects" }, description = "Write file with effects per snp and sample", required = false)
	String outputEffectsFilename = null;

	@Option(names = { "--dbsnp" }, description = "dbSNP Index file to support rsIDs", required = false)
	String dbsnp = null;

	@Option(names = { "--help" }, usageHelp = true)
	boolean showHelp;

	@Option(names = {
			"--no-ansi" }, description = "Disable ANSI output", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean noAnsi = false;

	@Option(names = {
			"--verbose" }, description = "Show debug messages", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean verbose = false;

	@Option(names = { "--version" }, versionHelp = true)
	boolean showVersion;

	@ArgGroup(exclusive = false, multiplicity = "0..1")
	Chunk chunk;

	public Integer call() throws Exception {

		if (verbose) {
			RiskScoreFile.VERBOSE = true;
			ResolveScoreTask.VERBOSE = true;
			ApplyScoreTask.VERBOSE = true;
			LiftOverScoreTask.VERBOSE = true;
			PGSCatalog.VERBOSE = true;
			noAnsi = true;
		}

		if (noAnsi) {
			TaskService.setAnimated(false);
			TaskService.setAnsiColors(false);
		}

		if (vcfs == null || vcfs.isEmpty()) {
			System.out.println();
			System.out.println("Please provide at least one VCF file.");
			System.out.println();
			return 1;
		}

		System.out.println();
		System.out.println("Input:");
		System.out.println("  ref: " + ref);
		System.out.println("  out: " + out);
		System.out.println("  genotypes: " + genotypeFormat);
		System.out.println("  minR2: " + minR2);
		if (chunk != null) {
			System.out.println("  Chunk: " + chunk.getStart() + " - " + chunk.getEnd());
		}
		System.out.println("  vcfs (" + vcfs.size() + "):");
		for (String vcf : vcfs) {
			System.out.println("   - " + vcf);
		}
		System.out.println();

		StopWatch watch = new StopWatch();
		watch.start();

		String outParent = new File(out).getParent();
		String tempFolder = FileUtil.path(outParent, "temp");
		File tempFolderFile = new File(tempFolder);
		tempFolderFile.mkdirs();

		List<ApplyScoreTask> tasks = new Vector<ApplyScoreTask>();
		for (String vcf : vcfs) {

			ApplyScoreTask task = new ApplyScoreTask();

			String[] refs = parseRef(ref);
			task.setRiskScoreFilenames(refs);

			for (String file : refs) {
				String autoFormat = file + ".format";
				if (new File(autoFormat).exists()) {
					task.setRiskScoreFormat(file, RiskScoreFormat.MAPPING_FILE);
				}
			}

			if (chunk != null) {
				task.setChunk(chunk);
			}

			String taskPrefix = FileUtil.path(tempFolder, "task_" + tasks.size());
			if (dbsnp != null) {
				task.setDbSnp(dbsnp);
			}
			task.setVcfFilename(vcf);
			task.setMinR2(minR2);
			task.setGenotypeFormat(genotypeFormat);
			if (outputVariantFilename != null) {
				task.setOutputVariantFilename(taskPrefix + ".variants.txt");
			}
			if (outputEffectsFilename != null) {
				task.setOutputEffectsFilename(taskPrefix + ".effects.txt");
			}
			task.setIncludeVariantFilename(includeVariantFilename);
			task.setIncludeSamplesFilename(includeSamplesFilename);
			task.setOutput(taskPrefix + ".scores.txt");
			tasks.add(task);

		}

		TaskService.setThreads(threads);
		List<Task> results = TaskService.monitor(App.STYLE_LONG_TASK).run(tasks);

		if (isFailed(results)) {
			cleanUp();
			return 1;
		}

		System.out.println();

		MergeScoreTask mergeScore = new MergeScoreTask();
		mergeScore.setInputs(tasks);
		mergeScore.setOutput(out);
		if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeScore))) {
			cleanUp();
			return 1;
		}

		if (outputEffectsFilename != null) {
			MergeEffectsTask mergeEffectsTask = new MergeEffectsTask();
			mergeEffectsTask.setInputs(tasks);
			mergeEffectsTask.setOutput(outputEffectsFilename);
			if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeEffectsTask))) {
				cleanUp();
				return 1;
			}
		}

		if (outputVariantFilename != null) {
			MergeVariantsTask mergeVariantsTask = new MergeVariantsTask();
			mergeVariantsTask.setInputs(tasks);
			mergeVariantsTask.setOutput(outputVariantFilename);
			if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeVariantsTask))) {
				cleanUp();
				return 1;
			}
		}

		MergeReportTask mergeReport = new MergeReportTask();
		mergeReport.setInputs(tasks);
		mergeReport.setOutput(reportJson);
		if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeReport))) {
			cleanUp();
			return 1;
		}

		ReportFile report = mergeReport.getResult();

		if (reportHtml != null) {

			if (meta != null) {
				MetaFile metaFile = MetaFile.load(meta);
				report.mergeWithMeta(metaFile);
			}

			OutputFile data = new OutputFile(out);

			CreateHtmlReportTask htmlReportTask = new CreateHtmlReportTask();
			htmlReportTask.setReport(report);
			htmlReportTask.setData(data);
			htmlReportTask.setOutput(reportHtml);
			if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(htmlReportTask))) {
				cleanUp();
				return 1;
			}
		}

		if (reportCsv != null) {

			if (meta != null) {
				MetaFile metaFile = MetaFile.load(meta);
				report.mergeWithMeta(metaFile);
			}

			OutputFile data = new OutputFile(out);

			CreateHtmlReportTask htmlReportTask = new CreateHtmlReportTask();
			htmlReportTask.setReport(report);
			htmlReportTask.setData(data);
			htmlReportTask.setTemplate("txt");
			htmlReportTask.setOutput(reportCsv);
			if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(htmlReportTask))) {
				cleanUp();
				return 1;
			}
		}

		System.out.println();
		System.out.println("Execution Time: " + formatTime(watch.getElapsedTimeSecs()));
		System.out.println();

		watch.stop();

		cleanUp();

		return 0;

	}

	private String[] parseRef(String ref) {

		try {

			// check if file is a pgscatalog file
			PGSCatalogIDFile file = new PGSCatalogIDFile(ref);
			return file.getIds();

		} catch (Exception e) {
			try {

				// check if file is a file with scores
				ScoresFile file = new ScoresFile(ref);
				return file.getFilenames();

			} catch (Exception e1) {

				String[] refs = ref.split(",");
				for (int i = 0; i < refs.length; i++) {
					refs[i] = refs[i].trim();
				}
				return refs;
			}
		}
	}

	public String formatTime(long timeInSeconds) {
		return String.format("%d min, %d sec", (timeInSeconds / 60), (timeInSeconds % 60));
	}

	private boolean isFailed(List<Task> tasks) {
		for (Task result : tasks) {
			if (!result.getStatus().isSuccess()) {
				result.getStatus().getThrowable().printStackTrace();
				return true;
			}
		}
		return false;
	}

	public void cleanUp() {
		String outParent = new File(out).getParent();
		String tempFolder = FileUtil.path(outParent, "temp");
		File tempFolderFile = new File(tempFolder);
		if (tempFolderFile.exists()) {
			FileUtil.deleteDirectory(tempFolderFile);
		}

	}

}
