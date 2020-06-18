package genepi.riskscore.io.vcf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MinimalVariantContext {

	public final static String NO_FILTERS = "";

	private int start;

	private String contig;

	private String referenceAllele;

	private String alternateAllele;

	private int hetCount;

	private int homRefCount;

	private int homVarCount;

	private int noCallCount;

	private int nSamples;

	private String rawLine;

	private String filters;

	private boolean[] genotypes;

	private float[] genotypesParsed;

	private String id = null;

	private String genotype = null;

	private String info = null;

	private Map<String, String> infos;

	private boolean dirtyGenotypes = true;
	
	public MinimalVariantContext(int samples) {
		genotypes = new boolean[samples];
		genotypesParsed = new float[samples];
	}

	public int getHetCount() {
		return hetCount;
	}

	public void setHetCount(int hetCount) {
		this.hetCount = hetCount;
		this.id = null;
	}

	public int getHomRefCount() {
		return homRefCount;
	}

	public void setHomRefCount(int homRefCount) {
		this.homRefCount = homRefCount;
		this.id = null;
	}

	public int getHomVarCount() {
		return homVarCount;
	}

	public void setHomVarCount(int homVarCount) {
		this.homVarCount = homVarCount;
		this.id = null;
	}

	public int getNoCallCount() {
		return noCallCount;
	}

	public void setNoCallCount(int noCallCount) {
		this.noCallCount = noCallCount;
		this.id = null;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
		this.id = null;
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
		this.id = null;
	}

	public String getReferenceAllele() {
		return referenceAllele;
	}

	public void setReferenceAllele(String referenceAllele) {
		this.referenceAllele = referenceAllele;
		this.id = null;
		this.genotype = null;
	}

	public String getAlternateAllele() {
		return alternateAllele;
	}

	public void setAlternateAllele(String alternateAllele) {
		this.alternateAllele = alternateAllele;
		this.id = null;
		this.genotype = null;
	}

	public void setNSamples(int nSamples) {
		this.nSamples = nSamples;
		this.id = null;
	}

	public int getNSamples() {
		return nSamples;
	}

	public void setRawLine(String rawLine) {
		this.rawLine = rawLine;
		this.id = null;
		this.dirtyGenotypes = true;
	}

	public String getRawLine() {
		return rawLine;
	}

	public boolean isFiltered() {
		return filters != null && !filters.isEmpty();
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
		this.id = null;
	}

	public boolean isIndel() {
		return getReferenceAllele().length() > 1 || getAlternateAllele().length() > 1;
	}

	public boolean isComplexIndel() {
		return getReferenceAllele().length() > 1 || getAlternateAllele().length() > 1;
	}

	public boolean isMonomorphicInSamples() {
		return (homRefCount + noCallCount == nSamples);
	}

	public void setCalled(int sample, boolean called) {
		genotypes[sample] = called;
		this.id = null;
	}

	public boolean isCalled(int sample) {
		return genotypes[sample];
	}

	public String getGenotype() {

		if (genotype == null) {
			StringBuilder builder = new StringBuilder(2);
			builder.append(referenceAllele);
			builder.append(alternateAllele);
			genotype = builder.toString();
		}
		return genotype;

	}

	public String toString() {
		if (id == null) {
			StringBuilder builder = new StringBuilder(7);
			builder.append(getContig());
			builder.append(":");
			builder.append(getStart());
			builder.append(":");
			builder.append(getReferenceAllele());
			builder.append(":");
			builder.append(getAlternateAllele());
			id = builder.toString();
		}
		return id;
	}

	public void setInfo(String info) {
		this.info = info;
		infos = null;
	}

	public String getInfo(String key) {
		// lazy loadding
		if (infos == null) {
			infos = new HashMap<String, String>();
			String[] tiles = this.info.split(";");
			for (int i = 0; i < tiles.length; i++) {
				String[] tiles2 = tiles[i].split("=");
				if (tiles2.length == 2) {
					infos.put(tiles2[0], tiles2[1]);
				}
			}
		}
		return infos.get(key);
	}

	public double getInfoAsDouble(String key, double defaultValue) {
		String value = getInfo(key);
		if (value != null) {
			return Double.parseDouble(value);
		} else {
			return defaultValue;
		}
	}

	public float[] getGenotypeDosages(String field) throws IOException {

		if (dirtyGenotypes) {

			String tiles[] = rawLine.split("\t", 10);
			String[] formats = tiles[8].split(":");
			int index = -1;
			for (int i = 0; i < formats.length; i++) {
				if (formats[i].equals(field)) {
					index = i;
				}
			}

			if (index == -1) {
				throw new IOException("field '" + field + "' not found in FORMAT");
			}
			String[] values = tiles[9].split("\t");
			for (int i = 0; i < genotypesParsed.length; i++) {
				String value = values[i];
				String[] tiles2 = value.split(":");
				String genotype = tiles2[index];

				float dosage = 0;
				// genotypes
				if (genotype.equals("0|0")) {
					dosage = 0;
				} else if (genotype.equals("0|1") || genotype.equals("1|0")) {
					dosage = 1;
				} else if (genotype.equals("1|1")) {
					dosage = 2;
				} else {
					// dosage
					dosage = Float.parseFloat(genotype);
				}
				genotypesParsed[i] = dosage;

			}
			
			dirtyGenotypes = false;

		}
		return genotypesParsed;
	}

}
