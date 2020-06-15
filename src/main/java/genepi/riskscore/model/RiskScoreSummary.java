package genepi.riskscore.model;

import java.text.DecimalFormat;

public class RiskScoreSummary {

	private String name;

	private int variants = 0;

	private int variantsUsed = 0;

	private int variantsSwitched = 0;

	private int variantsMultiAllelic = 0;

	private int variantsAlleleMissmatch = 0;

	private int R2Filtered = 0;

	private int NotFound = 0;

	private int Filtered = 0;

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
		return R2Filtered;
	}

	public void incR2Filtered() {
		this.R2Filtered++;
	}

	public int getVariants() {
		return variants;
	}

	public void setVariants(int count) {
		this.variants = count;
	}

	public int getNotFound() {
		return NotFound;
	}

	public void incNotFound() {
		this.NotFound++;
	}

	public int getFiltered() {
		return Filtered;
	}

	public void incFiltered() {
		this.Filtered++;
	}

	public int getVariantsNotUsed() {
		return (variants - variantsUsed);
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
