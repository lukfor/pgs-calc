package genepi.riskscore.model;

public class Population implements Comparable<Population> {

	public String name;

	public int count = 0;

	public float percentage = 0;

	public String color;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		color = getColor();
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

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {

		switch (name) {

		case "AFR":
			return "#FF6600";

		case "EUR":
			return "#0099E6";

		case "GME":
			return "#DBEE06";

		case "SAS":
			return "#F90026";

		case "EAS":
			return "#FF99E6";

		case "Oceania":
			return "#339933";
		case "Oceanian":
			return "#339933";

		case "AMR":
			return "#800080";

		}

		return "#B5B5B5";
	}

	@Override
	public int compareTo(Population o) {
		return -Float.compare(percentage, o.getPercentage());
	}

}
