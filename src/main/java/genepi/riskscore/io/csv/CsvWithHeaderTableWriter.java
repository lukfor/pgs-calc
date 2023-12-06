package genepi.riskscore.io.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;
import genepi.io.table.writer.AbstractTableWriter;
import genepi.io.text.LineWriter;
import genepi.riskscore.App;

public class CsvWithHeaderTableWriter extends AbstractTableWriter {

	private CSVWriter writer;
	public String[] currentLine;
	private Map<String, Integer> columns2Index = new HashMap<String, Integer>();

	public CsvWithHeaderTableWriter(String filename, char separator, List<String> header) {
		try {
			LineWriter lineWriter = new LineWriter(filename);
			for (String line : header) {
				lineWriter.write(line.replace("\n", "").replace("\r", ""));
			}
			if (!header.isEmpty()) {
				lineWriter.write("# Updated by " + App.APP + " " + App.VERSION + "\n");
			}
			lineWriter.close();
			writer = new CSVWriter(new FileWriter(filename, true), separator, CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.NO_ESCAPE_CHARACTER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CsvWithHeaderTableWriter( char separator, List<String> header) {
		for (String line : header) {
			System.out.println(line.replace("\n", "").replace("\r", ""));
		}
		if (!header.isEmpty()) {
			System.out.println("# Updated by " + App.APP + " " + App.VERSION + "\n");
		}
		writer = new CSVWriter(new OutputStreamWriter(System.out), separator, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.NO_ESCAPE_CHARACTER);
	}

	public CsvWithHeaderTableWriter(Writer stream, char separator, List<String> header) throws IOException {
		for (String line : header) {
			stream.write(line.replace("\n", "").replace("\r", ""));
			stream.write(System.lineSeparator());
		}
		stream.write("# Created by " + App.APP + " " + App.VERSION);
		stream.write(System.lineSeparator());
		writer = new CSVWriter(stream, separator, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.NO_ESCAPE_CHARACTER);
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getColumnIndex(String column) {
		return columns2Index.get(column);
	}

	@Override
	public boolean next() {
		writer.writeNext(currentLine);
		for (int i = 0; i < currentLine.length; i++) {
			currentLine[i] = "";
		}
		return true;
	}

	@Override
	public void setColumns(String[] columns) {
		currentLine = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			columns2Index.put(columns[i], i);
			currentLine[i] = "";
		}
		writer.writeNext(columns);
	}

	@Override
	public void setDouble(int column, double value) {
		currentLine[column] = value + "";
	}

	@Override
	public void setInteger(int column, int value) {
		currentLine[column] = value + "";
	}

	@Override
	public void setString(int column, String value) {
		currentLine[column] = value;
	}

	@Override
	public void setRow(String[] row) {
		currentLine = row;
	}

}
