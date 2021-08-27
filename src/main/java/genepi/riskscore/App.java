package genepi.riskscore;

import static lukfor.progress.Components.PROGRESS_BAR;
import static lukfor.progress.Components.SPACE;
import static lukfor.progress.Components.SPINNER;
import static lukfor.progress.Components.TASK_NAME;
import static lukfor.progress.Components.TIME;

import genepi.riskscore.commands.ApplyScoreCommand;
import lukfor.progress.renderer.ProgressIndicatorGroup;
import picocli.CommandLine;

public class App {

	public static final String APP = "pgs-calc";

	public static final String VERSION = "0.9.3-beta";

	public static final String URL = "https://github.com/lukfor/pgs-calc";

	public static final String COPYRIGHT = "(c) 2020 - 2021 Lukas Forer";

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

	public static ProgressIndicatorGroup STYLE_LONG_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE, TASK_NAME,
			PROGRESS_BAR, TIME);

	public static ProgressIndicatorGroup STYLE_SHORT_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE,
			TASK_NAME);

}
