package genepi.riskscore.io.proxy;

import java.io.IOException;

import htsjdk.tribble.readers.TabixReader;
import htsjdk.tribble.readers.TabixReader.Iterator;

public class ProxyReader {

	private TabixReader reader;

	public ProxyReader(String input) throws IOException {
		reader = new TabixReader(input);
	}

	public ProxySnp[] getByPosition(String chromosome, int position, String alleleA, String alleleB)
			throws IOException {

		Iterator result = reader.query(chromosome, position - 1, position);

		String line = result.next();

		if (line != null) {
			String[] tiles = line.split("\t");
			if (tiles.length == 5) {
				String[] proxiesDetails = tiles[4].split(";");
				if ((tiles[2].equals(alleleA) && tiles[3].equals(alleleB))
						|| (tiles[2].equals(alleleB) || tiles[3].equals(alleleA))) {
					ProxySnp[] proxies = new ProxySnp[proxiesDetails.length];
					for (int i = 0; i < proxiesDetails.length; i++) {
						String proxyDetails = proxiesDetails[i];
						String tiles2[] = proxyDetails.split(":");
						proxies[i] = new ProxySnp();
						proxies[i].setChromosome(tiles2[0]);
						proxies[i].setPosition(Integer.parseInt(tiles2[1]));
						proxies[i].setReference(tiles2[2]);
						proxies[i].setAlternate(tiles2[3]);
						proxies[i].setProxyReference(tiles[2]);
						proxies[i].setProxyAlternate(tiles[3]);
					}
					return proxies;
				} else {
					return new ProxySnp[0];
				}
			} else {
				throw new IOException("Index has not 5 columns.");
			}

		} else {
			return new ProxySnp[0];
		}
	}

	public class ProxySnp {

		private String chromosome;

		private int position;

		private String reference;

		private String alternate;

		private String proxyReference;

		private String proxyAlternate;

		public String getChromosome() {
			return chromosome;
		}

		public void setChromosome(String chromosome) {
			this.chromosome = chromosome;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
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

		public void setProxyAlternate(String proxyAlternate) {
			this.proxyAlternate = proxyAlternate;
		}

		public void setProxyReference(String proxyReference) {
			this.proxyReference = proxyReference;
		}

		public String mapAllele(String allele) throws IOException {
			if (allele.equals(proxyReference)) {
				return reference;
			}
			if (allele.equals(proxyAlternate)) {
				return alternate;
			}
			throw new IOException("No mapping entry for allele '" + allele + "'");
		}

		@Override
		public String toString() {
			return chromosome + ":" + position + ":" + reference;
		}

	}

	public void close() {
		reader.close();
	}

}
