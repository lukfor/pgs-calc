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

public class MetaFile {

	protected Map<String, Object> index;

	protected List list;

	protected String next;

	private MetaFile() {

	}

	public static MetaFile load(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		MetaFile file = new MetaFile();
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
		file.list = (List) results;

		file.index = new HashMap<String, Object>();
		for (Object score : file.list) {
			Object id = ((AbstractMap<String, Object>) score).get("id");
			file.index.put(id.toString(), score);
		}
		return file;
	}

	public Object getDataById(String id) {
		return index.get(id);
	}

	public void merge(MetaFile metaFile) {
		list.addAll(metaFile.list);
	}

	public int getScores() {
		return list.size();
	}

	public String getNext() {
		return next;
	}

	public void save(String filename) throws IOException {
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map>>() {
		}.getType();
		Map<String, Object> o = new HashMap<>();
		o.put("results", list);
		String json = gson.toJson(o);
		LineWriter writer = new LineWriter(filename);
		writer.write(json);
		writer.close();

	}

}
