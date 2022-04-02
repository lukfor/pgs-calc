package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.io.MetaFile;
import genepi.riskscore.io.OutputFile;
import genepi.riskscore.io.ReportFile;
import genepi.riskscore.tasks.CreateHtmlReportTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "report", version = App.VERSION)
public class CreateHtmlReportCommand implements Callable<Integer> {

	@Option(names = { "--data" }, description = "JSON file with meta data about scores", required = false)
	String data = null;

	@Option(names = { "--info" }, description = "JSON file with meta data about scores", required = true)
	String info = null;

	@Option(names = { "--meta" }, description = "JSON file with meta data about scores", required = false)
	String meta = null;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	@Option(names = { "--template" }, description = "template to create html report", required = false)
	String template = null;

	
	public Integer call() throws Exception {

		ReportFile infoFile = ReportFile.loadFromFile(info);

		if (meta != null) {
			MetaFile metaFile = MetaFile.load(meta);
			infoFile.mergeWithMeta(metaFile);
		}

		CreateHtmlReportTask htmlReportTask = new CreateHtmlReportTask();
		htmlReportTask.setReport(infoFile);
		if (data != null) {
			OutputFile outputFile = new OutputFile(data);
			htmlReportTask.setData(outputFile);
		}
		if (template != null) {
			htmlReportTask.setTemplate(template);
		}
		htmlReportTask.setOutput(out);

		if (isFailed(TaskService.monitor(App.STYLE_SHORT_TASK).run(htmlReportTask))) {
			return 1;
		} else {
			return 0;
		}

	}

	private boolean isFailed(List<Task> tasks) {
		for (Task result : tasks) {
			if (!result.getStatus().isSuccess()) {
				return true;
			}
		}
		return false;
	}

}
