package genepi.riskscore.tasks;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template;

import genepi.riskscore.App;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.tasks.report.CompressFunction;
import genepi.riskscore.tasks.report.DecimalFormatFunction;
import genepi.riskscore.tasks.report.PercentageFormatFunction;
import genepi.riskscore.tasks.report.TemplateLoader;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class CreateHtmlReportTask implements ITaskRunnable {

	public static final String TEMPLATE_DIRECTORY = "/templates";

	public static final String REPORT_TEMPLATE = TEMPLATE_DIRECTORY + "/report.html";

	private String output;

	private ReportFile report;

	private OutputFile data;

	private DecimalFormat df;

	public CreateHtmlReportTask() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		df = (DecimalFormat) nf;
		df.applyPattern("#.########");
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setReport(ReportFile report) {
		this.report = report;
	}

	public void setData(OutputFile data) {
		this.data = data;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Create HTML Report", ITaskMonitor.UNKNOWN);

		assert (report != null);
		assert (output != null);
		assert (data != null);

		Map<String, Object> variables = new HashMap<String, Object>();

		// general informations
		variables.put("createdOn", new Date());
		variables.put("version", App.VERSION);
		variables.put("application", App.APP);
		variables.put("application_name", "PGS-Calc");

		String args = String.join("\\<br>  ", App.ARGS);

		variables.put("application_args", args);
		variables.put("url", App.URL);
		variables.put("copyright", App.COPYRIGHT);

		variables.put("samples", data.getSamples());

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
			df.setRoundingMode(RoundingMode.CEILING);
			return new JsonPrimitive(Double.parseDouble(df.format(src)));
		});
		Gson gson = builder.create();

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
		variables.put("compress", new CompressFunction());

		InputStream is = getClass().getResourceAsStream(REPORT_TEMPLATE);
		Reader reader = new InputStreamReader(is);
		Compiler compiler = Mustache.compiler().escapeHTML(false).withLoader(new TemplateLoader(TEMPLATE_DIRECTORY));

		Template tmpl = compiler.compile(reader);
		reader.close();

		FileWriter writer = new FileWriter(output);
		tmpl.execute(variables, writer);
		writer.close();

		monitor.update("Html Report created and written to '" + output + "'");
		monitor.done();

	}

}
