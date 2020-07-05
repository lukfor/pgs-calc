package genepi.riskscore.tasks;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import genepi.riskscore.App;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.model.RiskScoreSummary;

public class CreateHtmlReportTask {

	public static final String TEMPLATE = "/templates/report.html";

	private String output;

	private ReportFile report;
	
	private OutputFile data;

	public void setOutput(String output) {
		this.output = output;
	}

	public void setReport(ReportFile report) {
		this.report = report;
	}
	
	public void setData(OutputFile data) {
		this.data = data;
	}

	public void run() throws Exception {
		assert (report != null);
		assert (output != null);
		assert (data != null);


		Map<String, Object> variables = new HashMap<String, Object>();
		
		//general informations
		variables.put("createdOn", new Date());
		variables.put("version", App.VERSION);
		variables.put("application", App.APP);
		variables.put("url", App.URL);
		variables.put("copyright", App.COPYRIGHT);

		variables.put("samples", data.getSamples());
		
		// score statistics
		
		Gson gson = new Gson();
		Type type = new TypeToken<List<Double>>() {
		}.getType();
		for (int i = 0; i < report.getSummaries().size(); i++) {
			String jsonArray = gson.toJson(data.getData()[i], type);
			report.getSummaries().get(i).setData(jsonArray);
			report.getSummaries().get(i).updateStatistics();
		}
		
		variables.put("scores", report.getSummaries());

		InputStream is = getClass().getResourceAsStream(TEMPLATE);
		Reader reader = new InputStreamReader(is);
		Template tmpl = Mustache.compiler().compile(reader);
		reader.close();

		FileWriter writer = new FileWriter(output);
		tmpl.execute(variables, writer);
		writer.close();

	}

}
