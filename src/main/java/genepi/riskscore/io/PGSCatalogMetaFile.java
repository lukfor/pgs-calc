package genepi.riskscore.io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import genepi.io.text.LineWriter;
import genepi.riskscore.io.MetaFile.MetaScore;
import genepi.riskscore.model.ScorePopulationMap;

public class PGSCatalogMetaFile {

	protected Map<String, MetaScore> index;

	protected String next;

	private PGSCatalogMetaFile() {

	}

	public static PGSCatalogMetaFile load(String filename)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		PGSCatalogMetaFile file = new PGSCatalogMetaFile();
		// file.data
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map>>() {
		}.getType();
		Object data = gson.fromJson(new FileReader(filename), Object.class);

		Object results = ((AbstractMap<String, Object>) data).get("results");
		if (((AbstractMap<String, Object>) data).get("next") != null) {
			file.next = ((AbstractMap<String, Object>) data).get("next").toString();
		} else {
			file.next = null;
		}
		List list = (List) results;

		file.index = new HashMap<String, MetaScore>();
		for (Object score : list) {
			MetaScore metaScore = parsePGSCatalog((AbstractMap<String, Object>) score);
			file.index.put(metaScore.getId(), metaScore);
		}
		return file;
	}

	public void merge(PGSCatalogMetaFile metaFile) {
		index.putAll(metaFile.index);
	}

	public int getScores() {
		return index.size();
	}

	public String getNext() {
		return next;
	}

	public void save(String filename) throws IOException {
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map>>() {
		}.getType();
		String json = gson.toJson(index);
		LineWriter writer = new LineWriter(filename);
		writer.write(json);
		writer.close();

	}

	protected static MetaScore parsePGSCatalog(AbstractMap<String, Object> data) {
		MetaScore score = new MetaScore();
		String id = data.get("id").toString();
		String trait = data.get("trait_reported").toString();
		score.setId(id);
		score.setTrait(trait);

		Object ancestry_distribution = data.get("ancestry_distribution");
		if (ancestry_distribution != null) {
			Object gwas = ((Map<String, Object>) ancestry_distribution).get("gwas");
			if (gwas != null) {
				Object samplesVariants = ((Map<String, Object>) gwas).get("dist");

				if (samplesVariants != null) {
					ScorePopulationMap populationMap = new ScorePopulationMap();
					Map<String, Double> items = (Map<String, Double>) samplesVariants;
					for (String key : items.keySet()) {
						populationMap.addSamples(key, items.get(key).floatValue() / 100.0f);
					}
					score.setPopulations(populationMap);
					Double samples = (Double) ((Map<String, Object>) gwas).get("count");
					populationMap.total = samples.intValue();
					score.setSamples(samples.intValue());
				}

			}
		}

		Object publication = data.get("publication");
		if (publication != null) {
			Map<String, String> publicationMap = (Map<String, String>) publication;
			Map<String, String> map = new HashMap<String, String>();
			map.put("doi", publicationMap.get("doi"));
			map.put("firstauthor", publicationMap.get("firstauthor"));
			map.put("journal", publicationMap.get("journal"));
			map.put("date", publicationMap.get("date_publication"));
			score.setPublication(map);			
		}
		
		score.setVariants(((Double) data.get("variants_number")).intValue());
		score.setRepository("PGS-Catalog");
		score.setLink("https://www.pgscatalog.org/score/" + id);

		return score;
	}

}
