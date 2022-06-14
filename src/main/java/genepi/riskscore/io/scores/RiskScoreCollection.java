package genepi.riskscore.io.scores;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreSummary;

public class RiskScoreCollection implements IRiskScoreCollection {

	private String build;

	private String name;

	private String version;

	private RiskScoreFile[] riskscores;

	private RiskScoreSummary[] summaries;

	private int numberRiskScores;

	private String[] filenames;

	private boolean verbose = false;

	public RiskScoreCollection(String... filenames) {
		this.filenames = filenames;
	}

	@Override
	public String getBuild() {
		return build;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Set<String> getAllChromosomes(String dbsnp) throws Exception {
		Set<String> chromosomes = new HashSet<String>();
		for (int i = 0; i < filenames.length; i++) {
			RiskScoreFormat format = null;

			String autoFormat = filenames[i] + ".format";
			if (new File(autoFormat).exists()) {
				format = RiskScoreFormat.MAPPING_FILE;
			} else {
				format = RiskScoreFormat.PGS_CATALOG;
			}
			RiskScoreFile riskscore = new RiskScoreFile(filenames[i], format, dbsnp);
			chromosomes.addAll(riskscore.getAllChromosomes());
		}
		return chromosomes;
	}

	@Override
	public void buildIndex(String chromosome, Chunk chunk, String dbsnp) throws Exception {

		numberRiskScores = filenames.length;

		summaries = new RiskScoreSummary[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {
			String name = RiskScoreFile.getName(filenames[i]);
			summaries[i] = new RiskScoreSummary(name);
		}

		int total = 0;

		riskscores = new RiskScoreFile[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {

			if (verbose) {
				System.out.println("Loading file " + filenames[i] + "...");
			}

			RiskScoreFormat format = null;

			String autoFormat = filenames[i] + ".format";
			if (new File(autoFormat).exists()) {
				format = RiskScoreFormat.MAPPING_FILE;
			} else {
				format = RiskScoreFormat.PGS_CATALOG;
			}

			RiskScoreFile riskscore = new RiskScoreFile(filenames[i], format, dbsnp);

			if (chunk != null) {
				riskscore.buildIndex(chromosome, chunk);
			} else {
				riskscore.buildIndex(chromosome);
			}

			summaries[i].setVariants(riskscore.getTotalVariants());
			summaries[i].setVariantsIgnored(riskscore.getIgnoredVariants());

			if (verbose) {
				System.out.println("Loaded " + riskscore.getCacheSize() + " weights for chromosome " + chromosome);
			}
			total += riskscore.getCacheSize();
			riskscores[i] = riskscore;

		}

		if (verbose) {
			System.out.println();
			System.out.println("Collection contains " + total + " weights for chromosome " + chromosome);
			System.out.println();
		}
	}

	@Override
	public RiskScoreSummary getSummary(int index) {
		return summaries[index];
	}

	@Override
	public boolean contains(int index, int position) {
		return riskscores[index].contains(position);
	}

	@Override
	public ReferenceVariant getVariant(int index, int position) {
		return riskscores[index].getVariant(position);
	}

	@Override
	public int getSize() {
		return numberRiskScores;
	}

	@Override
	public boolean isEmpty() {
		for (RiskScoreFile riskscore : riskscores) {
			if (riskscore.getCacheSize() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public RiskScoreSummary[] getSummaries() {
		return summaries;
	}

	@Override
	public SortedSet<Integer> getAllPositions() {
		SortedSet<Integer> positions = new TreeSet<Integer>();
		for (RiskScoreFile score : riskscores) {
			positions.addAll(score.getPositions());
		}
		return positions;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
