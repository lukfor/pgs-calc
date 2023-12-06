package genepi.riskscore.model;

public class ReferenceVariant {

	private float effectWeight;

	private String otherAllele;

	private String effectAllele;

	private boolean used = false;

	private ReferenceVariant parent;

	public ReferenceVariant(String otherAllele, String effectAllele, float effectWeight) {
		this.otherAllele = otherAllele;
		this.effectAllele = effectAllele;
		this.effectWeight = effectWeight;
	}

	public ReferenceVariant(){

	}

	public void setEffectWeight(float effectWeight) {
		this.effectWeight = effectWeight;
	}

	public void setOtherAllele(String otherAllele) {
		this.otherAllele = otherAllele;
	}

	public void setEffectAllele(String effectAllele) {
		this.effectAllele = effectAllele;
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
		if (parent != null) {
			parent.setUsed(used);
		} else {
			this.used = used;
		}
	}

	public boolean isUsed() {
		if (parent != null) {
			return parent.isUsed();
		} else {
			return used;
		}
	}

	public void setParent(ReferenceVariant parent) {
		this.parent = parent;
	}

	public ReferenceVariant getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return effectAllele + "_" + otherAllele + ":" + effectWeight;
	}
}
