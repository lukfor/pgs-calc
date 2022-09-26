package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.riskscore.io.PGSCatalog;
import lukfor.progress.TaskService;
import picocli.CommandLine;

public class CreateHtmlReportCommandTest {

	@BeforeClass
	public static void setup() {
		TaskService.setAnsiSupport(false);
		PGSCatalog.ENABLE_CACHE = false;
	}

	@Before
	public void beforeTest() {
		System.out.println("Clean up output directory");
		FileUtil.deleteDirectory("test-data-output");
		FileUtil.createDirectory("test-data-output");
	}

	@Test
	public void testReport() throws Exception {

		String[] args = { "--data", "test-data/output.csv", "--info", "test-data/report.json", "--out",
				"test-data-output/report.html" };
		int result = new CommandLine(new CreateHtmlReportCommand()).execute(args);
		assertEquals(0, result);

	}

	@Test
	public void testReportWithoutData() throws Exception {

		String[] args = { "--info", "test-data/report.json", "--out", "test-data-output/report.html" };
		int result = new CommandLine(new CreateHtmlReportCommand()).execute(args);
		assertEquals(0, result);

	}

	@Test
	public void testReportWithTxtTemplate() throws Exception {

		String[] args = { "--info", "test-data/report.json", "--out", "test-data-output/report.txt", "--template",
				"txt" };
		int result = new CommandLine(new CreateHtmlReportCommand()).execute(args);
		assertEquals(0, result);

	}

	@Test
	public void testReportWithTxtTemplateAndMetaData() throws Exception {

		String[] args = { "--info", "test-data/report.json", "--out", "test-data-output/report.txt", "--template",
				"txt", "--meta", "test-data/pgs-catalog-small.json" };
		int result = new CommandLine(new CreateHtmlReportCommand()).execute(args);
		assertEquals(0, result);

	}

	@Test
	public void testReportWithHtmlTemplateAndMetaData() throws Exception {

		String[] args = { "--info", "test-data/report.json", "--out", "test-data-output/report.html", "--meta",
				"test-data/pgs-catalog-small.json" };
		int result = new CommandLine(new CreateHtmlReportCommand()).execute(args);
		assertEquals(0, result);

	}

}
