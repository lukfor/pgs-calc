package genepi.riskscore.io.scores;

import java.io.File;
import java.util.*;

import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.RiskScoreFile;
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

	private Map<String, RiskScoreFormat> formats;

	public RiskScoreCollection(String[] filenames, Map<String, RiskScoreFormat> formats) {
		this.filenames = filenames;
		this.formats = formats;
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
	public void buildIndex(String chromosome, Chunk chunk, String dbsnp, String proxies) throws Exception {

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

			RiskScoreFormat format = formats.get(filenames[i]);
			RiskScoreFile riskscore = new RiskScoreFile(filenames[i], format,  dbsnp, proxies);

			if (chunk != null) {
				riskscore.buildIndex(chromosome, chunk);
			} else {
				riskscore.buildIndex(chromosome);
			}

			summaries[i].setVariants(riskscore.getTotalVariants());
			summaries[i].setVariantsIgnored(riskscore.getIgnoredVariants());

			if (verbose) {
				System.out.println("Loaded " + riskscore.getLoadedVariants() + " weights for chromosome " + chromosome);
			}
			total += riskscore.getLoadedVariants();
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
	public Set<Map.Entry<Integer, ReferenceVariant>> getAllVariants(int index) {
		return riskscores[index].getVariants().entrySet();
	}

	@Override
	public int getSize() {
		return numberRiskScores;
	}

	@Override
	public void clearIndex() {
		for (RiskScoreFile riskscore: riskscores) {
			riskscore.clearIndex();
		}
	}

	@Override
	public boolean isEmpty() {
		for (RiskScoreFile riskscore : riskscores) {
			if (riskscore.getLoadedVariants() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public RiskScoreSummary[] getSummaries() {
		return summaries;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
