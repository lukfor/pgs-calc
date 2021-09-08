package genepi.riskscore.tasks;

import org.junit.Before;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import lukfor.progress.tasks.monitors.TaskMonitorMock;

public class CreateHtmlReportTaskTest {

	@Before
	public void beforeTest() {
		System.out.println("Clean up output directory");
		FileUtil.deleteDirectory("test-data-output");
		FileUtil.createDirectory("test-data-output");
	}
	
	@Test
	public void testReport() throws Exception {

		ReportFile report = ReportFile.loadFromFile("test-data/report.json");
		OutputFile data = new OutputFile("test-data/output.csv");

		CreateHtmlReportTask task = new CreateHtmlReportTask();
		task.setData(data);
		task.setReport(report);
		task.setOutput("test-data-output/report.html");
		task.run(new TaskMonitorMock());

	}

}
