package genepi.riskscore.commands;

import java.util.List;
import java.util.concurrent.Callable;

import genepi.io.FileUtil;
import genepi.io.text.GzipLineWriter;
import genepi.io.text.LineReader;
import genepi.riskscore.App;
import genepi.riskscore.io.Chain;
import genepi.riskscore.tasks.LiftOverScoreTask;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "liftover", version = App.VERSION)
public class LiftOverScoreCommand implements Callable<Integer> {

	@Option(names = "--in", description = "input score file", required = true)
	private String input;

	@Option(names = "--out", description = "output score file", required = true)
	private String output;

	@ArgGroup(exclusive = false, multiplicity = "1")
	private Chain chain;

	@Override
	public Integer call() throws Exception {

		LiftOverScoreTask task = new LiftOverScoreTask(input, output + ".raw", chain);
		TaskService.setAnsiSupport(false);
		TaskService.setAnimated(false);
		List<Task> result = TaskService.run(task);
		if (!result.get(0).getStatus().isSuccess()) {
			result.get(0).getStatus().getThrowable().printStackTrace();
			System.out.println("*** ERROR ***  " + result.get(0).getStatus().getThrowable());
			return 1;
		}

		System.out.println("Number Variants Input: " + task.getTotal());
		System.out.println("Number Variants lifted: " + task.getResolved());
		System.out.println("Ignored Variants: " + task.getFailed());

		LineReader reader = new LineReader(output + ".raw");
		GzipLineWriter writer = new GzipLineWriter(output);
		while (reader.next()) {
			writer.write(reader.get());
		}
		writer.close();
		reader.close();

		FileUtil.deleteFile(output + ".raw");

		return 0;

	}

}
