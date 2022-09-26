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

		case "Africa":
			return "#FF6600";
		case "African":
			return "#FF6600";

		case "Europe":
			return "#0099E6";
		case "European":
			return "#0099E6";

		case "Middle East":
			return "#DBEE06";

		case "C/S Asia":
			return "#F90026";
		case "C/S Asian":
			return "#F90026";

		case "East Asia":
			return "#FF99E6";
		case "East Asian":
			return "#FF99E6";

		case "Oceania":
			return "#339933";
		case "Oceanian":
			return "#339933";

		case "America":
			return "#800080";
		case "American":
			return "#800080";

		case "unkown":
			return "#B5B5B5";
		}

		return "#000000";
	}

	@Override
	public int compareTo(Population o) {
		return -Integer.compare(count, o.getCount());
	}

}
