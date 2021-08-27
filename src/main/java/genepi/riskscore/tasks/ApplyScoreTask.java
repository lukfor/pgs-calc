package genepi.riskscore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.OutputFileWriter;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.SamplesFile;
import genepi.riskscore.io.VariantFile;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;
import genepi.riskscore.model.RiskScoreFormat;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;
import lukfor.progress.util.CountingInputStream;

public class ApplyScoreTask implements ITaskRunnable {

	private List<RiskScore> riskScores;

	private String vcf = null;

	private String riskScoreFilenames[] = null;

	private int countSamples = 0;

	private int countVariants = 0;

	private Chunk chunk = null;

	private float minR2 = 0;

	private String outputVariantFilename = null;

	private String includeVariantFilename = null;

	private String includeSamplesFilename = null;

	private CsvTableWriter variantFile;

	private RiskScoreFormat defaultFormat = new PGSCatalogFormat();

	private Map<String, RiskScoreFormat> formats = new HashMap<String, RiskScoreFormat>();

	private String genotypeFormat = DOSAGE_FORMAT;

	private int numberRiskScores = 0;

	private RiskScoreSummary[] summaries;

	private String output;

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

	public void setVcfFilename(String vcf) {
		this.vcf = vcf;
	}

	public void setOutputVariantFilename(String outputVariantFilename) {
		this.outputVariantFilename = outputVariantFilename;
	}

	public void setIncludeVariantFilename(String includeVariantFilename) {
		this.includeVariantFilename = includeVariantFilename;
	}

	public void setIncludeSamplesFilename(String includeSamplesFilename) {
		this.includeSamplesFilename = includeSamplesFilename;
	}

