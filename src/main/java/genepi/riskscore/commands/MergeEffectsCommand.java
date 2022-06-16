package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.riskscore.App;
import genepi.riskscore.tasks.MergeEffectsTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "merge-effects", version = App.VERSION)
public class MergeEffectsCommand implements Callable<Integer> {

	@Parameters(description = "effects files")
	String[] chunkFiles;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String out;

	public Integer call() throws Exception {

		MergeEffectsTask task = new MergeEffectsTask();
		task.setInputs(chunkFiles);
		task.setOutput(out);
		List<Task> results = TaskService.monitor(App.STYLE_LONG_TASK).run(task);

		if (isFailed(results)) {
			results.get(0).getStatus().getThrowable().printStackTrace();
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
