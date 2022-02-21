package genepi.riskscore.io.dbsnp;

import java.io.IOException;

import htsjdk.tribble.readers.TabixReader;
import htsjdk.tribble.readers.TabixReader.Iterator;

public class DbSnpReader {

	private TabixReader reader;

	public DbSnpReader(String input) throws IOException {
		reader = new TabixReader(input);
	}

	public Snp getByRsId(String rs) throws IOException {

		Iterator result = reader.query(DbSnpReader.getContig(rs), DbSnpReader.getPosition(rs) - 1,
				DbSnpReader.getPosition(rs));

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

	public static String getContig(String rsID) {
		if (rsID.length() > 10) {
			return rsID.substring(0, 3);
		} else {
			return "rs";
		}
	}

	public static int getPosition(String rsID) {
		if (rsID.length() > 10) {
			return Integer.parseInt(rsID.substring(3));
		} else {
			return Integer.parseInt(rsID.substring(2));
		}
	}

}
