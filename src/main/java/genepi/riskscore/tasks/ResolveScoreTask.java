package genepi.riskscore.tasks;

import genepi.riskscore.io.csv.CsvWithHeaderTableReader;
import genepi.riskscore.io.csv.CsvWithHeaderTableWriter;
import genepi.riskscore.io.dbsnp.DbSnpReader;
import genepi.riskscore.io.formats.RiskScoreFormatFactory;
import genepi.riskscore.io.formats.RiskScoreFormatFactory.RiskScoreFormat;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class ResolveScoreTask implements ITaskRunnable {

	private String input = null;

	private String output = null;

	private int found = 0;

	private int total = 0;

	private int otherAlleleSource = 0;

	private int otherAlleleAlternate = 0;

	private int otherAlleleReference = 0;

	private int ignoredNotInDbSnp = 0;

	private int ignoredMulAlternateAlleles = 0;

	private String dbsnpFilename;

	public static boolean VERBOSE = false;

	public ResolveScoreTask(String input, String output, String dbsnpFilename) {
		this.input = input;
		this.output = output;
		this.dbsnpFilename = dbsnpFilename;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		RiskScoreFormatImpl format = RiskScoreFormatFactory.buildFormat(input, RiskScoreFormat.AUTO_DETECT);

		CsvWithHeaderTableReader reader = new CsvWithHeaderTableReader(input, format.getSeparator());
		CsvWithHeaderTableWriter writer = new CsvWithHeaderTableWriter(output, format.getSeparator(), reader.getHeader());

		DbSnpReader dbSnpReader = new DbSnpReader(dbsnpFilename);

		writer.setColumns(new String[] { format.getChromosome(), format.getPosition(), format.getEffectAllele(),
				format.getEffectWeight(), format.getOtherAllele(), "rsId" });
		try {

			while (reader.next()) {

				String rsId = reader.getString("rsId");
				String effectAllele = reader.getString(format.getEffectAllele());
				String effectWeight = reader.getString(format.getEffectWeight());
				DbSnpReader.Snp snp = dbSnpReader.getByRsId(rsId);

				writer.setString("rsId", rsId);

				if (snp != null) {

					writer.setString(format.getChromosome(), snp.getChromosome().replaceAll("chr", ""));
					writer.setString(format.getPosition(), snp.getPosition() + "");
					writer.setString(format.getEffectAllele(), effectAllele);
					writer.setString(format.getEffectWeight(), effectWeight);

					String otherAllele = "";
					if (reader.hasColumn(format.getOtherAllele())
							&& !reader.getString(format.getOtherAllele()).isEmpty()) {
						otherAllele = reader.getString(format.getOtherAllele());
						otherAlleleSource++;
						found++;
					} else {
						if (snp.getReference().equals(effectAllele)) {
							if (!snp.getAlternate().contains(",")) {
								otherAllele = snp.getAlternate();
								otherAlleleAlternate++;
								found++;

							} else {
								warning("Ignore SNP " + rsId
										+ ": effect allele is reference allele and SNP has multiple alleles.");

								ignoredMulAlternateAlleles++;

							}
						} else {
							otherAllele = snp.getReference();
							otherAlleleReference++;
							found++;
						}
					}
					writer.setString(format.getOtherAllele(), otherAllele);

				} else {

					warning(" Ignore SNP " + rsId + ": not found in index.");

					writer.setString(format.getChromosome(), "");
					writer.setString(format.getPosition(), "");
					writer.setString(format.getEffectAllele(), "");
					writer.setString(format.getEffectWeight(), "");
					writer.setString(format.getOtherAllele(), "");

					ignoredNotInDbSnp++;

				}

				writer.next();

				total++;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			dbSnpReader.close();
			writer.close();
			reader.close();
		}

	}

	protected void warning(String message) {
		if (VERBOSE) {
			System.out.println("Warning: " + message);
		}
	}

	public int getTotal() {
		return total;
	}

	public int getResolved() {
		return found;
	}

	public int getOtherAlleleAlternate() {
		return otherAlleleAlternate;
	}

	public int getOtherAlleleReference() {
		return otherAlleleReference;
	}

	public int getOtherAlleleSource() {
		return otherAlleleSource;
	}

	public int getIgnoredMulAlternateAlleles() {
		return ignoredMulAlternateAlleles;
	}

	public int getIgnoredNotInDbSnp() {
		return ignoredNotInDbSnp;
	}

}
