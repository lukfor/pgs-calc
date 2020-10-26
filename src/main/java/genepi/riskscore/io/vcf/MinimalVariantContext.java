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
			String format = tiles[8];
			String[] formats = format.split(":");

			String[] values = tiles[9].split("\t");

			if (field.equals("DS")) {
				int indexDS = findField(formats, "DS");
				int indexGT = findField(formats, "GT");
				if (indexDS != -1) {
					parseDosages(values, indexDS, indexGT);
				} else {
					System.out.println("Variant '" + this + "': Field 'DS' not found in FORMAT. Try 'GT'");
					if (indexGT == -1) {
						throw new IOException(
								"Variant '" + this + "': Field 'GT' not found in FORMAT. Available: " + format);
					}
					parseGenotypes(values, indexGT);
				}

			} else {
				int indexGT = findField(formats, "GT");
				if (indexGT == -1) {
					throw new IOException(
							"Variant '" + this + "': Field 'GT' not found in FORMAT. Available: " + format);
				}
				parseGenotypes(values, indexGT);
			}

			dirtyGenotypes = false;

		}
		return genotypesParsed;
	}

	protected int findField(String[] formats, String field) {
		for (int i = 0; i < formats.length; i++) {
			if (formats[i].equals(field)) {
				return i;
			}
		}
		return -1;
	}

	protected void parseGenotypes(String[] values, int indexGT) {
		for (int i = 0; i < genotypesParsed.length; i++) {
			String value = values[i];
			String[] tiles2 = value.split(":");
			String genotype = tiles2[indexGT];
			float dosage = toDosage(genotype);
			genotypesParsed[i] = dosage;
		}
	}

	protected void parseDosages(String[] values, int indexDS, int indexGT) {
		for (int i = 0; i < genotypesParsed.length; i++) {
			String value = values[i];
			String[] tiles2 = value.split(":");
			String dosage = tiles2[indexDS];
			if (dosage.equals(".")) {
				// use genotype instead
				String genotype = tiles2[indexGT];
				genotypesParsed[i] = toDosage(genotype);
			} else {
				try {
					genotypesParsed[i] = Float.parseFloat(dosage);
				} catch (Exception e) {
					genotypesParsed[i] = -1;
				}
			}
		}
	}

	protected float toDosage(String genotype) {
		if (genotype.equals("0|0") || genotype.equals("0/0")) {
			return 0;
		} else if (genotype.equals("0|1") || genotype.equals("1|0") || genotype.equals("0/1")
				|| genotype.equals("1/0")) {
			return 1;
		} else if (genotype.equals("1|1") || genotype.equals("1/1")) {
			return 2;
		} else {
			return -1;
		}
	}

}
