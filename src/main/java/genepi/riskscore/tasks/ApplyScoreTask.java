package genepi.riskscore.tasks;

import java.io.File;

import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.model.RiskScore;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

public class ApplyScoreTask {

	private RiskScore[] riskScores;

	private int countSamples;

	private int countVariants;

	private int countVariantsUsed;

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

		for (VariantContext variant : vcfReader) {

			// TODO: add filter based on snp position (include, exclude) or imputation
			// quality (R2)

			if (!variant.getContig().equals(chromosome)) {
				vcfReader.close();
				throw new Exception("Different chromosomes found in file.");
			}

			boolean hasWeight = riskscore.contains(variant.getStart());

			if (hasWeight) {

				float variantWeight = riskscore.getWeight(variant.getStart());

				// TODO: compare alleles and switch sign

				for (int i = 0; i < countSamples; i++) {
					Genotype genotype = variant.getGenotype(i);
					float dosage = Float.parseFloat(genotype.getExtendedAttribute(DOSAGE_FORMAT).toString());
					float score = riskScores[i].getScore() + (dosage * variantWeight);
					riskScores[i].setScore(score);
				}
				countVariantsUsed++;
			}

			countVariants++;

		}

		vcfReader.close();

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
}
