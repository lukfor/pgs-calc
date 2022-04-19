package genepi.riskscore.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import picocli.CommandLine.Option;

public class Chain {

	@Option(names = "--chain", description = "chain file", required = false)
	private String chain;

	@Option(names = "--source", description = "source build", required = false)
	private String source;

	@Option(names = "--target", description = "target build", required = false)
	private String target;

	public static Map<String, String> chainFiles = new HashMap<String, String>();

	public static void addChainFile(String source, String target, String chain) {
		chainFiles.put(source + "_" + target, chain);
	}

	public static String getChainFile(String source, String target) {
		return chainFiles.get(source + "_" + target);
	}

	static {
		addChainFile("hg19", "hg38", "chains/hg19ToHg38.over.chain.gz");
		addChainFile("hg38", "hg19", "chains/hg38ToHg19.over.chain.gz");
	}

	public String getFilename() {
		String filename;
		if (chain == null) {
			if (source == null) {
				throw new RuntimeException("Source build not set");
			}
			if (target == null) {
				throw new RuntimeException("Target build not set");
			}
			filename = getChainFile(source, target);
			if (filename == null) {
				throw new RuntimeException("No chain file found for source '" + source + "' and target '" + target
						+ "'. You can use --chain for an external file.");
			}
			return filename;
		} else {
			return chain;
		}
	}
	
	public File getFile() {
		return new File(getFilename());
	}

}
