package genepi.riskscore.model;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import genepi.riskscore.io.MetaFile.MetaScore;
import genepi.riskscore.io.SamplesFile;

public class RiskScoreSummary {

	private String name;

	private int variants = 0;

	private int variantsUsed = 0;

	private int variantsIgnored = 0;

	private double coverage = 0;

	private int variantsSwitched = 0;

	private int variantsMultiAllelic = 0;

	private int variantsAlleleMissmatch = 0;

	private int r2Filtered = 0;

	private int notFound = 0;

	private int filtered = 0;

	private int ambiguous = 0;

	private int flipped = 0;

	private int strandFlips = 0;

	private int proxiesUsed = 0;

	private MetaScore meta;

	private double[] data;

	private String coverageLabel;

	private int missingGenotypes = 0;

	private String populationCheckMessage = "";

	private boolean populationCheckStatus = true;

	private double[] excluded;

	private int samples;

	private float samplesPercentage;

	public RiskScoreSummary(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getVariantsUsed() {
		return variantsUsed;
	}

	public void incVariantsUsed() {
		this.variantsUsed++;
		coverage = getVariantsUsed() / getVariants();
	}

	public int getSwitched() {
		return variantsSwitched;
	}

	public void incSwitched() {
		this.variantsSwitched++;
	}

	public int getMultiAllelic() {
		return variantsMultiAllelic;
	}

	public void incMultiAllelic() {
		this.variantsMultiAllelic++;
	}

	public int getAlleleMissmatch() {
		return variantsAlleleMissmatch;
	}

	public void incAlleleMissmatch() {
		this.variantsAlleleMissmatch++;
	}

	public int getR2Filtered() {
		return r2Filtered;
	}

	public void incR2Filtered() {
		this.r2Filtered++;
	}

	public int getVariants() {
		return variants;
	}

	public void incVariants() {
		this.variants++;
		coverage = getVariantsUsed() / getVariants();
	}

	public void setVariants(int count) {
		this.variants = count;
		coverage = getVariantsUsed() / getVariants();
	}

	public int getNotFound() {
		return notFound;
	}

	public void incNotFound() {
		this.notFound++;
	}

	public int getFiltered() {
		return filtered;
	}

	public void incFiltered() {
		this.filtered++;
	}

	public int getAmbiguous() {
		return ambiguous;
	}

	public void incAmbiguous() {
		ambiguous++;
	}

	public int getVariantsNotUsed() {
		return (variants - variantsUsed);
	}

	public void setCoverage(double coverage) {
		// nothing to do. needed for gson.
	}

	public double getCoverage() {
		return coverage;
	}

	public void setMeta(MetaScore meta) {
		this.meta = meta;
	}

	public MetaScore getMeta() {
		return meta;
	}

	public void setData(double[] data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setCoverageLabel(String coverageLabel) {
		// nothing to do. needed for gson.
	}

	public String getCoverageLabel() {
		return coverageLabel;
	}

	public void setVariantsIgnored(int variantsIgnored) {
		this.variantsIgnored = variantsIgnored;
	}

	public int getVariantsIgnored() {
		return variantsIgnored;
	}

	public int getFlipped() {
		return flipped;
	}

	public void setFlipped(int flipped) {
		this.flipped = flipped;
	}

	public void incFlipped() {
		flipped++;
	}

	public int getStrandFlips() {
		return strandFlips;
	}

	public void setStrandFlips(int strandFlips) {
		this.strandFlips = strandFlips;
	}

	public void incStrandFlips() {
		this.strandFlips++;
	}

	public void setProxiesUsed(int proxiesUsed) {
		this.proxiesUsed = proxiesUsed;
	}

	public int getProxiesUsed() {
		return proxiesUsed;
	}

	public void incProxiesUsed() {
		this.proxiesUsed++;
	}

	public int getMissingGenotypes() {
		return missingGenotypes;
	}

	public void setMissingGenotypes(int missingGenotypes) {
		this.missingGenotypes = missingGenotypes;
	}

	public void incMissingGenotypes() {
		this.missingGenotypes++;
	}

	public void setPopulationCheckMessage(String populationCheckMessage) {
		this.populationCheckMessage = populationCheckMessage;
	}

	public String getPopulationCheckMessage() {
		return populationCheckMessage;
	}

	public void setPopulationCheckStatus(boolean populationCheckStatus) {
		this.populationCheckStatus = populationCheckStatus;
	}

	public boolean isPopulationCheckStatus() {
		return populationCheckStatus;
	}

	public void checkPopulation(List<String> samples, SamplesFile file) {

		populationCheckStatus = true;

		if (file == null) {
			populationCheckMessage = "No population information available for your samples. Be aware of possible population stratification.";
			populationCheckStatus = false;
			return;
		}

		if (this.meta == null || this.meta.getPopulations() == null || this.meta.getPopulations().getTotal() == 0) {
			populationCheckMessage = "No population information available for this score. Be aware of possible population stratification.";
			populationCheckStatus = false;
			return;
		}

		// excluded = new Vector<>();

		populationCheckMessage = "";
		Set<String> excludedSamples = new HashSet<String>();
		for (Population pop : file.getPopulations().getPopulations()) {
			// TODO: support names with ,... split...
			// TODO: detected instead of excluded?
			if (!meta.getPopulations().supports(pop)) {
				populationCheckMessage += "Excluded <b>" + pop.getCount() + " sample(s)</b> (" + pop.getLabel()
						+ ") due to ancestry mismatch.<br>";
				populationCheckStatus = false;
				excludedSamples.addAll(file.getSamples(pop));
			}
		}

		if (data == null) {
			return;
		}

		if (populationCheckStatus == false && !excludedSamples.isEmpty()) {
			double[] oldData = data;
			data = new double[oldData.length - excludedSamples.size()];
			excluded = new double[excludedSamples.size()];
			int dataIndex = 0;
			int excludedIndex = 0;
			for (int i = 0; i < oldData.length; i++) {
				String sample = samples.get(i);
				if (excludedSamples.contains(sample)) {
					excluded[excludedIndex] = oldData[i];
					excludedIndex++;
				} else {
					data[dataIndex] = oldData[i];
					dataIndex++;
				}
			}
		}

		this.samples = data.length;
		this.samplesPercentage = this.samples / (float) samples.size();
	}

	public double[] getExcluded() {
		return excluded;
	}

	public void setExcluded(double[] excluded) {
		this.excluded = excluded;
	}

	public int getSamples() {
		return samples;
	}

	public void setSamples(int samples) {
		this.samples = samples;
	}

	public float getSamplesPercentage() {
		return samplesPercentage;
	}

	public void setSamplesPercentage(float samplesPercentage) {
		this.samplesPercentage = samplesPercentage;
	}

	public void updateStatistics() {
		coverage = (double) getVariantsUsed() / (double) getVariants();

		if (getVariantsUsed() == 0) {
			coverageLabel = "zero";
		} else if (coverage <= 0.25) {
			coverageLabel = "low";
		} else if (coverage > 0.25 && coverage <= 0.75) {
			coverageLabel = "medium";
		} else {
			coverageLabel = "high";
		}
	}

	public void merge(RiskScoreSummary other) throws Exception {
		if (!other.name.equals(name)) {
			throw new Exception("Different score names: '" + name + "' vs. '" + other.name + "'.");
		}

		// total variants has to be ignored.
		variantsUsed += other.variantsUsed;
		variantsIgnored += other.variantsIgnored;
		variantsSwitched += other.variantsSwitched;
		variantsMultiAllelic += other.variantsMultiAllelic;
		variantsAlleleMissmatch += other.variantsAlleleMissmatch;
		r2Filtered += other.r2Filtered;
		notFound += other.notFound;
		filtered += other.filtered;
		ambiguous += other.ambiguous;
		flipped += other.flipped;
		strandFlips += other.strandFlips;
		missingGenotypes += other.missingGenotypes;
		updateStatistics();
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		buffer.append("  " + name + ":\n");
		buffer.append("    - Variants: " + number(getVariants()) + "\n");
		buffer.append("    - Variants ignored: " + number(getVariantsIgnored()) + "\n");
		buffer.append("    - Variants used: " + number(getVariantsUsed()) + " ("
				+ percentage(getVariantsUsed(), getVariants()) + ")\n");
		buffer.append("    - Proxies used: " + number(getProxiesUsed()) + "\n");
		buffer.append("    - Found in target and filtered by:\n");
		buffer.append("      - ambiguous: " + number(getAmbiguous()) + "\n");
		buffer.append("      - strand flip: " + number(getStrandFlips()) + "\n");
		buffer.append("      - allele mismatch: " + number(getAlleleMissmatch()) + "\n");
		buffer.append("      - multi allelic or indels: " + number(getMultiAllelic()) + "\n");
		buffer.append("      - low R2 value: " + number(getR2Filtered()) + "\n");
		buffer.append("      - missing genotypes: " + number(getMissingGenotypes()) + "\n");
		buffer.append("      - variants file: " + number(getFiltered()) + "\n");
		buffer.append("    - Strand Flips fixed: " + number(getFlipped()) + "\n");

		int notFound = getVariants() - (getVariantsUsed() + getFiltered() + getAlleleMissmatch() + getMultiAllelic()
				+ getR2Filtered() + getAmbiguous());

		return buffer.toString();

	}

	public static String number(long number) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		return formatter.format(number);
	}

	public static String percentage(double obtained, double total) {
		double percentage = (obtained / total) * 100;
		DecimalFormat df = new DecimalFormat("###.##'%'");
		return df.format(percentage);
	}

	public void updateColorAndLabel() {
		if (meta != null && meta.getPopulations() != null) {
			meta.getPopulations().updateColorAndLabel();
		}
	}

}
