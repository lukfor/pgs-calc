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
	public String getEffect_allele() {
		return "effect_allele";
	}

	@Override
	public String getEffect_weight() {
		return "effect_weight";
	}

	@Override
	public String getAllele_a() {
		return "effect_allele";
	}

	@Override
	public String getAllele_b() {
		return "reference_allele";
	}

}
