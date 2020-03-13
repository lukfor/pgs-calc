package genepi.riskscore.model;

public class RiskScore {

	private String sample;

	private float score = 0;

	public RiskScore(String sample) {
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

	@Override
	public String toString() {
		return sample + ": " + score;
	}
}
