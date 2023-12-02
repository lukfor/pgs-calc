package genepi.riskscore.io.scores;

import genepi.riskscore.io.Chunk;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.VariantFile;
import genepi.riskscore.io.csv.CsvWithHeaderTableReader;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreSummary;
import jxl.format.PaperSize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergedRiskScoreCollection implements IRiskScoreCollection {

	private String build;

	private String name;

	private String version;

	private RiskScoreSummary[] summaries;

	private int numberRiskScores;

	private String filename;

	private boolean verbose = false;

	private Map<Integer, Map<Integer, ReferenceVariant>> variantsIndex = new HashMap<Integer, Map<Integer, ReferenceVariant>>();

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

		CsvWithHeaderTableReader reader = new CsvWithHeaderTableReader(filename, '\t');
		String[] columns = reader.getColumns();

		numberRiskScores = columns.length - COLUMNS.size();

		summaries = new RiskScoreSummary[numberRiskScores];
		int index = 0;
		for (String column: columns){
			if (COLUMNS.contains(column)){
				continue;
			}
			summaries[index] = new RiskScoreSummary(column);
			variantsIndex.put(index, new HashMap<Integer, ReferenceVariant>());
			index++;
		}


		int total = 0;

		while(reader.next()){

			String _chromosome = reader.getString(COLUMN_CHROMOSOME);
			int position = reader.getInteger(COLUMN_POSITION);
			if (!_chromosome.equals(chromosome)){
				continue;
			}
			if (chunk != null) {
				if (position < chunk.getStart()) {
					continue;
				}

				if (position > chunk.getEnd()) {
					break;
				}
			}

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

				//TODO; just testing; we need all variants from whole file!
				summaries[index].incVariants();
				index++;
			}

			//TODO: total variants. meta file?
			//go to chunk or complete file.
			//summaries[i].setVariants(riskscore.getTotalVariants());
			//summaries[i].setVariantsIgnored(riskscore.getIgnoredVariants());

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
