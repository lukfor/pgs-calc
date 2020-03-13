package genepi.riskscore.tasks;

import java.io.File;

import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

public class ApplyScoreTask {

	private RiskScore[] riskScores;

	private int countSamples;

	private int countVariants;

	private int countVariantsUsed;

	private int countVariantsSwitched;

	private int countVariantsMultiAllelic;

	private int countVariantsNotUsed;

	private int countVariantsAlleleMissmatch;

	public static final String DOSAGE_FORMAT = "DS";

	public void run(String chromosome, String vcfFilename, String riskScoreFilename) throws Exception {

		RiskScoreFile riskscore = new RiskScoreFile(riskScoreFilename);
		System.out.println("Loading file " + riskScoreFilename + "...");
		riskscore.buildIndex(chromosome);
		System.out.println("Loaded " + riskscore.getCountVariants() + " weights for chromosome " + chromosome);

		System.out.println("Loading file " + vcfFilename + "...");

		VCFFileReader vcfReader = new VCFFileReader(new File(vcfFilename), false);

		countSamples = vcfReader.getFileHeader().getGenotypeSamples().size();

		riskScores = new RiskScore[countSamples];
		for (int i = 0; i < countSamples; i++) {
			riskScores[i] = new RiskScore(vcfReader.getFileHeader().getGenotypeSamples().get(i));
		}

		countVariants = 0;

		countVariantsUsed = 0;

		countVariantsSwitched = 0;

		countVariantsMultiAllelic = 0;

		countVariantsAlleleMissmatch = 0;

		for (VariantContext variant : vcfReader) {

			countVariants++;

			// TODO: add filter based on snp position (include, exclude) or imputation
			// quality (R2)

			if (!variant.getContig().equals(chromosome)) {
				vcfReader.close();
				throw new Exception("Different chromosomes found in file.");
			}

			int position = variant.getStart();

			boolean isPartOfRiskScore = riskscore.contains(position);

			if (isPartOfRiskScore) {

				ReferenceVariant referenceVariant = riskscore.getVariant(position);

				if (variant.getAlleles().size() > 2) {
					countVariantsMultiAllelic++;
					continue;
				}

				float effectWeight = referenceVariant.getEffectWeight();

				char referenceAllele = variant.getReference().getBaseString().charAt(0);

				// ignore deletions
				if (variant.getAlternateAllele(0).getBaseString().length() == 0) {
					continue;
				}

				char alternateAllele = variant.getAlternateAllele(0).getBaseString().charAt(0);

				if (!referenceVariant.hasAllele(referenceAllele) || !referenceVariant.hasAllele(alternateAllele)) {
					countVariantsAlleleMissmatch++;
					continue;
				}

				if (!referenceVariant.isEffectAllele(referenceAllele)) {
					effectWeight = -effectWeight;
					countVariantsSwitched++;
				}

				for (int i = 0; i < countSamples; i++) {
					Genotype genotype = variant.getGenotype(i);
					float dosage = Float.parseFloat(genotype.getExtendedAttribute(DOSAGE_FORMAT).toString());
					float score = riskScores[i].getScore() + (dosage * effectWeight);
					riskScores[i].setScore(score);
				}
				countVariantsUsed++;
			}

		}

		vcfReader.close();

		System.out.println("Loaded " + getRiskScores().length + " samples and " + getCountVariants() + " variants.");

		countVariantsNotUsed = riskscore.getCountVariants() - countVariantsUsed;

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

}
