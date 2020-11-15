package genepi.riskscore.tasks;

import org.junit.Test;

import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import lukfor.progress.tasks.monitors.TaskMonitorMock;

public class CreateHtmlReportTaskTest {

	@Test
	public void testReport() throws Exception {

		ReportFile report = ReportFile.loadFromFile("test-data/report.json");
		OutputFile data = OutputFile.loadFromFile("test-data/output.csv");

		CreateHtmlReportTask task = new CreateHtmlReportTask();
		task.setData(data);
		task.setReport(report);
		task.setOutput("report.html");
		task.run(new TaskMonitorMock());

	}

	@Test
	public void testReportWithReferenceData() throws Exception {

		ReportFile report = ReportFile.loadFromFile("test-data/report.json");
		OutputFile data = OutputFile.loadFromFile("test-data/output.csv");
		OutputFile referenceDataAFR = OutputFile.loadFromFile("test-data/1000_genomes_AFR.txt");
		OutputFile referenceDataAMR = OutputFile.loadFromFile("test-data/1000_genomes_AMR.txt");

		MetaFile metaFile = MetaFile.loadFromFile("test-data/pgs-catalog-small.json");

		CreateHtmlReportTask task = new CreateHtmlReportTask();
		task.setData(data);
		task.setReferenceData(referenceDataAFR, referenceDataAMR);
		task.setReport(report);
		task.setMetaFile(metaFile);
		task.setOutput("report.html");
		task.run(new TaskMonitorMock());

	}

}
