package genepi.riskscore.commands;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.io.scores.LineWriter;
import genepi.riskscore.io.scores.RiskScoreCollection;
import genepi.riskscore.io.scores.RiskScoreFile;
import genepi.riskscore.model.ReferenceVariant;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create-panel", version = App.VERSION)
public class CreatePanelCommand implements Callable<Integer> {

	@Option(names = "--out", description = "output score file", required = true)
	private String output;

	@Parameters(description = "score files")
	private String[] filenames;

	@Override
	public Integer call() throws Exception {

		RiskScoreCollection collection = new RiskScoreCollection(filenames);
		collection.setVerbose(true);

		System.out.println("Detect all chromosomes...");

		Set<String> chromosomes = collection.getAllChromosomes(null);
		System.out.println("Found " + chromosomes.size() + " chromosomes.");

		LineWriter writer = new LineWriter(output);

		// TODO: write header (e.g. build, ....)
		writer.write("#Date: " + new Date());
		writer.write("#Scores: " + filenames.length);

		String line = ("chr_name" + "\t" + "chr_position");
		for (int i = 0; i < filenames.length; i++) {
			String name = RiskScoreFile.getName(filenames[i]);
			line += ("\t" + name + "_effect_weight\t" + name + "_effect_allele\t" + name + "_other_allele");
		}

		writer.write(line);

		for (String chr : chromosomes) {

			System.out.println();
			System.out.println("Process chromosome " + chr + "...");

			collection.buildIndex(chr, null, null);

			System.out.println("Write collection to file " + output);

			SortedSet<Integer> positions = collection.getAllPositions();
			for (Integer position : positions) {
				line = (chr + "\t" + position);
				for (int i = 0; i < collection.getSize(); i++) {
					if (collection.contains(i, position)) {
						ReferenceVariant variant = collection.getVariant(i, position);
						line += ("\t" + variant.getEffectWeight() + "\t" + variant.getEffectAllele() + "\t"
								+ variant.getOtherAllele());
					} else {
						line += ("\t\t\t");
					}
				}
				writer.write(line);

			}

			System.out
					.println("Wrote " + positions.size() + " unique variants and " + collection.getSize() + " scores.");

		}

		writer.close();

		return 0;

	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setFilenames(String[] filenames) {
		this.filenames = filenames;
	}

}
