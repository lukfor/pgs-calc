package genepi.riskscore.model;

public class ReferenceVariant {

	private float effectWeight;

	private char alleleA;

	private char alleleB;

	private char effectAllele;

	public ReferenceVariant(char alleleA, char alleleB, char effectAllele, float effectWeight) {
		this.alleleA = alleleA;
		this.alleleB = alleleB;
		this.effectAllele = effectAllele;
		this.effectWeight = effectWeight;
	}

	public float getEffectWeight() {
		return effectWeight;
	}

	public char getAlleleA() {
		return alleleA;
	}

	public char getAlleleB() {
		return alleleB;
	}

	public char getEffectAllele() {
		return effectAllele;
	}

	public boolean isEffectAllele(char allele) {
		return (effectAllele == allele);
	}

}
