package genepi.riskscore.tasks.report;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.samskivert.mustache.Mustache;

public class TemplateLoader implements Mustache.TemplateLoader {
	
	private String location;
	
	public TemplateLoader(String location) {
		this.location = location;
	}
	
	public Reader getTemplate(String name) {
		InputStream is = getClass().getResourceAsStream(location + "/" + name);
		return new InputStreamReader(is);
	}
}
