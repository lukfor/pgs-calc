package genepi.riskscore.io.scores;

import java.io.File;

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
	public void buildIndex(String chromosome, Chunk chunk, String dbsnp) throws Exception {

		numberRiskScores = filenames.length;

		summaries = new RiskScoreSummary[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {
			String name = RiskScoreFile.getName(filenames[i]);
			summaries[i] = new RiskScoreSummary(name);
		}

		riskscores = new RiskScoreFile[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {

			// System.out.println("Loading file " + riskScoreFilenames[i] + "...");

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

			// System.out.println("Loaded " + riskscore.getCacheSize() + " weights for
			// chromosome " + chromosome);
			riskscores[i] = riskscore;

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

}
