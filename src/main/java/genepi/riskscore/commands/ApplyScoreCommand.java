package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.tasks.ApplyScoreTask;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class ApplyScoreCommand implements Callable<Integer> {

	@Parameters
	List<String> vcfs;

	@Option(names = { "--ref" }, description = "Reference weights", required = true)
	String ref;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	@Option(names = { "--minR2" }, description = "Minimal imputation quality", required = false)
	float minR2 = 0;

	public Integer call() throws Exception {

		if (vcfs == null || vcfs.isEmpty()) {
			System.out.println();
			System.out.println("Please provide at least one VCF file.");
			System.out.println();
			return 1;
		}
		
		System.out.println();
		System.out.println("Input:");
		System.out.println("  vcfs: " + vcfs);
		System.out.println("  ref: " + ref);
		System.out.println("  out: " + out);
		System.out.println("  minR2: " + minR2);
		System.out.println();
		
		ITableWriter writer = new CsvTableWriter(out, ',');
		writer.setColumns(new String[] { "chr", "sample", "score" });
		
		for (String vcf : vcfs) {

			ApplyScoreTask task = new ApplyScoreTask();
			task.setMinR2(minR2);
			task.run(vcf, ref);

			System.out.println();
			System.out.println("Risk Score calculation:");
			System.out.println("  Variants: " + task.getCountVariantsUsed());
			System.out.println("  Switched: " + task.getCountVariantsSwitched());
			System.out.println("  Multi Allelic: " + task.getCountVariantsMultiAllelic());
			System.out.println("  Allele Mismatch: " + task.getCountVariantsAlleleMissmatch());
			System.out.println();


			for (RiskScore riskScore : task.getRiskScores()) {
				writer.setString("chr", riskScore.getChromosome());
				writer.setString("sample", riskScore.getSample());
				writer.setString("score", riskScore.getScore() + "");
				writer.next();
			}

			System.out.println("Output written to '" + out + "'. Done!");
			System.out.println();

		}
		
		writer.close();
		
		return 0;

	}

}
