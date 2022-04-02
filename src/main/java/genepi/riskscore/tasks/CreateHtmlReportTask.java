package genepi.riskscore.tasks;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

import genepi.riskscore.App;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.model.RiskScoreSummary;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;
import lukfor.reports.HtmlReport;

public class CreateHtmlReportTask implements ITaskRunnable {

	public static final String TEMPLATE_DIRECTORY = "/templates";

	public static final String DEFAULT_TEMPLATE = "default";

	public static final String INDEX_FILE = "index.html";

	private String output;

	private ReportFile report;

	private OutputFile data;

	private boolean showCommand = true;

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

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Create HTML Report", ITaskMonitor.UNKNOWN);

		assert (report != null);
		assert (output != null);

		HtmlReport report = new HtmlReport(TEMPLATE_DIRECTORY + "/" + template);
		report.setMainFilename(INDEX_FILE);

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

		// add data do summaries
		for (int i = 0; i < this.report.getSummaries().size(); i++) {
			// ignore empty scores
			if (this.report.getSummaries().get(i).getVariantsUsed() > 0) {
				if (data != null) {
					this.report.getSummaries().get(i).setData(data.getValuesByScore(i));
				}
			}
			this.report.getSummaries().get(i).updateStatistics();
		}

		// sort summaries by pgs name
		this.report.getSummaries().sort(new Comparator<RiskScoreSummary>() {
			@Override
			public int compare(RiskScoreSummary o1, RiskScoreSummary o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		report.set("scores", this.report.getSummaries());

		report.setSelfContained(true);
		report.generate(new File(output));

		monitor.update("Html Report created and written to '" + output + "'");
		monitor.done();

	}

}
