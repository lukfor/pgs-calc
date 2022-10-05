package genepi.riskscore.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import genepi.io.FileUtil;
import genepi.riskscore.App;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.io.SamplesFile;
import genepi.riskscore.model.PopulationMap;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;
import lukfor.reports.HtmlReport;

public class CreateHtmlReportTask implements ITaskRunnable {

	public static final String TEMPLATE_DIRECTORY = "/templates";

	public static final String DEFAULT_TEMPLATE = "default";

	public static final String INDEX_FILE = "index.html";

	public static final String SCORE_FILE = "score.html";

	public static final String SAMPLES_FILE = "samples.html";

	private String output;

	private ReportFile report;

	private OutputFile data;

	private SamplesFile samples;

	private boolean showCommand = true;

	private boolean showDistribution = true;

	private String application = App.APP;

	private String applicationName = "PGS-Server";

	private String version = App.VERSION;

	private String url = App.URL;

	private String template = DEFAULT_TEMPLATE;

	public CreateHtmlReportTask() {

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

	public void setSamples(SamplesFile samples) {
		this.samples = samples;
	}

	public void setShowCommand(boolean showCommand) {
		this.showCommand = showCommand;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void setShowDistribution(boolean showDistribution) {
		this.showDistribution = showDistribution;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Create HTML Report", ITaskMonitor.UNKNOWN);

		assert (report != null);
		assert (output != null);

		if (data != null && showDistribution) {
			report.mergeWithData(data);
		} else {
			report.mergeWithData(null);
		}

		report.checkPopulations(samples, data);
		
		String templateLocation = TEMPLATE_DIRECTORY + "/" + template;

		// create index
		HtmlReport htmlReport = buildReport(templateLocation, INDEX_FILE);
		htmlReport.set("scores", this.report.getSummaries());
		htmlReport.generate(new File(output));

		String scoresOutput = FileUtil.path(new File(output).getParent(), "scores");

		if (exists(templateLocation, SCORE_FILE)) {
			// FileUtil.deleteDirectory(scores);
			FileUtil.createDirectory(scoresOutput);
			for (int i = 0; i < this.report.getSummaries().size(); i++) {
				RiskScoreSummary score = this.report.getSummaries().get(i);
				HtmlReport htmlReportScore = buildReport(templateLocation, SCORE_FILE);
				List<RiskScoreSummary> currentScore = new Vector<RiskScoreSummary>();
				currentScore.add(score);
				htmlReportScore.set("scores", currentScore);
				htmlReportScore.generate(new File(FileUtil.path(scoresOutput, score.getName() + ".html")));
			}
		}
		if (exists(templateLocation, SAMPLES_FILE)) {
			htmlReport = buildReport(templateLocation, SAMPLES_FILE);
			htmlReport.set("scores", this.report.getSummaries());
			htmlReport.generate(new File(FileUtil.path(scoresOutput, "samples.html")));
		}
		monitor.update("Html Report created and written to '" + output + "'");
		monitor.done();

	}


	private HtmlReport buildReport(String root, String indexFile) throws IOException {
		HtmlReport report = new HtmlReport(root);
		report.setMainFilename(indexFile);

		// general informations
		report.set("createdOn", new Date());
		report.set("version", version);
		report.set("application", application);
		report.set("application_name", applicationName);

		if (showCommand) {
			String args = String.join("\\<br>  ", App.ARGS);
			report.set("show_command", true);
			report.set("application_args", args);
		} else {
			report.set("show_command", false);
		}

		report.set("url", url);
		report.set("copyright", App.COPYRIGHT);

		if (data != null) {
			report.set("show_samples", true);
			report.set("samples", data.getSamples());
		} else {
			report.set("show_samples", false);
			report.set("samples", null);
		}

		if (samples != null) {
			report.set("populations", samples.getPopulations());
		} else {
			report.set("populations", new PopulationMap());
		}

		if (samples != null && data != null) {
			report.set("population_check", true);
		} else {
			report.set("population_check", false);
		}

		report.set("showDistribution", showDistribution);
		report.setSelfContained(true);

		return report;

	}

	public boolean exists(String root, String filename) {
		try {
			InputStream stream = this.getClass().getResource(root + "/" + filename).openStream();
			stream.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
