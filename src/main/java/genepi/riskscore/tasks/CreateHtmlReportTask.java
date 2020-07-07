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
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template;

import genepi.riskscore.App;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.tasks.report.DecimalFormatFunction;
import genepi.riskscore.tasks.report.PercentageFormatFunction;
import genepi.riskscore.tasks.report.TemplateLoader;

public class CreateHtmlReportTask {

	public static final String TEMPLATE_DIRECTORY = "/templates";

	public static final String REPORT_TEMPLATE = TEMPLATE_DIRECTORY + "/report.html";

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

		// general informations
		variables.put("createdOn", new Date());
		variables.put("version", App.VERSION);
		variables.put("application", App.APP);
		variables.put("url", App.URL);
		variables.put("copyright", App.COPYRIGHT);

		variables.put("samples", data.getSamples());
		Gson gson = new Gson();
		Type type = new TypeToken<List<String>>() {
		}.getType();
		String jsonArray = gson.toJson(data.getSamples(), type);

		variables.put("samplesData", jsonArray);

		// score statistics

		Type type2 = new TypeToken<List<Double>>() {
		}.getType();
		for (int i = 0; i < report.getSummaries().size(); i++) {
			// ignore empty scores
			if (report.getSummaries().get(i).getVariantsUsed() > 0) {
				jsonArray = gson.toJson(data.getData()[i], type2);
				report.getSummaries().get(i).setData(jsonArray);
			}
			report.getSummaries().get(i).updateStatistics();
		}
		variables.put("scores", report.getSummaries());

		// format functions and helpers
		variables.put("decimal", new DecimalFormatFunction());
		variables.put("percentage", new PercentageFormatFunction());

		InputStream is = getClass().getResourceAsStream(REPORT_TEMPLATE);
		Reader reader = new InputStreamReader(is);
		Compiler compiler = Mustache.compiler().escapeHTML(false).withLoader(new TemplateLoader(TEMPLATE_DIRECTORY));

		Template tmpl = compiler.compile(reader);
		reader.close();

		FileWriter writer = new FileWriter(output);
		tmpl.execute(variables, writer);
		writer.close();

	}

}
