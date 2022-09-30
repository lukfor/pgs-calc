package genepi.riskscore.io.meta;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.MetaFile.MetaScore;

public class JsonMetaFileReader {

	private JsonMetaFileReader() {

	}

	public static MetaFile load(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {

		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, MetaScore>>() {
		}.getType();

		Map<String, MetaScore> index = gson.fromJson(new FileReader(filename), type);
		MetaFile metaFile = new MetaFile(index);
		return metaFile;
	}


}
