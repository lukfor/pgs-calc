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

	public Integer call() throws Exception {

		System.out.println("Input:\n  " + vcf);

		ApplyScoreTask task = new ApplyScoreTask();
		task.run(chr, vcf, ref);

		System.out.println("Samples: " + task.getRiskScores().length);
		System.out.println("Variants Total: " + task.getCountVariants());
		System.out.println("Variants Used: " + task.getCountVariantsUsed());

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
