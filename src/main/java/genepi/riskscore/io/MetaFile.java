package genepi.riskscore.io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import genepi.riskscore.model.RiskScoreSummary;

public class MetaFile {

	protected Map<String, Object> index;
	
	private MetaFile() {
		
	}
	
	public static MetaFile load(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		MetaFile file = new MetaFile();
		//file.data
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map>>() {
		}.getType();
		Object data = gson.fromJson(new FileReader(filename), Object.class);
		
		Object results = ( (AbstractMap<String, Object>) data).get("results");
		List list = (List) results;
				
		file.index = new HashMap<String, Object>();
		for (Object score: list) {
			Object id = ( (AbstractMap<String, Object>) score).get("id");
			file.index.put(id.toString(), score);
		}
		return file;
	}
	
	public Object getDataById(String id) {
		return index.get(id);
	}
	
}
