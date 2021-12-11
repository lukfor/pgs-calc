package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.tasks.MergeScoreTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import lukfor.progress.tasks.monitors.TaskMonitorMock;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "merge-scores", version = App.VERSION)
public class MergeScoreCommand implements Callable<Integer> {

	@Parameters(description = "score files")
	String[] chunkFiles;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	public Integer call() throws Exception {

		MergeScoreTask task = new MergeScoreTask();
		task.setInputs(chunkFiles);
		task.setOutput(out);
		List<Task> results = TaskService.monitor(App.STYLE_LONG_TASK).run(task);

		if (isFailed(results)) {
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
