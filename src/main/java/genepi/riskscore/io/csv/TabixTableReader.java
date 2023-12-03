package genepi.riskscore.io.csv;

import genepi.io.FileUtil;
import genepi.io.table.reader.AbstractTableReader;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;
import genepi.riskscore.io.dbsnp.DbSnpReader;
import htsjdk.tribble.readers.TabixReader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TabixTableReader extends AbstractTableReader  {

	private List<String> headerLines = new Vector<String>();

	private String[] columns = null;

	private Map<String, Integer> index = new HashMap<String, Integer>();

	private String[] row = null;

	private TabixReader tabixReader;

	private TabixReader.Iterator iterator;

	private int lineNumber = 0;

	public TabixTableReader(String filename, String chromosome) throws IOException {
		this(filename, chromosome, 0, Integer.MAX_VALUE);
	}


	public TabixTableReader(String filename, String chromosome, int start, int end) throws IOException {
		LineReader reader = new LineReader(openTxtOrGzipStream(filename));
		while(reader.next()) {
			if (reader.get().startsWith("#")) {
				headerLines.add(reader.get());
			} else{
				columns = reader.get().split("\t");
				for (int i = 0 ; i < columns.length; i++){
					index.put(columns[i], i);
				}
				break;
			}
		}
		reader.close();

		tabixReader = new TabixReader(filename);
		iterator = tabixReader.query(chromosome, start, end);
	}

	public List<String> getHeader() {
		return headerLines;
	}

	@Override
	public String[] getColumns() {
		return columns;
	}

	@Override
	public boolean next() {
		String line = null;
		try {
			line = iterator.next();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (line == null) {
			row = null;
			return false;
		}
		row = line.split("\t", -1);
		lineNumber++;
		if (row.length != columns.length){
			throw new RuntimeException("Different number of columns in line " + lineNumber + ": " + line);
		}
		return true;
	}

	@Override
	public int getColumnIndex(String column) {
		return index.get(column);
	}

	@Override
	public String[] getRow() {
		return row;
	}

	@Override
	public void close() {
		tabixReader.close();
	}

	@Override
	public boolean hasColumn(String column) {
		return index.containsKey(column);
	}

	private static DataInputStream openTxtOrGzipStream(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		return new DataInputStream(in2);
	}
	
}
