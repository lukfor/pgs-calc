package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;

public class ApplyScoreTask {

	private RiskScore[] riskScores;

	private List<String> vcfs = null;

	private String riskScoreFilename = null;

	private int countSamples = 0;

	private int countVariants = 0;

	private int countVariantsUsed = 0;

	private int countVariantsSwitched = 0;

	private int countVariantsMultiAllelic = 0;

	private int countVariantsNotUsed = 0;

	private int countVariantsAlleleMissmatch = 0;

	private int countR2Filtered = 0;

	private int countVariantsRiskScore = 0;

	private int countNotFound = 0;
	
	private float minR2 = 0;

	public static final String INFO_R2 = "R2";

	public static final String DOSAGE_FORMAT = "DS";

	public void setRiskScoreFilename(String filename) {
		this.riskScoreFilename = filename;
	}

	public void setVcfFilenames(List<String> vcfs) {
		this.vcfs = vcfs;
	}

	public void setVcfFilenames(String... vcfs) {
		this.vcfs = new Vector<String>();
		for (String vcf : vcfs) {
			this.vcfs.add(vcf);
		}
	}

	public void run() throws Exception {

		if (vcfs == null || vcfs.isEmpty()) {
			throw new Exception("Please specify at leat one vcf file.");
		}

		if (riskScoreFilename == null) {
			throw new Exception("Reference can not be null.");
		}

		long start = System.currentTimeMillis();

		for (String vcfFilename : vcfs) {
			processVCF(vcfFilename, riskScoreFilename);
		}

		countVariantsNotUsed = (countVariantsRiskScore - countVariantsUsed);

		long end = System.currentTimeMillis();

		System.out.println("Execution Time: " + ((end - start) / 1000.0 / 60.0) + " min");

	}

	private void processVCF(String vcfFilename, String riskScoreFilename) throws Exception {

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
		if (countVariantsRiskScore == 0) {
			countVariantsRiskScore = riskscore.getTotalVariants();
		}
		System.out.println("Loaded " + riskscore.getCacheSize() + " weights for chromosome " + chromosome);

		System.out.println("Loading file " + vcfFilename + "...");

		vcfReader = new FastVCFFileReader(vcfFilename);
		countSamples = vcfReader.getGenotypedSamples().size();

		if (riskScores == null) {
			riskScores = new RiskScore[countSamples];
			for (int i = 0; i < countSamples; i++) {
				riskScores[i] = new RiskScore(chromosome, vcfReader.getGenotypedSamples().get(i));
			}
		} else {
			if (riskScores.length != countSamples) {
				vcfReader.close();
				throw new IOException("Different number of samples in file '" + vcfFilename + "'. Expected "
						+ riskScores.length + " samples but found " + countSamples + " samples.");
			}
		}

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
				countNotFound++;
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
				countVariantsMultiAllelic++;
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

	public int getCountVariantsFilteredR2() {
		return countR2Filtered;
	}
	
	public int getCountVariantsRiskScore() {
		return countVariantsRiskScore;
	}
	
	public int getCountVariantsNotFound() {
		return countNotFound;
	}

}
