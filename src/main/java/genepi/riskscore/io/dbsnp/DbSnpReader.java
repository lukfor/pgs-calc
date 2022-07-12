package genepi.riskscore.io.dbsnp;

import java.io.IOException;

import genepi.io.text.LineReader;
import htsjdk.tribble.readers.TabixReader;
import htsjdk.tribble.readers.TabixReader.Iterator;

public class DbSnpReader {

	private TabixReader reader;

	private int size = 3;

	public DbSnpReader(String input) throws IOException {

		// read header
		LineReader headerReader = new LineReader(input);
		while (headerReader.next()) {
			String line = headerReader.get();
			if (!line.startsWith("#")) {
				break;
			}
			String[] tiles = line.split("=");
			if (tiles.length == 2) {
				String key = tiles[0];
				String value = tiles[1];
				if (key.equalsIgnoreCase("#size")) {
					size = Integer.parseInt(value);
				}
			}
		}
		headerReader.close();

		reader = new TabixReader(input);

	}

	public Snp getByRsId(String rs) throws IOException {

		Iterator result = reader.query(DbSnpReader.getContig(rs, size), DbSnpReader.getPosition(rs, size) - 1,
				DbSnpReader.getPosition(rs, size));

		String line = result.next();

		if (line != null) {
			String[] tiles = line.split("\t");
			if (tiles.length == 6) {
				Snp snp = new Snp();
				snp.setChromosome(tiles[2]);
				snp.setPosition(Integer.parseInt(tiles[3]));
				snp.setReference(tiles[4].replaceAll("\\*", ""));
				snp.setAlternate(tiles[5]);
				return snp;
			} else {
				throw new IOException("Index has not 6 columns.");
			}

		} else {
			return null;
		}
	}

	public class Snp {

		private String chromosome;

		private long position;

		private String reference;

		private String alternate;

		public String getChromosome() {
			return chromosome;
		}

		public void setChromosome(String chromosome) {
			this.chromosome = chromosome;
		}

		public long getPosition() {
			return position;
		}

		public void setPosition(long position) {
			this.position = position;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

		public String getReference() {
			return reference;
		}

		public void setAlternate(String alternate) {
			this.alternate = alternate;
		}

		public String getAlternate() {
			return alternate;
		}

		@Override
		public String toString() {
			return chromosome + ":" + position + ":" + reference;
		}

	}

	public void close() {
		reader.close();
	}

	public static String getContig(String rsID, int size) {
		if (rsID.length() > 10) {
			// count zeros --> rs1, rs10, ...
			String position = rsID.substring(size);
			int count = countCharacter(position, '0');
			return rsID.substring(0, size) + sequence('0', count);
		} else {
			String position = rsID.substring(2);
			int count = countCharacter(position, '0');
			return "rs" + sequence('0', count);
		}
	}

	public static int getPosition(String rsID, int size) {
		if (rsID.length() > 10) {
			return Integer.parseInt(rsID.substring(size));
		} else {
			return Integer.parseInt(rsID.substring(2));
		}
	}

	public static int countCharacter(String string, char character) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != character) {
				break;
			}
			count++;
		}
		return count;
	}

	public static String sequence(char character, int count) {
		String result = "";
		for (int i = 0; i < count; i++) {
			result += character;
		}
		return result;
	}

}
