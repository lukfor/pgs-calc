package genepi.riskscore.model;

public class Population implements Comparable<Population> {

	private String name;

	private int count = 0;

	private float percentage = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incCount() {
		this.count++;
	}

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	@Override
	public int compareTo(Population o) {
		return -Integer.compare(count, o.getCount());
	}

}
