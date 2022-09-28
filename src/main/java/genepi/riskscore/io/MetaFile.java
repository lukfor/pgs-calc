package genepi.riskscore.io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import genepi.io.text.LineWriter;
import genepi.riskscore.model.ScorePopulationMap;

public class MetaFile {

	protected Map<String, MetaScore> index;

	private MetaFile() {

	}

	public static MetaFile load(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {

		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, MetaScore>>() {
		}.getType();
		
		MetaFile metaFile = new MetaFile();
		metaFile.index = gson.fromJson(new FileReader(filename),  type);
		return metaFile;
	}
	
	public MetaScore getById(String id) {
		return index.get(id);
	}

	public void save(String filename) throws IOException {
		Gson gson = new Gson();
		String json = gson.toJson(index);
		LineWriter writer = new LineWriter(filename);
		writer.write(json);
		writer.close();
	}

	public static class MetaScore {

		private String id;

		private String trait;

		private String traitAdditional;

		private ScorePopulationMap populations;

		private Map<String, String> publication;

		private int variants;

		private String repository;

		private String link;

		private int samples;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTrait() {
			return trait;
		}

		public void setTrait(String trait) {
			this.trait = trait;
		}

		public String getTraitAdditional() {
			return traitAdditional;
		}

		public void setTraitAdditional(String traitAdditional) {
			this.traitAdditional = traitAdditional;
		}

		public ScorePopulationMap getPopulations() {
			return populations;
		}

		public void setPopulations(ScorePopulationMap populations) {
			this.populations = populations;
		}

		public Map<String, String> getPublication() {
			return publication;
		}

		public void setPublication(Map<String, String> publication) {
			this.publication = publication;
		}

		public int getVariants() {
			return variants;
		}

		public void setVariants(int variants) {
			this.variants = variants;
		}

		public String getRepository() {
			return repository;
		}

		public void setRepository(String repository) {
			this.repository = repository;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public int getSamples() {
			return samples;
		}

		public void setSamples(int samples) {
			this.samples = samples;
		}

	}

}
