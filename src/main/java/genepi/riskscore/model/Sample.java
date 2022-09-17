package genepi.riskscore.model;

public class Sample {

	private String id;

	private String population;

	public static String UNKNOWN_POPULATION = "Not Reported";

	public Sample(String id, String population) {
		this.id = id;
		this.population = population;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPopulation() {
		return population;
	}

	public void setPopulation(String population) {
		this.population = population;
	}

}
