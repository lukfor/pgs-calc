package genepi.riskscore;

import genepi.riskscore.commands.ApplyScoreCommand;
import lukfor.progress.ProgressBarBuilder;
import lukfor.progress.TaskService;
import lukfor.progress.renderer.labels.StringLabel;
import lukfor.progress.renderer.labels.TaskNameLabel;
import lukfor.progress.renderer.labels.TimeLabel;
import lukfor.progress.renderer.spinners.DefaultSpinner;
import picocli.CommandLine;

public class App {

	public static final String APP = "pgs-calc";

	public static final String VERSION = "0.9.0";

	public static final String URL = "https://github.com/lukfor/pgs-calc";

	public static final String COPYRIGHT = "(c) 2020 Lukas Forer";

	public static String[] ARGS = new String[0];

	public static void main(String[] args) {

		System.out.println();
		System.out.println(APP + " " + VERSION);
		if (URL != null && !URL.isEmpty()) {
			System.out.println(URL);
		}
		if (COPYRIGHT != null && !COPYRIGHT.isEmpty()) {
			System.out.println(COPYRIGHT);
		}
		System.out.println();

		ARGS = args;

		new CommandLine(new ApplyScoreCommand()).execute(args);

	}

	public static ProgressBarBuilder STYLE_LONG_TASK() {

		if (TaskService.isAnsiSupport()) {

			return new ProgressBarBuilder().components(new StringLabel(" "), new DefaultSpinner(), new TaskNameLabel(),
					ProgressBarBuilder.DEFAULT_STYLE, new TimeLabel());

		} else {

			return new ProgressBarBuilder().animated(false);

		}

	}

	public static ProgressBarBuilder STYLE_SHORT_TASK() {

		if (TaskService.isAnsiSupport()) {

			return new ProgressBarBuilder().components(new StringLabel(" "), new DefaultSpinner(), new StringLabel(" "), new TaskNameLabel());

		} else {

			return new ProgressBarBuilder().animated(false);

		}

	}

}
