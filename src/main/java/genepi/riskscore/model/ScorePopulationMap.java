package genepi.riskscore.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ScorePopulationMap {

	private Map<String, Population> items = new HashMap<String, Population>();

	public int total = 0;

	public void addSample(String population) {
		addSamples(population, 1);
	}

	public void addSamples(String population, float percentage) {
		Population item = items.get(population);
		item = new Population();
		item.setName(population);
		item.setCount(-1);
		item.setPercentage(percentage);
		items.put(population, item);

	}

	public List<Population> getPopulations() {
		List<Population> result = new Vector<Population>();
		for (Population item : items.values()) {
			result.add(item);
		}
		Collections.sort(result);
		return result;
	}

	public int getTotal() {
		return total;
	}

	public boolean supports(String population) {
		return items.containsKey(population);
	}

	public boolean supports(Population population) {
		return items.containsKey(population.getName());
	}

	public void updateColorAndLabel() {
		for (Population item: items.values()) {
			item.updateColorAndLabel();
		}
	}
	
}
