package genepi.riskscore.model;

import java.text.DecimalFormat;

public class RiskScoreSummary {

	private String name;

	private int variants = 0;

	private int variantsUsed = 0;

	private int variantsSwitched = 0;

	private int variantsMultiAllelic = 0;

	private int variantsAlleleMissmatch = 0;

	private int r2Filtered = 0;

	private int notFound = 0;

	private int filtered = 0;

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

	public void setVariants(int count) {
		this.variants = count;
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

	public int getVariantsNotUsed() {
		return (variants - variantsUsed);
	}

	public void merge(RiskScoreSummary other) throws Exception {
		if (!other.name.equals(name)) {
			throw new Exception("Different score names: '" + name + "' vs. '" + other.name + "'.");
		}

		// total variants has to be ignored.
		variantsUsed += other.variantsUsed;
		variantsSwitched += other.variantsSwitched;
		variantsMultiAllelic += other.variantsMultiAllelic;
		variantsAlleleMissmatch += other.variantsAlleleMissmatch;
		r2Filtered += other.r2Filtered;
		notFound += other.notFound;
		filtered += other.filtered;

	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		buffer.append("  " + name + ":\n");
		buffer.append("    - Variants: " + number(getVariants()) + "\n");
		buffer.append("    - Variants used: " + number(getVariantsUsed()) + " ("
				+ percentage(getVariantsUsed(), getVariants()) + ")\n");
		buffer.append("    - Found in target and filtered by:\n");
		buffer.append("      - allele mismatch: " + number(getAlleleMissmatch()) + "\n");
		buffer.append("      - multi allelic or indels: " + number(getMultiAllelic()) + "\n");
		buffer.append("      - low R2 value: " + number(getR2Filtered()) + "\n");
		buffer.append("      - variants file: " + number(getFiltered()) + "\n");

		int notFound = getVariants()
				- (getVariantsUsed() + getFiltered() + getAlleleMissmatch() + getMultiAllelic() + getR2Filtered());

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

}
