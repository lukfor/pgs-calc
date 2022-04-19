package genepi.riskscore.model;

public class Sample {

	private String sample;

	private String chromosome;

	private double[] scores;

	public Sample(String chromosome, String sample, int numberOfScores) {
		this.chromosome = chromosome;
		this.sample = sample;
		this.scores = new double[numberOfScores];
		for (int i = 0; i < scores.length; i++) {
			this.scores[i] = 0;
		}
	}

	public void setScore(int index, double score) {
		this.scores[index] = score;
	}
	
	public void incScore(int index, double score) {
		this.scores[index] += score;
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
