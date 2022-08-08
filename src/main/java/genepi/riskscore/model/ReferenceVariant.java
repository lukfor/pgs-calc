package genepi.riskscore.model;

public class ReferenceVariant {

	private float effectWeight;

	private String otherAllele;

	private String effectAllele;

	private boolean used = false;

	public ReferenceVariant(String otherAllele, String effectAllele, float effectWeight) {
		this.otherAllele = otherAllele;
		this.effectAllele = effectAllele;
		this.effectWeight = effectWeight;
	}

	public float getEffectWeight() {
		return effectWeight;
	}

	public String getOtherAllele() {
		return otherAllele;
	}

	public String getEffectAllele() {
		return effectAllele;
	}

	public boolean isEffectAllele(String allele) {
		return (effectAllele.equals(allele));
	}

	public boolean hasAllele(String allele) {
		return ((otherAllele.equals(allele)) || (effectAllele.equals(allele)));
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public boolean isUsed() {
		return used;
	}

}
