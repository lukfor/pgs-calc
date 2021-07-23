package genepi.riskscore.tasks;

import org.junit.Test;

import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import lukfor.progress.tasks.monitors.TaskMonitorMock;

public class CreateHtmlReportTaskTest {

	@Test
	public void testReport() throws Exception {

		ReportFile report = ReportFile.loadFromFile("test-data/report.json");
		OutputFile data = new OutputFile("test-data/output.csv");

		CreateHtmlReportTask task = new CreateHtmlReportTask();
		task.setData(data);
		task.setReport(report);
		task.setOutput("report.html");
		task.run(new TaskMonitorMock());

	}

}
