package genepi.riskscore.commands;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.model.RiskScoreSummary;
import genepi.riskscore.tasks.ApplyScoreTask;
import htsjdk.samtools.util.StopWatch;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = App.APP, version = App.VERSION)
public class ApplyScoreCommand implements Callable<Integer> {

	@Parameters(description = "VCF files")
	List<String> vcfs;

	@Option(names = { "--ref" }, description = "Reference weights", required = true)
	String ref;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	@Option(names = {
			"--minR2" }, description = "Minimal imputation quality", required = false, showDefaultValue = Visibility.ALWAYS)
	float minR2 = 0;

	@Option(names = {
			"--genotypes" }, description = "Genotype field (DS or GT)", required = false, showDefaultValue = Visibility.ALWAYS)
	String genotypeFormat = ApplyScoreTask.DOSAGE_FORMAT;

	@Option(names = { "--format" }, description = "Reference weights format file", required = false)
	String format = null;

	@Option(names = { "--writeVariants" }, description = "Write csv file with all used variants", required = false)
	String outputVariantFilename = null;

	@Option(names = { "--includeVariants" }, description = "Include only variants from this file", required = false)
	String includeVariantFilename = null;

	@Option(names = { "--help" }, usageHelp = true)
	boolean showHelp;

	@Option(names = { "--version" }, versionHelp = true)
	boolean showVersion;

	@ArgGroup(exclusive = false, multiplicity = "0..1")
	Chunk chunk;

	public Integer call() throws Exception {

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

		ApplyScoreTask task = new ApplyScoreTask();

		String[] refs = parseRef(ref);
		task.setRiskScoreFilenames(refs);
		if (format != null) {
			RiskScoreFormat riskScoreFormat = RiskScoreFormat.load(format);
			for (String file : refs) {
				task.setRiskScoreFormat(file, riskScoreFormat);
			}
		} else {
			for (String file : refs) {
				String autoFormat = file + ".format";
				if (new File(autoFormat).exists()) {
					RiskScoreFormat riskScoreFormat = RiskScoreFormat.load(autoFormat);
					task.setRiskScoreFormat(file, riskScoreFormat);
				}
			}
		}

		if (chunk != null) {
			task.setChunk(chunk);
		}
		task.setVcfFilenames(vcfs);
		task.setMinR2(minR2);
		task.setGenotypeFormat(genotypeFormat);
		task.setOutputVariantFilename(outputVariantFilename);
		task.setIncludeVariantFilename(includeVariantFilename);

		StopWatch watch = new StopWatch();
		watch.start();
		task.run();

		OutputFile output = new OutputFile(task.getRiskScores(), task.getSummaries());
		output.save(out);
		watch.stop();

		System.out.println("Output written to '" + out + "'. Done!");
		System.out.println();

		System.out.println();
		System.out.println("Summary");
		System.out.println("-------");
		System.out.println();
		System.out.println("  Target VCF file(s):");
		System.out.println("    - Samples: " + number(task.getCountSamples()));
		System.out.println("    - Variants: " + number(task.getCountVariants()));
		System.out.println();

		for (RiskScoreSummary summary : task.getSummaries()) {
			System.out.println(summary);
			System.out.println();
		}

		// System.out.println(" - Not found in target: " + number(notFound));
		System.out.println();
		System.out.println("Execution Time: " + formatTime(watch.getElapsedTimeSecs()));
		System.out.println();
		return 0;

	}

	private String[] parseRef(String ref) {
		String[] refs = ref.split(",");
		for (int i = 0; i < refs.length; i++) {
			refs[i] = refs[i].trim();
		}
		return refs;
	}

	public static String number(long number) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		return formatter.format(number);
	}

	public static String percentage(double obtained, double total) {
		double percentage = (obtained / total) * 100;
		DecimalFormat df = new DecimalFormat("###.##'%'");
		return df.format(percentage);
	}

	public String formatTime(long timeInSeconds) {
		return String.format("%d min, %d sec", (timeInSeconds / 60), (timeInSeconds % 60));
	}

}
