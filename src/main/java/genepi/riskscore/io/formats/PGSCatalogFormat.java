package genepi.riskscore.io.formats;

import genepi.riskscore.model.RiskScoreFormat;

public class PGSCatalogFormat extends RiskScoreFormat {

	@Override
	public String getChromosome() {
		return "chr_name";
	}

	@Override
	public String getPosition() {
		return "chr_position";
	}

	@Override
	public String getEffectAllele() {
		return "effect_allele";
	}

	@Override
	public String getEffectWeight() {
		return "effect_weight";
	}

	@Override
	public String getOtherAllele() {
		return "reference_allele";
	}

}
