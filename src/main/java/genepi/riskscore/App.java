package genepi.riskscore;

import static lukfor.progress.Components.PROGRESS_BAR;
import static lukfor.progress.Components.SPACE;
import static lukfor.progress.Components.SPINNER;
import static lukfor.progress.Components.TASK_NAME;
import static lukfor.progress.Components.TIME;

import java.util.concurrent.Callable;

import genepi.riskscore.commands.*;
import lukfor.progress.renderer.ProgressIndicatorGroup;
import picocli.CommandLine;
import picocli.CommandLine.Command;

public class App {

	public static final String APP = "pgs-calc";

	public static final String VERSION = "1.6.0";

	public static final String URL = "https://github.com/lukfor/pgs-calc";

	public static final String COPYRIGHT = "(c) 2020 - 2023 Lukas Forer";

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

		int exitCode = new CommandLine(new DefaultCommand()).execute(args);
		System.exit(exitCode);

	}

	@Command(name = App.APP, version = App.VERSION, subcommands = { ApplyScoreCommand.class, MergeScoreCommand.class,
			MergeInfoCommand.class, MergeVariantsCommand.class, MergeEffectsCommand.class, ResolveScoreCommand.class,
			CreateHtmlReportCommand.class, DownloadMetaCommand.class, LiftOverScoreCommand.class,
			DownloadScoreCommand.class, ClearCacheCommand.class, ValidateScoreCommand.class,
			ProcessHaploRegCommand.class, CreateChunksCommand.class, CreateCollectionCommand.class, FilterMetaCommand.class })
	public static class DefaultCommand implements Callable<Integer> {

		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static ProgressIndicatorGroup STYLE_LONG_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE, TASK_NAME,
			PROGRESS_BAR, TIME);

	public static ProgressIndicatorGroup STYLE_SHORT_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE,
			TASK_NAME);

}
