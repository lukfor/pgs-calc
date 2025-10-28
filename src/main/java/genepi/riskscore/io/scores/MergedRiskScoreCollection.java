package genepi.riskscore.io.scores;

import genepi.io.text.LineReader;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.csv.CsvWithHeaderTableReader;
import genepi.riskscore.io.csv.TabixTableReader;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreSummary;

import java.io.File;
import java.util.*;

public class MergedRiskScoreCollection implements IRiskScoreCollection {

	private String build;

	private String name;

	private String version;

	private RiskScoreSummary[] summaries;

	private int numberRiskScores;

	private String filename;

	private String includeScoresFilename;

	private boolean verbose = false;

	private Map<String, MergedVariant> variantsIndex = new HashMap<String, MergedVariant>();

	public static String HEADER = "# PGS-Collection v1";

	public static String META_EXTENSION = ".info";

	public static String INDEX_EXTENSION = ".tbi";

	public static  String COLUMN_CHROMOSOME = "chr_name";

	public static  String COLUMN_POSITION = "chr_position";

	public static  String COLUMN_EFFECT_ALLELE = "effect_allele";

	public static  String COLUMN_OTHER_ALLELE = "other_allele";

	public static Set<String> COLUMNS = new HashSet<String>();

	private ReferenceVariant reference = new ReferenceVariant();

	static {
		COLUMNS.add(COLUMN_CHROMOSOME);
		COLUMNS.add(COLUMN_POSITION);
		COLUMNS.add(COLUMN_EFFECT_ALLELE);
		COLUMNS.add(COLUMN_OTHER_ALLELE);
	}

	public MergedRiskScoreCollection(String filename, String includeScoresFilename) {
		this.filename = filename;
		this.includeScoresFilename = includeScoresFilename;
	}

