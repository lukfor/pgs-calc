package genepi.riskscore.io.formats;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class RiskScoreFormatImpl {

	private static final char SEPARATOR = '\t';

	private static final String CHROMOSOME = "chr";
	
	private static final String POSITION = "position_hg19";

	private static final String EFFECT_WEIGHT = "effect_weight";

	private static final String OTHER_ALLELE = "A2";

	private static final String EFFECT_ALLELE = "effect_allele";

	private String chromosome = CHROMOSOME;

	private String position = POSITION;

	private String effect_weight = EFFECT_WEIGHT;

	private String otherAllele = OTHER_ALLELE;

	private String effectAllele = EFFECT_ALLELE;
	
	private char separator = SEPARATOR;

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

	public String getEffectWeight() {
		return effect_weight;
	}

	public void setEffectWeight(String effect_weight) {
		this.effect_weight = effect_weight;
	}

	public void setOtherAllele(String otherAllele) {
		this.otherAllele = otherAllele;
	}

	public String getOtherAllele() {
		return otherAllele;
	}

	public void setEffectAllele(String effectAllele) {
		this.effectAllele = effectAllele;
	}

	public String getEffectAllele() {
		return effectAllele;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	public char getSeparator() {
		return separator;
	}
	
	public static RiskScoreFormatImpl load(String filename)
			throws JsonSyntaxException, JsonIOException, FileNotFoundException {

		Gson gson = new Gson();
		RiskScoreFormatImpl format = gson.fromJson(new FileReader(filename), RiskScoreFormatImpl.class);
		return format;
	}

	public boolean hasRsIds() {
		return false;
	}

}
