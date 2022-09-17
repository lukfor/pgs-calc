package genepi.riskscore.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PopulationMap {

	private Map<String, Population> items = new HashMap<String, Population>();

	public int total = 0;
	
	public void addSample(String population) {
		addSamples(population, 1);
	}
	
	public void addSamples(String population, int count) {
		Population item = items.get(population);
		if (item == null) {
			item = new Population();
			item.setName(population);
			item.setCount(0);
			items.put(population, item);
		}
		item.setCount(item.getCount() + count);
		this.total += count;
		for ( Population _item: items.values()) {
			_item.setPercentage(_item.getCount() / (float) total);
		}
	}

	public List<Population> getPopulations() {
		List<Population> result = new Vector<Population>();
		for ( Population item: items.values()) {
			result.add(item);
		}
		Collections.sort(result);
		return result;
	}

	public int getTotal() {
		return total;
	}
	
}
