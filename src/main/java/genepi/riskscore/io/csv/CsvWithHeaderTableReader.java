package genepi.riskscore.io.csv;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;

public class CsvWithHeaderTableReader extends CsvTableReader {

	private List<String> headerLines = new Vector<String>();

	public CsvWithHeaderTableReader(String filename, char seperator) throws IOException {
		super(filename, seperator);
		LineReader reader = new LineReader(openTxtOrGzipStream(filename));
		while(reader.next()) {
			if (reader.get().startsWith("#")) {
				headerLines.add(reader.get());
			} else{
				break;
			}
		}
		reader.close();
	}

	public List<String> getHeader() {
		return headerLines;
	}
	
	
	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}
	
}
