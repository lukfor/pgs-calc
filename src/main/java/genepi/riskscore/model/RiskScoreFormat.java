package genepi.riskscore.model;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class RiskScoreFormat {

	public static final char SEPARATOR = '\t';

	public static final String CHROMOSOME = "chr";

	public static final String POSITION = "position_hg19";

	public static final String EFFECT_WEIGHT = "effect_weight";

	public static final String ALLELE_A = "A1";

	public static final String ALLELE_B = "A2";

	public static final String EFFECT_ALLELE = "effect_allele";

	private String chromosome = CHROMOSOME;

	private String position = POSITION;

	private String effect_weight = EFFECT_WEIGHT;

	private String allele_a = ALLELE_A;

	private String allele_b = ALLELE_B;

	private String effect_allele = EFFECT_ALLELE;

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getEffect_weight() {
		return effect_weight;
	}

	public void setEffect_weight(String effect_weight) {
		this.effect_weight = effect_weight;
	}

	public String getAllele_a() {
		return allele_a;
	}

	public void setAllele_a(String allele_a) {
		this.allele_a = allele_a;
	}

	public String getAllele_b() {
		return allele_b;
	}

	public void setAllele_b(String allele_b) {
		this.allele_b = allele_b;
	}

	public String getEffect_allele() {
		return effect_allele;
	}

	public void setEffect_allele(String effect_allele) {
		this.effect_allele = effect_allele;
	}

	public static RiskScoreFormat load(String filename)
			throws JsonSyntaxException, JsonIOException, FileNotFoundException {

		Gson gson = new Gson();
		RiskScoreFormat format = gson.fromJson(new FileReader(filename), RiskScoreFormat.class);
		return format;
	}

}
