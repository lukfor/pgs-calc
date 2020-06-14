package genepi.riskscore.io;

import picocli.CommandLine.Option;

public class Chunk {

	@Option(names = "--start", required = true)
	int start = 0;
	@Option(names = "--end", required = true)
	int end = Integer.MAX_VALUE;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
