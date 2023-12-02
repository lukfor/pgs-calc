package genepi.riskscore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.*;
import genepi.riskscore.io.scores.MergedRiskScoreCollection;
import genepi.riskscore.io.formats.RiskScoreFormatFactory;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.io.scores.IRiskScoreCollection;
import genepi.riskscore.io.scores.RiskScoreCollection;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScore;
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

	private String outputReportFilename = null;

	private RiskScoreFormat defaultFormat = RiskScoreFormat.AUTO_DETECT;

	private Map<String, RiskScoreFormat> formats = new HashMap<String, RiskScoreFormat>();

	private String genotypeFormat = DOSAGE_FORMAT;

	private String output;

	private String outputEffectsFilename;

	private String dbsnp = null;

	private String proxies;

	private boolean fixStrandFlips = false;

	private boolean removeAmbiguous = false;

	private boolean inverseDosage = false;

	private boolean averaging = false;

	private IRiskScoreCollection collection;
	
	public static final String INFO_R2 = "R2";

	public static final String DOSAGE_FORMAT = "DS";

	public static boolean VERBOSE = false;

	public static final Map<Character, Character> ALLELE_SWITCHES = new HashMap<Character, Character>();

	static {
		ALLELE_SWITCHES.put('A', 'T');
		ALLELE_SWITCHES.put('T', 'A');
		ALLELE_SWITCHES.put('G', 'C');
		ALLELE_SWITCHES.put('C', 'G');
	}

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

	public void setOutputReportFilename(String outputReportFilename) {
		this.outputReportFilename = outputReportFilename;
	}

	public void setOutputEffectsFilename(String outputEffectsFilename) {
		this.outputEffectsFilename = outputEffectsFilename;
	}

	public void setDbSnp(String dbsnp) {
		this.dbsnp = dbsnp;
	}

	public void setProxies(String proxies) {
		this.proxies = proxies;
	}

	public void run(ITaskMonitor monitor) throws Exception {

		if (vcf == null || vcf.isEmpty()) {
			throw new Exception("Please specify a vcf file.");
		}

		if (output == null || output.isEmpty()) {
			throw new Exception("Please specify a output filename.");
		}

		try {

			// read chromosome from first variant
			String chromosome = null;
			FastVCFFileReader vcfReader = new FastVCFFileReader(vcf);
			if (vcfReader.next()) {
				chromosome = vcfReader.get().getContig();
				vcfReader.close();
			} else {
				vcfReader.close();
				throw new Exception("VCF file is empty.");
			}

			String taskName = "[Chr " + (chromosome.length() == 1 ? "0" : "") + chromosome + "]";
			monitor.begin(taskName, new File(vcf).length());
			monitor.worked(0);

			if (riskScoreFilenames == null || riskScoreFilenames.length == 0){
				throw new Exception("Reference score or collection can not be null or empty.");
			}

			//TODO "## PGS-Collection v1"
			if (riskScoreFilenames.length == 1 && new File(riskScoreFilenames[0]).exists() && RiskScoreFormatFactory.readHeader(riskScoreFilenames[0]).startsWith("#Date=")) {
				collection = new MergedRiskScoreCollection(riskScoreFilenames[0]);
			} else {
				collection = new RiskScoreCollection(riskScoreFilenames, formats);
			}

			collection.buildIndex(chromosome, chunk, dbsnp, proxies);

			processVCF(monitor, chromosome, vcf, collection);

			OutputFileWriter outputFile = new OutputFileWriter(riskScores, collection.getSummaries());
			outputFile.save(output);

			if (outputReportFilename != null) {
				ReportFile reportFile = new ReportFile(collection.getSummaries());
				reportFile.save(outputReportFilename);
			}

			monitor.done();
		} catch (Exception e) {
			if (VERBOSE) {
				System.out.println("ERROR:");
				e.printStackTrace();
			}
			throw e;
		} catch (Error e) {
			if (VERBOSE) {
				System.out.println("ERROR:");
				e.printStackTrace();
			}
			throw new Exception(e);
		}

	}

	private void processVCF(ITaskMonitor monitor, String chromosome, String vcfFilename,IRiskScoreCollection collection) throws Exception {

		debug("Loading file " + vcfFilename + "...");

		VariantFile includeVariants = null;
		if (includeVariantFilename != null) {
			debug("Loading file " + includeVariantFilename + "...");
			includeVariants = new VariantFile(includeVariantFilename);
			includeVariants.buildIndex(chromosome);
			debug("Loaded " + includeVariants.getCacheSize() + " variants for chromosome " + chromosome);
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
				RiskScore riskScore = new RiskScore(chromosome, sample, collection.getSize());
				riskScores.add(riskScore);
			}
		}

		boolean outOfChunk = false;

		CsvTableWriter variantsWriter = null;
		if (outputVariantFilename != null) {
			variantsWriter = new CsvTableWriter(outputVariantFilename, VariantFile.SEPARATOR);
			variantsWriter.setColumns(new String[] { VariantFile.SCORE, VariantFile.CHROMOSOME, VariantFile.POSITION,
					VariantFile.R2, VariantFile.WEIGHT, VariantFile.INCLUDE });
		}

		CsvTableWriter effectsWriter = null;
		if (outputEffectsFilename != null) {
			effectsWriter = new CsvTableWriter(outputEffectsFilename, MergeEffectsTask.EFFECTS_FILE_SEPARATOR);
			effectsWriter.setColumns(
					new String[] { "score", "sample", VariantFile.CHROMOSOME, VariantFile.POSITION, "effect" });
		}
		
		int proxy = 0;

		while (vcfReader.next() && !outOfChunk && !collection.isEmpty()) {

			if (monitor.isCanceled()) {
				return;
			}

			MinimalVariantContext variant = vcfReader.get();

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
			for (int j = 0; j < collection.getSize(); j++) {

				RiskScoreSummary summary = collection.getSummaries()[j];

				boolean isPartOfRiskScore = collection.contains(j, position);

				if (!isPartOfRiskScore) {
					summary.incNotFound();
					continue;
				}

				if (includeVariants != null) {
					if (!includeVariants.contains(summary.getName(), position)) {
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
				ReferenceVariant referenceVariant = collection.getVariant(j, position);

				float effectWeight = referenceVariant.getEffectWeight();
				String referenceAllele = variant.getReferenceAllele();
				// ignore deletions
				if (variant.getAlternateAllele().length() == 0) {
					summary.incMultiAllelic();
					continue;
				}

				String[] alternateAlleles = variant.getAlternateAllele().split(",");

				if (alternateAlleles.length > 1) {
					summary.incMultiAllelic();
					continue;
				}

				String alternateAllele = alternateAlleles[0];

				// remove Ambiguous SNPs (AC, GT)
				if (removeAmbiguous && variant.isAmbigous()) {
					summary.incAmbiguous();
					continue;
				}

				// check if alleles (ref and alt) are present
				if (!referenceVariant.hasAllele(referenceAllele) || !referenceVariant.hasAllele(alternateAllele)) {

					if (!fixStrandFlips) {
						summary.incAlleleMissmatch();
						continue;
					}

					String flippedReferenceAllele = flip(referenceAllele);
					String flippedAlternateAllele = flip(alternateAllele);
					if (variant.isAmbigous() && (!referenceVariant.hasAllele(flippedReferenceAllele)
							|| !referenceVariant.hasAllele(flippedAlternateAllele))) {
						summary.incAlleleMissmatch();
						continue;
					} else {
						referenceAllele = flippedReferenceAllele;
						alternateAllele = flippedAlternateAllele;
						summary.incFlipped();
					}

				}

				// check if alleles are switched and update effect weight (effect_allele !=
				// alternate_allele)
				boolean switched = false;
				if (!referenceVariant.isEffectAllele(alternateAllele)) {
					if (referenceVariant.isEffectAllele(referenceAllele)) {
						effectWeight = -effectWeight;
						switched = true;
						summary.incSwitched();
					} else {
						summary.incAlleleMissmatch();
						continue;
					}
				}

				if (referenceVariant.isUsed()) {
					//System.out.println(variant + " ignored ");
					continue;
				}

				if (!averaging && variant.hasMissingGenotypes(genotypeFormat)) {
					summary.incMissingGenotypes();
					continue;
				}
				/*if (referenceVariant.getParent() == null) {
					System.out.println(variant);
				} else {
					proxy++;
					System.out.println(variant + " --> proxy --> ");
				}*/
				
				referenceVariant.setUsed(true);

				if (variantsWriter != null) {
					variantsWriter.setString(VariantFile.SCORE, summary.getName());
					variantsWriter.setString(VariantFile.CHROMOSOME, variant.getContig());
					variantsWriter.setInteger(VariantFile.POSITION, variant.getStart());
					variantsWriter.setDouble(VariantFile.R2, variant.getInfoAsDouble(INFO_R2, 0));
					variantsWriter.setDouble(VariantFile.WEIGHT, referenceVariant.getEffectWeight());
					variantsWriter.setInteger(VariantFile.INCLUDE, 1);
					variantsWriter.next();
				}

				float[] dosages = variant.getGenotypeDosages(genotypeFormat);

				int indexSample = 0;
				for (int i = 0; i < countSamples; i++) {
					String sample = vcfReader.getGenotypedSamples().get(i);
					if (samplesFile == null || samplesFile.contains(sample)) {
						float dosage = dosages[i];
						if (dosage >= 0) {
							double effect = 0;
							if (inverseDosage && switched) {
								effect = (2 - dosage) * -effectWeight;
							} else {
								effect = dosage * effectWeight;
							}
							riskScores.get(indexSample).incScore(j, effect);
							indexSample++;
							if (effectsWriter != null) {
								effectsWriter.setString("score", summary.getName());
								effectsWriter.setString("sample", sample);
								effectsWriter.setString(VariantFile.CHROMOSOME, variant.getContig());
								effectsWriter.setInteger(VariantFile.POSITION, variant.getStart());
								effectsWriter.setDouble("effect", effect);
								effectsWriter.next();
							}
						}
					}
				}

				summary.incVariantsUsed();

			}
		}

		if (variantsWriter != null) {

			// write all unused variants to file!
			for (int j = 0; j < collection.getSize(); j++) {
				RiskScoreSummary summary = collection.getSummaries()[j];
				for (Entry<Integer, ReferenceVariant> item : collection.getAllVariants(j)) {
					ReferenceVariant variant = item.getValue();
					int position = item.getKey();

					if (chunk != null) {

						if (position < chunk.getStart()) {
							continue;
						}

						if (position > chunk.getEnd()) {
							outOfChunk = true;
							continue;
						}

					}

					if (!variant.isUsed()) {
						variantsWriter.setString(VariantFile.SCORE, summary.getName());
						variantsWriter.setString(VariantFile.CHROMOSOME, chromosome);
						variantsWriter.setInteger(VariantFile.POSITION, position);
						variantsWriter.setString(VariantFile.R2, "");
						variantsWriter.setDouble(VariantFile.WEIGHT, variant.getEffectWeight());
						variantsWriter.setInteger(VariantFile.INCLUDE, 0);
						variantsWriter.next();

					}
				}
			}

			variantsWriter.close();
		}

		if (effectsWriter != null) {
			effectsWriter.close();
		}

		vcfReader.close();
		debug("Used " + proxy + " proxies");
		debug("Loaded " + countSamples + " samples and " + countVariants + " variants.");

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

	public RiskScoreSummary[] getSummaries() {
		return collection.getSummaries();
	}

	int getCountVariants() {
		return countVariants;
	}

	public String getOutput() {
		return output;
	}

	public String getOutputReportFilename() {
		return outputReportFilename;
	}

	public String getOutputEffectsFilename() {
		return outputEffectsFilename;
	}

	public String getOutputVariantFilename() {
		return outputVariantFilename;
	}

	public void debug(String text) {
		if (VERBOSE) {
			System.out.println(text);
		}
	}

	public void setFixStrandFlips(boolean fixStrandFlips) {
		this.fixStrandFlips = fixStrandFlips;
	}

	public void setRemoveAmbiguous(boolean removeAmbiguous) {
		this.removeAmbiguous = removeAmbiguous;
	}

	protected static String flip(String allele) {
		String flippedAllele = "";
		for (int i = 0; i < allele.length(); i++) {
			Character flipped = ALLELE_SWITCHES.get(allele.charAt(i));
			flippedAllele += flipped;
		}
		return flippedAllele;
	}

	public void setInverseDosage(boolean inverseDosage) {
		this.inverseDosage = inverseDosage;
	}

}