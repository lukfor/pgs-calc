package genepi.riskscore.model;

public class ReferenceVariant {

	private float effectWeight;

	private char otherAllele;

	private char effectAllele;

	private boolean used = false;

	public ReferenceVariant(char otherAllele, char effectAllele, float effectWeight) {
		this.otherAllele = otherAllele;
		this.effectAllele = effectAllele;
		this.effectWeight = effectWeight;
	}

	public float getEffectWeight() {
		return effectWeight;
	}

	public char getOtherAllele() {
		return otherAllele;
	}

	public char getEffectAllele() {
		return effectAllele;
	}

	public boolean isEffectAllele(char allele) {
		return (effectAllele == allele);
	}

	public boolean hasAllele(char allele) {
		return ((otherAllele == allele) || (effectAllele == allele));
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public boolean isUsed() {
		return used;
	}

}
