package genepi.riskscore.commands;

import java.util.concurrent.Callable;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.tasks.ApplyScoreTask;
import picocli.CommandLine.Option;

public class ApplyScoreCommand implements Callable<Integer> {

	@Option(names = { "--chr" }, description = "Chromosome", required = true)
	String chr;

	@Option(names = { "--vcf" }, description = "VCF file with imputed genotypes", required = true)
	String vcf;

	@Option(names = { "--ref" }, description = "Reference weights", required = true)
	String ref;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	@Option(names = { "--minR2" }, description = "Minimal imputation quality", required = false)
	float minR2 = 0;

	public Integer call() throws Exception {

		System.out.println();
		System.out.println("Input:");
		System.out.println("  chr: " + chr);
		System.out.println("  vcf: " + vcf);
		System.out.println("  ref: " + ref);
		System.out.println("  out: " + out);
		System.out.println();

		ApplyScoreTask task = new ApplyScoreTask();
		task.setMinR2(minR2);
		task.run(chr, vcf, ref);

		System.out.println();
		System.out.println("Risk Score calculation:");
		System.out.println("  Variants: " + task.getCountVariantsUsed());
		System.out.println("  Switched: " + task.getCountVariantsSwitched());
		System.out.println("  Multi Allelic: " + task.getCountVariantsMultiAllelic());
		System.out.println("  Allele Mismatch: " + task.getCountVariantsAlleleMissmatch());
		System.out.println();

		ITableWriter writer = new CsvTableWriter(out, ',');
		writer.setColumns(new String[] { "chr", "sample", "score" });

		System.out.println("Write output to " + out + "...");
		for (RiskScore riskScore : task.getRiskScores()) {
			writer.setString("chr", chr);
			writer.setString("sample", riskScore.getSample());
			writer.setString("score", riskScore.getScore() + "");
			writer.next();
		}

		writer.close();

		System.out.println("Output written. Done!");
		System.out.println();

		return 0;

	}

}
