package genepi.riskscore;

import genepi.riskscore.commands.ApplyScoreCommand;
import picocli.CommandLine;


public class App {

	public static final String APP = "riskscore";

	public static final String VERSION = "0.7.0";

	public static final String URL = "https://github.com/lukfor/riskscore";
	
	public static final String COPYRIGHT = "(c) 2020 Lukas Forer, Claudia Lamina and Stefan Coassin";
	
	public static void main(String[] args) {
		
		System.out.println();
		System.out.println(APP + " "+ VERSION);
		if (URL != null && !URL.isEmpty()) {
			System.out.println(URL);
		}
		if (COPYRIGHT != null &&! COPYRIGHT.isEmpty()) {
			System.out.println(COPYRIGHT);
		}
		System.out.println();
		
		new CommandLine(new ApplyScoreCommand()).execute(args);
		
	}
}
