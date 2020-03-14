package genepi.riskscore.model;

public class RiskScore {

	private String sample;
	
	private String chromosome;

	private float score = 0;

	public RiskScore(String chromosome, String sample) {
		this.chromosome = chromosome;
		this.sample = sample;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	public float getScore() {
		return score;
	}

	public String getSample() {
		return sample;
	}
	
	public String getChromosome() {
		return chromosome;
	}

	@Override
	public String toString() {
		return sample + ": " + score;
	}
}
