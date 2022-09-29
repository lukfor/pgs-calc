package genepi.riskscore.io.formats;

public class PRSwebFormat extends RiskScoreFormatImpl {

	@Override
	public String getChromosome() {
		return "CHROM";
	}

	@Override
	public String getPosition() {
		return "POS";
	}

	@Override
	public String getEffectAllele() {
		return "EA";
	}

	@Override
	public String getEffectWeight() {
		return "WEIGHT";
	}

	@Override
	public String getOtherAllele() {
		return "OA";
	}

	@Override
	public boolean hasRsIds() {
		return false;
	}
	
	public String toString() {
		return "PRSweb format";
	}
}
