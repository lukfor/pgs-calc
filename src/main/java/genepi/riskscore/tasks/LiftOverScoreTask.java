package genepi.riskscore.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import htsjdk.samtools.liftover.LiftOver;
import htsjdk.samtools.util.Interval;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class LiftOverScoreTask implements ITaskRunnable {

	private String input = null;

	private String output = null;

	private int total = 0;

	private int resolved = 0;

	private int failed = 0;

	private int ignored;

	private String chainFile;

	public static boolean VERBOSE = false;

	public static final Map<Character, Character> ALLELE_SWITCHES = new HashMap<Character, Character>();

	static {
		ALLELE_SWITCHES.put('A', 'T');
		ALLELE_SWITCHES.put('T', 'A');
		ALLELE_SWITCHES.put('G', 'C');
		ALLELE_SWITCHES.put('C', 'G');
	}

	public LiftOverScoreTask(String input, String output, String chainFile) {
		this.input = input;
		this.output = output;
		this.chainFile = chainFile;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		RiskScoreFormatImpl format = new PGSCatalogFormat(input, false);
		if (new File(input + ".format").exists()) {
			format = RiskScoreFormatImpl.load(input + ".format");
		}

		CsvTableReader reader = new CsvTableReader(input, PGSCatalogFormat.SEPARATOR);
		CsvTableWriter writer = new CsvTableWriter(output, PGSCatalogFormat.SEPARATOR, false);

		LiftOver liftOver = new LiftOver(new File(chainFile));

		int row = 0;
		writer.setColumns(reader.getColumns());
		try {

			while (reader.next()) {

				row++;

				writer.setRow(reader.getRow());

				String orginalContig = reader.getString(format.getChromosome());

				boolean ignore = false;

				if (reader.getString(format.getPosition()).isEmpty()) {
					log("Warning: Row " + row + ": Position is empty. Ignore variant.");
					ignored++;
					ignore = true;
				}
				int originalPosition = 0;
				if (!ignore) {
					try {
						originalPosition = reader.getInteger(format.getPosition());

					} catch (NumberFormatException e) {
						log("Warning: Row " + row + ": '" + reader.getString(format.getPosition())
								+ "' is an invalid position. Ignore variant.");
						ignored++;
						ignore = true;
					}
				}

				String effectAllele = reader.getString(format.getEffectAllele());
				String otherAllele = reader.getString(format.getOtherAllele());

				if (!ignore) {

					String contig = "";
					String newContig = "";

					if (orginalContig.equals("chr23")) {
						orginalContig = "chrX";
					}
					if (orginalContig.equals("23")) {
						orginalContig = "X";
					}

					if (orginalContig.startsWith("chr")) {
						contig = orginalContig;
						newContig = orginalContig.replaceAll("chr", "");

					} else {
						contig = "chr" + orginalContig;
						newContig = orginalContig;
					}

					String id = orginalContig + ":" + originalPosition;

					int length = otherAllele.length();
					if (length == 0) {
						length = 1;
					}
					int start = originalPosition;
					int stop = originalPosition + length - 1;

					Interval source = new Interval(contig, start, stop, false, id);

					Interval target = liftOver.liftOver(source);

					if (target != null) {

						if (source.getContig().equals(target.getContig())) {

							if (length != target.length()) {

								log(id + "\t" + "LiftOver" + "\t" + "INDEL_STRADDLES_TWO_INTERVALS. SNP removed.");
								ignore = true;
								failed++;

							} else {

								if (target.isNegativeStrand()) {

									writer.setString(format.getEffectAllele(), flip(effectAllele));
									writer.setString(format.getOtherAllele(), flip(otherAllele));
								}

								if (otherAllele != null && effectAllele != null) {

									writer.setString(format.getChromosome(), newContig);
									writer.setInteger(format.getPosition(), target.getStart());
									resolved++;

								} else {

									log(id + "\t" + "LiftOver" + "\t" + "Indel on negative strand. SNP removed.");
									ignore = true;
									failed++;
								}
							}

						} else {

							log(id + "\t" + "LiftOver" + "\t" + "On different chromosome after LiftOver. SNP removed.");
							ignore = true;
							failed++;

						}

					} else {
						log(id + "\t" + "LiftOver" + "\t" + "LiftOver failed. SNP removed.");
						ignore = true;
						failed++;

					}
				}

				if (ignore) {
					writer.setString(format.getChromosome(), "");
					writer.setString(format.getPosition(), "");
					writer.setString(format.getEffectAllele(), "");
					writer.setString(format.getEffectWeight(), "");
					writer.setString(format.getOtherAllele(), "");
				}
				writer.next();

				total++;
			}

		} catch (Exception e) {
			throw e;
		} finally {
			writer.close();
			reader.close();
		}

	}

	protected static String flip(String allele) {
		String flippedAllele = "";
		for (int i = 0; i < allele.length(); i++) {
			Character flipped = ALLELE_SWITCHES.get(allele.charAt(i));
			flippedAllele += flipped;
		}
		return flippedAllele;
	}

	protected void log(String message) {
		if (VERBOSE) {
			System.out.println(message);
		}
	}

	public int getTotal() {
		return total;
	}

	public int getResolved() {
		return resolved;
	}

	public int getFailed() {
		return failed;
	}

	public int getIgnored() {
		return ignored;
	}

}
