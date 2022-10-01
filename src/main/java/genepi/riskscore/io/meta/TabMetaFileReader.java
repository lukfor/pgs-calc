package genepi.riskscore.io.meta;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import genepi.io.table.reader.CsvTableReader;
import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.MetaFile.MetaScore;
import genepi.riskscore.model.ScorePopulationMap;

public class TabMetaFileReader {

	public static String COLUMN_ID = "id";

	public static String COLUMN_TRAIT = "trait";

	public static String COLUMN_TRAIT_EFO = "trait_efo";

	public static String COLUMN_ANCESTRY_DISTRIBUTION = "ancestry_distribution";

	public static String COLUMN_VARIANTS = "variants";

	public static String COLUMN_SAMPLES = "samples";

	public static String COLUMN_LINK = "url.details";

	public static String COLUMN_REPOSITORY = "repository";

	public static String COLUMN_PUBLICATION_DOI = "publication.doi";

	public static String COLUMN_PUBLICATION_FIRSTAUTHOR = "publication.firstauthor";

	public static String COLUMN_PUBLICATION_JOURNAL = "publication.journal";

	public static String COLUMN_PUBLICATION_DATE = "publication.date";

	private TabMetaFileReader() {

	}

	public static MetaFile load(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {

		Map<String, MetaScore> index = new HashMap<String, MetaFile.MetaScore>();

		CsvTableReader reader = new CsvTableReader(filename, '\t');
		while (reader.next()) {
			MetaScore score = parseCsvRecord(reader);
			index.put(score.getId(), score);
		}
		reader.close();

		MetaFile metaFile = new MetaFile(index);
		return metaFile;

	}

	private static MetaScore parseCsvRecord(CsvTableReader reader) {

		MetaScore score = new MetaScore();
		String id = reader.getString(COLUMN_ID);
		String trait = reader.getString(COLUMN_TRAIT);
		score.setId(id);
		score.setTrait(trait);
		if (!reader.getString(COLUMN_SAMPLES).trim().isEmpty()) {
			score.setSamples(reader.getInteger(COLUMN_SAMPLES));
		}
		if (!reader.getString(COLUMN_VARIANTS).trim().isEmpty()) {
			score.setVariants(reader.getInteger(COLUMN_VARIANTS));
		}
		score.setRepository(reader.getString(COLUMN_REPOSITORY));
		score.setLink(reader.getString(COLUMN_LINK));

		String ancestry_distribution = reader.getString(COLUMN_ANCESTRY_DISTRIBUTION);
		if (!ancestry_distribution.trim().isEmpty()) {

			ScorePopulationMap populationMap = new ScorePopulationMap();
			String[] populations = ancestry_distribution.split(",");
			for (String population : populations) {
				String[] tiles = population.split("=");
				populationMap.addSamples(tiles[0], Float.parseFloat(tiles[1]) / 100.0f);
			}
			populationMap.total = score.getSamples();
			score.setPopulations(populationMap);
		}

		String doi = reader.getString(COLUMN_PUBLICATION_DOI);
		if (!doi.trim().isEmpty()) {
			Map<String, String> publication = new HashMap<String, String>();
			publication.put("doi", doi);
			publication.put("firstauthor", reader.getString(COLUMN_PUBLICATION_FIRSTAUTHOR));
			publication.put("journal", reader.getString(COLUMN_PUBLICATION_JOURNAL));
			publication.put("date", reader.getString(COLUMN_PUBLICATION_DATE));
			score.setPublication(publication);
		}

		// Object efos = data.get("trait_efo");
		// if (efos != null) {
		// score.setEfo((List<Map<String, String>>) efos);
		// }

		return score;

	}

}
