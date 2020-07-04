package genepi.riskscore.tasks;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import genepi.riskscore.App;
import genepi.riskscore.io.ReportFile;

public class CreateHtmlReportTask {

	public static final String TEMPLATE = "/templates/report.html";

	private String output;

	private ReportFile report;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setReport(ReportFile report) {
		this.report = report;
	}

	public void run() throws Exception {
		assert (report != null);
		assert (output != null);


		Map<String, Object> data = new HashMap<String, Object>();
		
		//general informations
		data.put("createdOn", new Date());
		data.put("version", App.VERSION);
		data.put("application", App.APP);
		data.put("url", App.URL);
		data.put("copyright", App.COPYRIGHT);

		// score statistics
		data.put("scores", report.getSummaries());

		InputStream is = getClass().getResourceAsStream(TEMPLATE);
		Reader reader = new InputStreamReader(is);
		Template tmpl = Mustache.compiler().compile(reader);
		reader.close();

		FileWriter writer = new FileWriter(output);
		tmpl.execute(data, writer);
		writer.close();

	}

}
