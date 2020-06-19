package genepi.riskscore.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.VariantFile;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.model.RiskScoreSummary;

public class ApplyScoreTask {

	private RiskScore[] riskScores;

	private List<String> vcfs = null;

	private String riskScoreFilenames[] = null;

	private int countSamples = 0;

	private int countVariants = 0;

	private Chunk chunk = null;

	private float minR2 = 0;

	private String outputVariantFilename = null;

	private String includeVariantFilename = null;

	private CsvTableWriter variantFile;

	private RiskScoreFormat defaultFormat = new PGSCatalogFormat();

	private Map<String, RiskScoreFormat> formats = new HashMap<String, RiskScoreFormat>();

	private String genotypeFormat = DOSAGE_FORMAT;

	private int numberRiskScores = 0;

	private RiskScoreSummary[] summaries;

	public static final String INFO_R2 = "R2";

	public static final String DOSAGE_FORMAT = "DS";

	public void setRiskScoreFilenames(String... filenames) {
		this.riskScoreFilenames = filenames;
		for (String filename : filenames) {
			formats.put(filename, defaultFormat);
		}
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
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

	public void setOutputVariantFilename(String outputVariantFilename) {
		this.outputVariantFilename = outputVariantFilename;
	}

	public void setIncludeVariantFilename(String includeVariantFilename) {
		this.includeVariantFilename = includeVariantFilename;
	}

	public void setGenotypeFormat(String genotypeFormat) {
		this.genotypeFormat = genotypeFormat;
	}

	public void run() throws Exception {

		if (vcfs == null || vcfs.isEmpty()) {
			throw new Exception("Please specify at leat one vcf file.");
		}

		if (riskScoreFilenames == null || riskScoreFilenames.length == 0) {
			throw new Exception("Reference can not be null or empty.");
		}

		long start = System.currentTimeMillis();

		if (outputVariantFilename != null) {
			variantFile = new CsvTableWriter(outputVariantFilename, VariantFile.SEPARATOR);
			variantFile.setColumns(new String[] { VariantFile.CHROMOSOME, VariantFile.POSITION });
		}

		numberRiskScores = riskScoreFilenames.length;
		summaries = new RiskScoreSummary[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {
			if (i == 0) {
				summaries[i] = new RiskScoreSummary("score");
			} else {
				summaries[i] = new RiskScoreSummary("score_" + i);
			}
		}

		for (String vcfFilename : vcfs) {
			processVCF(vcfFilename, riskScoreFilenames);
		}

		if (variantFile != null) {
			variantFile.close();
		}

		long end = System.currentTimeMillis();

		System.out.println("Execution Time: " + ((end - start) / 1000.0 / 60.0) + " min");

	}

	private void processVCF(String vcfFilename, String... riskScoreFilenames) throws Exception {

		// read chromosome from first variant
		String chromosome = null;
		FastVCFFileReader vcfReader = new FastVCFFileReader(vcfFilename);
		if (vcfReader.next()) {
			chromosome = vcfReader.getVariantContext().getContig();
			vcfReader.close();
		} else {
			vcfReader.close();
			throw new Exception("VCF file is empty.");
		}

		VariantFile includeVariants = null;
		if (includeVariantFilename != null) {
			System.out.println("Loading file " + includeVariantFilename + "...");
			includeVariants = new VariantFile(includeVariantFilename);
			includeVariants.buildIndex(chromosome);
			System.out.println("Loaded " + includeVariants.getCacheSize() + " variants for chromosome " + chromosome);
		}
		RiskScoreFile[] riskscores = new RiskScoreFile[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {

			System.out.println("Loading file " + riskScoreFilenames[i] + "...");

			RiskScoreFormat format = formats.get(riskScoreFilenames[i]);
			RiskScoreFile riskscore = new RiskScoreFile(riskScoreFilenames[i], format);

			if (chunk != null) {
				riskscore.buildIndex(chromosome, chunk);
			} else {
				riskscore.buildIndex(chromosome);
			}

			summaries[i].setVariants(riskscore.getTotalVariants());

			System.out.println("Loaded " + riskscore.getCacheSize() + " weights for chromosome " + chromosome);
			riskscores[i] = riskscore;
		}

		System.out.println("Loading file " + vcfFilename + "...");

		vcfReader = new FastVCFFileReader(vcfFilename);
		countSamples = vcfReader.getGenotypedSamples().size();

		if (riskScores == null) {
			riskScores = new RiskScore[countSamples];
			for (int i = 0; i < countSamples; i++) {
				riskScores[i] = new RiskScore(chromosome, vcfReader.getGenotypedSamples().get(i),
						riskScoreFilenames.length);

			}
		} else {
			if (riskScores.length != countSamples) {
				vcfReader.close();
				throw new IOException("Different number of samples in file '" + vcfFilename + "'. Expected "
						+ riskScores.length + " samples but found " + countSamples + " samples.");
			}
		}

		boolean outOfChunk = false;

		while (vcfReader.next() && !outOfChunk) {

			MinimalVariantContext variant = vcfReader.getVariantContext();

			countVariants++;

			if (!variant.getContig().equals(chromosome)) {
				vcfReader.close();
				throw new Exception("Different chromosomes found in file.");
			}

			int position = variant.getStart();

			if (chunk != null) {

				if (position < chunk.getStart()) {
					continue;
				}

				if (position > chunk.getEnd()) {
					outOfChunk = true;
					continue;
				}

			}

			for (int j = 0; j < riskScoreFilenames.length; j++) {

				RiskScoreSummary summary = summaries[j];

				RiskScoreFile riskscore = riskscores[j];
				boolean isPartOfRiskScore = riskscore.contains(position);

				if (!isPartOfRiskScore) {
					summary.incNotFound();
					continue;
				}

				if (includeVariants != null) {
					if (!includeVariants.contains(position)) {
						summary.incFiltered();
						continue;
					}
				}

				// Imputation Quality Filter
				double r2 = variant.getInfoAsDouble(INFO_R2, 0);
				if (r2 < minR2) {
					summary.incR2Filtered();
					continue;
				}

				ReferenceVariant referenceVariant = riskscore.getVariant(position);

				if (variant.isComplexIndel()) {
					summary.incMultiAllelic();
					continue;
				}

				float effectWeight = -referenceVariant.getEffectWeight();

				char referenceAllele = variant.getReferenceAllele().charAt(0);

				// ignore deletions
				if (variant.getAlternateAllele().length() == 0) {
					summary.incMultiAllelic();
					continue;
				}

				char alternateAllele = variant.getAlternateAllele().charAt(0);

				if (!referenceVariant.hasAllele(referenceAllele) || !referenceVariant.hasAllele(alternateAllele)) {
					summary.incAlleleMissmatch();
					continue;
				}

				if (!referenceVariant.isEffectAllele(referenceAllele)) {
					effectWeight = -effectWeight;
					summary.incSwitched();
				}

				if (variantFile != null) {
					variantFile.setString(VariantFile.CHROMOSOME, variant.getContig());
					variantFile.setInteger(VariantFile.POSITION, variant.getStart());
					variantFile.next();
				}

				float[] dosages = variant.getGenotypeDosages(genotypeFormat);

				for (int i = 0; i < countSamples; i++) {
					float dosage = dosages[i];
					double score = riskScores[i].getScore(j) + (dosage * effectWeight);
					riskScores[i].setScore(j, score);
				}

				summary.incVariantsUsed();

			}
		}

		vcfReader.close();

		System.out.println("Loaded " + getRiskScores().length + " samples and " + countVariants + " variants.");

	}

	public void setMinR2(float minR2) {
		this.minR2 = minR2;
	}

	public void setDefaultRiskScoreFormat(RiskScoreFormat defaultFormat) {
		this.defaultFormat = defaultFormat;
		if (riskScoreFilenames != null) {
			for (String file : riskScoreFilenames) {
				setRiskScoreFormat(file, defaultFormat);
			}
		}
	}

	public void setRiskScoreFormat(String file, RiskScoreFormat format) {
		this.formats.put(file, format);
	}

	public int getCountSamples() {
		return countSamples;
	}

	public RiskScore[] getRiskScores() {
		return riskScores;
	}

	public RiskScoreSummary[] getSummaries() {
		return summaries;
	}

	public int getCountVariants() {
		return countVariants;
	}
}
