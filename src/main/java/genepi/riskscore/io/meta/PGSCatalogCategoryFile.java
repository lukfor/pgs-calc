package genepi.riskscore.io.meta;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import genepi.io.text.LineWriter;
import genepi.riskscore.io.MetaFile.MetaScore;
import genepi.riskscore.model.ScorePopulationMap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class PGSCatalogCategoryFile {

	protected Map<String, List<String>> traits;

	protected String next;

	private PGSCatalogCategoryFile() {

	}

	public static PGSCatalogCategoryFile load(String filename)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		PGSCatalogCategoryFile file = new PGSCatalogCategoryFile();
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

		file.traits = new HashMap<String, List<String>>();
		for (Object score : list) {
			Category category = parsePGSCatalog((AbstractMap<String, Object>) score);
			System.out.println("Process " + category.getLabel());
			for (String trait: category.getTraits()){
				List<String> categories = file.traits.get(trait);
				if (categories == null){
					categories = new Vector<String>();
					file.traits.put(trait, categories);
				}
				if (!categories.contains(category.label)) {
					categories.add(category.label);
				}
			}
		}
		System.out.println("Loaded categories for " + file.traits.size() + " traits.");
		return file;
	}

	public void merge(PGSCatalogCategoryFile metaFile) {
		traits.putAll(metaFile.traits);
	}

	public List<String> getCategories(String trait){
		return traits.get(trait);
	}

	public int getTraits() {
		return traits.size();
	}

	public String getNext() {
		return next;
	}


	protected static Category parsePGSCatalog(AbstractMap<String, Object> data) {
		Category category = new Category();
		String label = data.get("label").toString();
		category.setLabel(label);

		Object efos = data.get("efotraits");
		if (efos != null) {
			List<Map<String, String>> efoTraits = (List<Map<String, String>>) efos;
			for (Map<String, String> efo: efoTraits){
				category.addTrait(efo.get("id"));
			}
		}

		return category;
	}


	public static class Category {

		public String label;

		private List<String> traits = new Vector<String>();

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public void addTrait(String trait){
			traits.add(trait);
		}

		public List<String> getTraits() {
			return traits;
		}
	}

}
