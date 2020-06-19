package genepi.riskscore.model;

public class RiskScore {

	private String sample;
	
	private String chromosome;

	private double[] scores;

	public RiskScore(String chromosome, String sample, int numberOfScores) {
		this.chromosome = chromosome;
		this.sample = sample;
		this.scores = new double[numberOfScores];
	}

	public void setScore(int index, double score) {
		this.scores[index] = score;
	}
	
	public double getScore(int index) {
		return scores[index];
	}

	public String getSample() {
		return sample;
	}
	
	public String getChromosome() {
		return chromosome;
	}

	@Override
	public String toString() {
		return sample + ": " + scores;
	}
}
