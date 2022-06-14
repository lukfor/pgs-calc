package genepi.riskscore.io.scores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import htsjdk.samtools.util.BlockCompressedOutputStream;

public class LineWriter {

	private BlockCompressedOutputStream bw;

	private boolean first = true;

	private boolean data = false;

	public LineWriter(String filename) throws IOException {
		bw = new BlockCompressedOutputStream(new File(filename));
		first = true;
		data = false;
	}

	public void write(String line) throws IOException {
		write(line, true);
	}

	public void write(String line, boolean dataRow) throws IOException {
		if (first) {
			first = false;
		} else {
			bw.write('\n');
		}

		this.data = dataRow;

		bw.write(line.getBytes());
	}

	public void close() throws IOException {
		bw.close();
	}

	public boolean hasData() {
		return data;
	}

}