	public void setGenotypeFormat(String genotypeFormat) {
		this.genotypeFormat = genotypeFormat;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void run(ITaskMonitor monitor) throws Exception {

		if (vcf == null || vcf.isEmpty()) {
			throw new Exception("Please specify a vcf file.");
		}

		if (output == null || output.isEmpty()) {
			throw new Exception("Please specify a output filename.");
		}

		if (riskScoreFilenames == null || riskScoreFilenames.length == 0) {
			throw new Exception("Reference can not be null or empty.");
		}

		if (outputVariantFilename != null) {
			variantFile = new CsvTableWriter(outputVariantFilename, VariantFile.SEPARATOR);
			variantFile.setColumns(new String[] { VariantFile.CHROMOSOME, VariantFile.POSITION });
		}

		// read chromosome from first variant
		String chromosome = null;
		FastVCFFileReader vcfReader = new FastVCFFileReader(new FileInputStream(vcf), vcf);
		if (vcfReader.next()) {
			chromosome = vcfReader.getVariantContext().getContig();
			vcfReader.close();
		} else {
			vcfReader.close();
			throw new Exception("VCF file is empty.");
		}

		String taskName = "[Chr " + (chromosome.length() == 1 ? "0" : "") + chromosome + "]";
		monitor.begin(taskName, new File(vcf).length());
		monitor.worked(0);

		numberRiskScores = riskScoreFilenames.length;
		summaries = new RiskScoreSummary[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {
			String name = RiskScoreFile.getName(riskScoreFilenames[i]);
			summaries[i] = new RiskScoreSummary(name);
		}

		RiskScoreFile[] riskscores = loadReferenceFiles(monitor, chromosome, riskScoreFilenames);

		boolean empty = true;
		for (RiskScoreFile riskscore : riskscores) {
			if (riskscore.getCacheSize() > 0) {
				empty = false;
				break;
			}
		}

		if (!empty) {

			processVCF(monitor, chromosome, vcf, riskscores);

			if (variantFile != null) {
				variantFile.close();
			}

			OutputFileWriter outputFile = new OutputFileWriter(riskScores, summaries);
			outputFile.save(output);

			ReportFile reportFile = new ReportFile(summaries);
			reportFile.save(output + ".report");

		}

		monitor.done();

	}

	private RiskScoreFile[] loadReferenceFiles(ITaskMonitor monitor, String chromosome, String... riskScoreFilenames)
			throws Exception {

		RiskScoreFile[] riskscores = new RiskScoreFile[numberRiskScores];
		for (int i = 0; i < numberRiskScores; i++) {

			// System.out.println("Loading file " + riskScoreFilenames[i] + "...");

			RiskScoreFormat format = formats.get(riskScoreFilenames[i]);
			RiskScoreFile riskscore = new RiskScoreFile(riskScoreFilenames[i], format);

			if (chunk != null) {
				riskscore.buildIndex(chromosome, chunk);
			} else {
				riskscore.buildIndex(chromosome);
			}

			summaries[i].setVariants(riskscore.getTotalVariants());

			// System.out.println("Loaded " + riskscore.getCacheSize() + " weights for
			// chromosome " + chromosome);
			riskscores[i] = riskscore;
			monitor.worked(0);
		}

		return riskscores;

	}

	private void processVCF(ITaskMonitor monitor, String chromosome, String vcfFilename, RiskScoreFile[] riskscores)
			throws Exception {

		// System.out.println("Loading file " + vcfFilename + "...");

		VariantFile includeVariants = null;
		if (includeVariantFilename != null) {
			// System.out.println("Loading file " + includeVariantFilename + "...");
			includeVariants = new VariantFile(includeVariantFilename);
			includeVariants.buildIndex(chromosome);
			// System.out.println("Loaded " + includeVariants.getCacheSize() + " variants
			// for chromosome " + chromosome);
		}

		SamplesFile samplesFile = null;
		if (includeSamplesFilename != null) {
			samplesFile = new SamplesFile(includeSamplesFilename);
			samplesFile.buildIndex();
		}

		CountingInputStream countingStream = new CountingInputStream(new FileInputStream(vcfFilename), monitor);
		FastVCFFileReader vcfReader = new FastVCFFileReader(countingStream, vcfFilename);
		countSamples = vcfReader.getGenotypedSamples().size();

		riskScores = new Vector<RiskScore>();
		for (int i = 0; i < countSamples; i++) {
			String sample = vcfReader.getGenotypedSamples().get(i);
			if (samplesFile == null || samplesFile.contains(sample)) {
				RiskScore riskScore = new RiskScore(chromosome, sample, riskScoreFilenames.length);
				riskScores.add(riskScore);
			}
		}

		boolean outOfChunk = false;

		while (vcfReader.next() && !outOfChunk) {

			if (monitor.isCanceled()) {
				return;
			}

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

				float effectWeight = referenceVariant.getEffectWeight();

				char referenceAllele = variant.getReferenceAllele().charAt(0);

				// ignore deletions
				if (variant.getAlternateAllele().length() == 0) {
					summary.incMultiAllelic();
					continue;
				}

				char alternateAllele = variant.getAlternateAllele().charAt(0);

				// check if alleles (ref and alt) are present
				if (!referenceVariant.hasAllele(referenceAllele) || !referenceVariant.hasAllele(alternateAllele)) {
					summary.incAlleleMissmatch();
					continue;
				}

				// check if alleles are switched and update effect weight (effect_allele !=
				// alternate_allele)
				if (!referenceVariant.isEffectAllele(alternateAllele)) {
					if (referenceVariant.isEffectAllele(referenceAllele)) {
						effectWeight = -effectWeight;
						summary.incSwitched();
					} else {
						summary.incAlleleMissmatch();
						continue;
					}
				}

				if (referenceVariant.isUsed()) {
					System.out.println(variant.getContig() + " " + variant.getStart());
				}

				referenceVariant.setUsed(true);

				if (variantFile != null) {
					variantFile.setString(VariantFile.CHROMOSOME, variant.getContig());
					variantFile.setInteger(VariantFile.POSITION, variant.getStart());
					variantFile.next();
				}

				float[] dosages = variant.getGenotypeDosages(genotypeFormat);

				int indexSample = 0;
				for (int i = 0; i < countSamples; i++) {
					String sample = vcfReader.getGenotypedSamples().get(i);
					if (samplesFile == null || samplesFile.contains(sample)) {
						float dosage = dosages[i];
						if (dosage >= 0) {
							double effect = dosage * effectWeight;
							riskScores.get(indexSample).incScore(j, effect);
							indexSample++;
						}
					}
				}

				summary.incVariantsUsed();

			}
		}

		vcfReader.close();

		// System.out.println("Loaded " + getRiskScores().length + " samples and " +
		// countVariants + " variants.");

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

	/*
	 * public RiskScore[] getRiskScores() { return riskScores.toArray(new
	 * RiskScore[0]); }
	 */

	public RiskScoreSummary[] getSummaries() {
		return summaries;
	}

	public int getCountVariants() {
		return countVariants;
	}

	public String getOutput() {
		return output;
	}
}
