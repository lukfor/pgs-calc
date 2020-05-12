package genepi.riskscore.commands;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.App;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.tasks.ApplyScoreTask;
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

	public static final String COLUMN_SAMPLE = "sample";

	public static final String COLUMN_SCORE = "score";

	public static final char SEPARATOR = ',';

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
		System.out.println("  vcfs (" + vcfs.size() + "):");
		for (String vcf : vcfs) {
			System.out.println("   - " + vcf);
		}
		System.out.println();

		ApplyScoreTask task = new ApplyScoreTask();
		task.setRiskScoreFilename(ref);
		task.setVcfFilenames(vcfs);
		task.setMinR2(minR2);
		task.setGenotypeFormat(genotypeFormat);
		if (format != null) {
			RiskScoreFormat riskScoreFormat = RiskScoreFormat.load(format);
			task.setRiskScoreFormat(riskScoreFormat);
		} else {
			String autoFormat = ref + ".format";
			if (new File(autoFormat).exists()) {
				RiskScoreFormat riskScoreFormat = RiskScoreFormat.load(autoFormat);
				task.setRiskScoreFormat(riskScoreFormat);
			}
		}
		task.setOutputVariantFilename(outputVariantFilename);
		task.setIncludeVariantFilename(includeVariantFilename);

		task.run();

		writeOutputFile(task.getRiskScores(), out);

		System.out.println();
		System.out.println("Summary");
		System.out.println("-------");
		System.out.println();
		System.out.println("  Target VCF file(s):");
		System.out.println("    - Samples: " + task.getCountSamples());
		System.out.println("    - Variants: " + task.getCountVariants());
		System.out.println();
		System.out.println("  Risk Score:");
		System.out.println("    - Variants: " + task.getCountVariantsRiskScore());
		System.out.println("    - Found in target: " + task.getCountVariantsUsed());
		System.out.println("    - Found in target and filtered by: ");
		System.out.println("      - not in variant file: " + task.getCountFiltered());
		System.out.println("      - allele mismatch: " + task.getCountVariantsAlleleMissmatch());
		System.out.println("      - multi allelic or indels: " + task.getCountVariantsMultiAllelic());
		System.out.println("      - low R2 value: " + task.getCountVariantsFilteredR2());
		System.out.println("    - Not found in target: " + task.getCountVariantsNotFound());
		System.out.println();
		return 0;

	}

	protected void writeOutputFile(RiskScore[] finalScores, String filename) {

		ITableWriter writer = new CsvTableWriter(filename, SEPARATOR);
		writer.setColumns(new String[] { COLUMN_SAMPLE, COLUMN_SCORE });

		for (RiskScore riskScore : finalScores) {
			writer.setString(COLUMN_SAMPLE, riskScore.getSample());
			writer.setString(COLUMN_SCORE, riskScore.getScore() + "");
			writer.next();
		}

		System.out.println("Output written to '" + filename + "'. Done!");
		System.out.println();

		writer.close();

	}

}
