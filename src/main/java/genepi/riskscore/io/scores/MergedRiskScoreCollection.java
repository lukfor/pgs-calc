package genepi.riskscore.io.scores;

import genepi.io.table.reader.ITableReader;
import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.VariantFile;
import genepi.riskscore.io.csv.CsvWithHeaderTableReader;
import genepi.riskscore.io.csv.TabixTableReader;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreSummary;
import jxl.format.PaperSize;

import java.io.File;
import java.util.*;

public class MergedRiskScoreCollection implements IRiskScoreCollection {

	private String build;

	private String name;

	private String version;

	private RiskScoreSummary[] summaries;

	private int numberRiskScores;

	private String filename;

	private boolean verbose = false;

	private Map<Integer, Map<Integer, ReferenceVariant>> variantsIndex = new HashMap<Integer, Map<Integer, ReferenceVariant>>();

	public static String HEADER = "# PGS-Collection v1";

	public static String META_EXTENSION = ".info";

	public static String INDEX_EXTENSION = ".tbi";

	public static  String COLUMN_CHROMOSOME = "chr_name";

	public static  String COLUMN_POSITION = "chr_position";

	public static  String COLUMN_EFFECT_ALLELE = "effect_allele";

	public static  String COLUMN_OTHER_ALLELE = "other_allele";

	public static Set<String> COLUMNS = new HashSet<String>();

	static {
		COLUMNS.add(COLUMN_CHROMOSOME);
		COLUMNS.add(COLUMN_POSITION);
		COLUMNS.add(COLUMN_EFFECT_ALLELE);
		COLUMNS.add(COLUMN_OTHER_ALLELE);
	}

	public MergedRiskScoreCollection(String filename) {
		this.filename = filename;
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

		numberRiskScores = columns.length - COLUMNS.size();

		summaries = new RiskScoreSummary[numberRiskScores];
		int index = 0;
		for (String column: columns){
			if (COLUMNS.contains(column)){
				continue;
			}
			summaries[index] = new RiskScoreSummary(column);
			summaries[index].setVariants(metaIndex.get(column));
			summaries[index].setVariantsIgnored(0);//TODO: merge sums them up. metaIndex2.get(column));
			variantsIndex.put(index, new HashMap<Integer, ReferenceVariant>());
			index++;
		}


		int total = 0;

		while(reader.next()){

			String _chromosome = reader.getString(COLUMN_CHROMOSOME);
			int position = reader.getInteger(COLUMN_POSITION);
			String otherAllele = reader.getString(COLUMN_OTHER_ALLELE);
			String effectAllele = reader.getString(COLUMN_EFFECT_ALLELE);

			index = 0;
			for (String column: columns){
				if (COLUMNS.contains(column)){
					continue;
				}
				if (reader.getString(column).equals("") || reader.getString(column).equals(".")){
					index++;
					continue;
				}
				Double weight = reader.getDouble(column);
				//TODO: rethink: better position -> score index? easier to check..
				ReferenceVariant variant = new ReferenceVariant(otherAllele, effectAllele, weight.floatValue());
				variantsIndex.get(index).put(position, variant);
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
	public boolean contains(int index, int position) {
		if(!variantsIndex.containsKey(index)){
			return false;
		}
		if (!variantsIndex.get(index).containsKey(position)){
			return false;
		}
		return true;
	}

	@Override
	public ReferenceVariant getVariant(int index, int position) {
		return variantsIndex.get(index).get(position);
	}

	@Override
	public Set<Map.Entry<Integer, ReferenceVariant>> getAllVariants(int index) {
		return variantsIndex.get(index).entrySet();
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

}
