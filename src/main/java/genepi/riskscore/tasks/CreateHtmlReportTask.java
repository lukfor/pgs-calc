package genepi.riskscore.tasks;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import genepi.riskscore.App;
import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;
import lukfor.reports.HtmlReport;

public class CreateHtmlReportTask implements ITaskRunnable {

	public static final String TEMPLATE_DIRECTORY = "/templates";

	public static final String REPORT_TEMPLATE = "/report.html";

	private String output;

	private MetaFile metaFile;

	private ReportFile report;

	private OutputFile data;

	private OutputFile[] referenceData;

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

	public void setReferenceData(OutputFile... referenceData) {
		this.referenceData = referenceData;
	}

	public void setMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Create HTML Report", ITaskMonitor.UNKNOWN);

		assert (report != null);
		assert (output != null);
		assert (data != null);

		if (metaFile != null) {
			report.mergeWithMeta(metaFile);
		}

		HtmlReport report = new HtmlReport(TEMPLATE_DIRECTORY);
		report.setMainFilename(REPORT_TEMPLATE);

		// general informations
		report.set("createdOn", new Date());
		report.set("version", App.VERSION);
		report.set("application", App.APP);
		report.set("application_name", "PGS-Calc");

		String args = String.join("\\<br>  ", App.ARGS);

		report.set("application_args", args);
		report.set("url", App.URL);
		report.set("copyright", App.COPYRIGHT);

		report.set("samples", data.getSamples());

		// add data do summaries
		for (int i = 0; i < this.report.getSummaries().size(); i++) {
			// ignore empty scores
			if (this.report.getSummaries().get(i).getVariantsUsed() > 0) {
				this.report.getSummaries().get(i).setData(data.getData(i));
				String name = this.report.getSummaries().get(i).getName();
				if (referenceData != null) {
					for (OutputFile refData : referenceData) {
						List<Double> reference = refData.getData(name);
						this.report.getSummaries().get(i).setReferenceData(refData.getName(), reference);
					}
				}
			}
			this.report.getSummaries().get(i).updateStatistics();
		}
		report.set("scores", this.report.getSummaries());

		report.setSelfContained(true);
		report.generate(new File(output));

		monitor.update("Html Report created and written to '" + output + "'");
		monitor.done();

	}

}
