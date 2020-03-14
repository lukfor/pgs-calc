package genepi.riskscore.tasks;

import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;

public class ApplyScoreTask {

	private RiskScore[] riskScores;

	private int countSamples;

	private int countVariants;

	private int countVariantsUsed;

	private int countVariantsSwitched;

	private int countVariantsMultiAllelic;

	private int countVariantsNotUsed;

	private int countVariantsAlleleMissmatch;

	private int countR2Filtered;

	private float minR2 = 0;

	public static final String INFO_R2 = "R2";

	public static final String DOSAGE_FORMAT = "DS";

	public void run(String vcfFilename, String riskScoreFilename) throws Exception {

		long start = System.currentTimeMillis();

		// read chromosome from first variant
		String chromosome = null;
		FastVCFFileReader vcfReader = new FastVCFFileReader(vcfFilename);
		while (vcfReader.next()) {
			chromosome = vcfReader.getVariantContext().getContig();
		}
		vcfReader.close();

		RiskScoreFile riskscore = new RiskScoreFile(riskScoreFilename);
		System.out.println("Loading file " + riskScoreFilename + "...");
		riskscore.buildIndex(chromosome);
		System.out.println("Loaded " + riskscore.getCountVariants() + " weights for chromosome " + chromosome);

		System.out.println("Loading file " + vcfFilename + "...");

		vcfReader = new FastVCFFileReader(vcfFilename);
		countSamples = vcfReader.getGenotypedSamples().size();

		riskScores = new RiskScore[countSamples];
		for (int i = 0; i < countSamples; i++) {
			riskScores[i] = new RiskScore(chromosome, vcfReader.getGenotypedSamples().get(i));
		}

		countVariants = 0;

		countVariantsUsed = 0;

		countVariantsSwitched = 0;

		countVariantsMultiAllelic = 0;

		countVariantsAlleleMissmatch = 0;

		countR2Filtered = 0;

		while (vcfReader.next()) {

			MinimalVariantContext variant = vcfReader.getVariantContext();

			countVariants++;

			// TODO: add filter based on snp position (include, exclude) or imputation
			// quality (R2)

			if (!variant.getContig().equals(chromosome)) {
				vcfReader.close();
				throw new Exception("Different chromosomes found in file.");
			}

			int position = variant.getStart();

			boolean isPartOfRiskScore = riskscore.contains(position);

			if (!isPartOfRiskScore) {
				continue;
			}

			// Imputation Quality Filter
			double r2 = variant.getInfoAsDouble(INFO_R2, 0);
			if (r2 < minR2) {
				countR2Filtered++;
				continue;
			}

			ReferenceVariant referenceVariant = riskscore.getVariant(position);

			if (variant.isComplexIndel()) {
				countVariantsMultiAllelic++;
				continue;
			}

			float effectWeight = referenceVariant.getEffectWeight();

			char referenceAllele = variant.getReferenceAllele().charAt(0);

			// ignore deletions
			if (variant.getAlternateAllele().length() == 0) {
				continue;
			}

			char alternateAllele = variant.getAlternateAllele().charAt(0);

			if (!referenceVariant.hasAllele(referenceAllele) || !referenceVariant.hasAllele(alternateAllele)) {
				countVariantsAlleleMissmatch++;
				continue;
			}

			if (!referenceVariant.isEffectAllele(referenceAllele)) {
				effectWeight = -effectWeight;
				countVariantsSwitched++;
			}

			String[] values = variant.getGenotypes(DOSAGE_FORMAT);

			for (int i = 0; i < countSamples; i++) {
				float dosage = Float.parseFloat(values[i]);
				float score = riskScores[i].getScore() + (dosage * effectWeight);
				riskScores[i].setScore(score);
			}

			countVariantsUsed++;

		}

		vcfReader.close();

		System.out.println("Loaded " + getRiskScores().length + " samples and " + getCountVariants() + " variants.");

		countVariantsNotUsed = riskscore.getCountVariants() - countVariantsUsed;

		long end = System.currentTimeMillis();

		System.out.println("Execution Time: " + ((end - start) / 1000.0 / 60.0) + " min");

	}

	public void setMinR2(float minR2) {
		this.minR2 = minR2;
	}

	public int getCountSamples() {
		return countSamples;
	}

	public RiskScore[] getRiskScores() {
		return riskScores;
	}

	public int getCountVariants() {
		return countVariants;
	}

	public int getCountVariantsUsed() {
		return countVariantsUsed;
	}

	public int getCountVariantsSwitched() {
		return countVariantsSwitched;
	}

	public int getCountVariantsMultiAllelic() {
		return countVariantsMultiAllelic;
	}

	public int getCountVariantsNotUsed() {
		return countVariantsNotUsed;
	}

	public int getCountVariantsAlleleMissmatch() {
		return countVariantsAlleleMissmatch;
	}

	public int getCountR2Filtered() {
		return countR2Filtered;
	}

}
