package genepi.riskscore.model;

public class Population implements Comparable<Population> {

	private String name;

	public int count = 0;

	public float percentage = 0;

	public String color;
	
	public String label;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		color = getColor();
		label = getLabel();
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

	public void updateColorAndLabel() {
		color = getColor();
		label = getLabel();
	}
	
	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {

		switch (name) {

		case "AFR":
			return "#FF6600";

		case "ASN":
			//TODO: update
			return "#999999";			
			
		case "EUR":
			return "#0099E6";

		case "GME":
			return "#DBEE06";

		case "SAS":
			return "#F90026";

		case "EAS":
			return "#FF99E6";

		case "OTH":
			return "#339933";

		case "AMR":
			return "#800080";
		
			// TODO: udpate colors 			
		case "MAE":
			return "eeeeee";

		case "MAO":
			return "333333";
			
		}

		return "#B5B5B5";
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		switch (name) {

		case "AFR":
			return "African";

		case "ASN":
			return "Additional Asian Ancestries";
			
		case "EUR":
			return "European";

		case "GME":
			return "Greater Middle Eastern";

		case "SAS":
			return "South Asian";

		case "EAS":
			return "East Asian";

		case "OTH":
			return "Additional Diverse Ancestries";
		
		case "AMR":
			return "Hispanic or Latin American";

		case "MAE":
			return "Multi-Ancestry (including Europeans)";

		case "MAO":
			return "Multi-Ancestry (excluding Europeans)";

			
		}
		
		return "Unknown";

	}
	
	@Override
	public int compareTo(Population o) {
		return -Float.compare(percentage, o.getPercentage());
	}

}
