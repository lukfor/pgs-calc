package genepi.riskscore;

import genepi.riskscore.commands.ApplyScoreCommand;
import picocli.CommandLine;


public class App {

	public static final String APP = "riskscore";

	public static final String VERSION = "0.0.1";

	public static void main(String[] args) {
		
		System.out.println();
		System.out.println(APP + " "+ VERSION);
		System.out.println();
		
		new CommandLine(new ApplyScoreCommand()).execute(args);
		
	}
}