	@Override
	public String getBuild() {
		return build;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void buildIndex(String chromosome, Chunk chunk, String dbsnp, String proxies) throws Exception {

		List<String> includedScores = new Vector<String>();

		if (includeScoresFilename != null) {
			LineReader lineReader = new LineReader(includeScoresFilename);
			while(lineReader.next()){
				String score = lineReader.get().trim();
				if (!score.isEmpty()) {
					includedScores.add(score);
				}
			}
			lineReader.close();
		}

		String metaFilename = filename + META_EXTENSION;
		File metaFile = new File(metaFilename);
		if (!metaFile.exists()){
			throw new RuntimeException("No meta file '" + metaFilename + "' found.");
		}

		Map<String, Integer> metaIndex = new HashMap<String, Integer>();
		Map<String, Integer> metaIndex2 = new HashMap<String, Integer>();
		CsvWithHeaderTableReader readerMeta = new CsvWithHeaderTableReader(metaFilename, '\t');
		while(readerMeta.next()){
			metaIndex.put(readerMeta.getString("score"), readerMeta.getInteger("variants"));
			metaIndex2.put(readerMeta.getString("score"), readerMeta.getInteger("ignored"));
		}
		readerMeta.close();

		if (chunk == null){
			chunk = new Chunk();
		}
		TabixTableReader reader = new TabixTableReader(filename, chromosome, chunk.getStart(), chunk.getEnd());
		String[] columns = reader.getColumns();

		Map<String, Integer> scoreToColumn = new HashMap<String, Integer>();
		summaries = new RiskScoreSummary[numberRiskScores];
		for (int i = 0; i < columns.length; i++){
			String column = columns[i];
			if (COLUMNS.contains(column)){
				continue;
			}
			if (!includedScores.isEmpty() && !includedScores.contains(column)){
				continue;
			}
			scoreToColumn.put(column, i);
		}


		numberRiskScores = scoreToColumn.size();
		summaries = new RiskScoreSummary[numberRiskScores];
		int index = 0;
		for (String column: columns){
			if (!scoreToColumn.containsKey(column)){
				continue;
			}
			summaries[index] = new RiskScoreSummary(column);
			summaries[index].setVariants(metaIndex.get(column));
			summaries[index].setVariantsIgnored(0);//TODO: merge sums them up. metaIndex2.get(column));
			index++;
		}


		int total = 0;

		while(reader.next()) {

			String _chromosome = reader.getString(COLUMN_CHROMOSOME);
			int position = reader.getInteger(COLUMN_POSITION);
			String otherAllele = reader.getString(COLUMN_OTHER_ALLELE);
			String effectAllele = reader.getString(COLUMN_EFFECT_ALLELE);

			String key = createKey(position, effectAllele, otherAllele);
			MergedVariant variant = null;
			if (variantsIndex.containsKey(key)) {
				variant = variantsIndex.get(key);
			} else{
				variant = new MergedVariant(effectAllele, otherAllele);
				variantsIndex.put(key, variant);
			}
			index = 0;
			for (String column: columns){
				if (!scoreToColumn.containsKey(column)){
					continue;
				}
				if (reader.getString(column).equals("") || reader.getString(column).equals(".")){
					index++;
					continue;
				}
				Double weight = reader.getDouble(column);
				variant.add(index, weight.floatValue());
				index++;
			}

		}

		reader.close();

		if (verbose) {
			System.out.println();
			System.out.println("Collection contains " + total + " weights for chromosome " + chromosome);
			System.out.println();
		}
	}

	@Override
	public RiskScoreSummary getSummary(int index) {
		return summaries[index];
	}


	@Override
	public boolean contains(int position, String alleleA, String alleleB) {
		String key = createKey(position, alleleA, alleleB);
		return variantsIndex.containsKey(key);
	}
	@Override
	public boolean contains(int index, int position, String alleleA, String alleleB) {
		String key = createKey(position, alleleA, alleleB);
		if(!variantsIndex.containsKey(key)){
			return false;
		}
        return variantsIndex.get(key).contains(index);
    }

	@Override
	public ReferenceVariant getVariant(int index, int position,  String alleleA, String alleleB) {
		String key = createKey(position, alleleA, alleleB);
		MergedVariant a = variantsIndex.get(key);
		//ReferenceVariant reference = new ReferenceVariant(a.otherAllele, a.effectAllele, a.getWeight(index));
		reference.setOtherAllele(a.otherAllele);
		reference.setEffectAllele(a.effectAllele);
		reference.setEffectWeight(a.getWeight(index));
		reference.setUsed(false);
		return reference;
	}

	@Override
	public List<Integer> getUniquePositions() {
		Set<Integer> positions = new TreeSet<>(); // automatically unique & sorted
		for (String key : variantsIndex.keySet()) {
			String position = key.split("_")[0];
			positions.add(Integer.parseInt(position));
		}
		return new ArrayList<>(positions);
	}

	@Override
	public Set<Map.Entry<Integer, ReferenceVariant>> getAllVariants(int index) {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public void clearIndex() {
		variantsIndex = null;
	}

	@Override
	public int getSize() {
		return numberRiskScores;
	}

	@Override
	public boolean isEmpty() {
		return variantsIndex.isEmpty();
	}

	@Override
	public RiskScoreSummary[] getSummaries() {
		return summaries;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	public class MergedVariant {

		private String effectAllele = "";

		private String otherAllele = "";

		private Map<Integer, Float> weights;

		public MergedVariant(String effectAllele, String otherAllele){
			this.effectAllele = effectAllele;
			this.otherAllele = otherAllele;
			weights = new HashMap<Integer, Float>();
		}

		public void add(int score, float weight){
			weights.put(score, weight);
		}

		public float getWeight(int score){
			return weights.get(score);
		}

		public boolean contains(int score) {
			return weights.containsKey(score);
		}

	}

	public static String createKey(int position, String alleleA, String alleleB){
		if (alleleA.compareTo(alleleB) < 1){
			return position + "_" + alleleA + "_" + alleleB;
		} else {
			return position + "_" + alleleB + "_" + alleleA;
		}
	}

}
