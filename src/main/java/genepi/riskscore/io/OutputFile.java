package genepi.riskscore.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class OutputFile {

	private List<String> samples;

	private List<double[]> data;

	private List<String> scores;

	private Map<String, Integer> scoresIndex = new HashMap<String, Integer>();
	
	public OutputFile(String filename) throws IOException {

		OutputFileReader outputFile = new OutputFileReader(filename);

		scores = outputFile.getScores();
		int index = 0;
		for (String score: scores) {
			scoresIndex.put(score, index);
			index++;
		}

		samples = new Vector<String>();
		data = new Vector<double[]>();
		while (outputFile.next()) {
			samples.add(outputFile.getSample());
			double[] values = Arrays.copyOf(outputFile.getValues(), outputFile.getValues().length);
			data.add(values);
		}
		outputFile.close();

	}

	public int getCountSamples() {
		return samples.size();
	}

	public List<String> getSamples() {
		return samples;
	}

	public int getCountScores() {
		return scores.size();
	}

	public List<String> getScores() {
		return scores;
	}
	
	public double getValue(int score, int sample) {
		return data.get(sample)[score];
	}

	public double[] getValuesByScore(String score) {
		int scoreIndex = scoresIndex.get(score);
		double[] values = new double[samples.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = getValue(scoreIndex, i);
		}
		return values;
	}
	
}
