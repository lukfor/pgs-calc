package genepi.riskscore.tasks;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.dbsnp.DbSnp;
import genepi.riskscore.io.dbsnp.DbSnpReader;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class ConvertRsIdsTask implements ITaskRunnable {

	private String input = null;

	private String output = null;

	private String dbsnpVersion = "150";

	private String dbsnpBuild = "hg19";

	private int found = 0;

	private int total = 0;

	public ConvertRsIdsTask(String input, String output, String dbsnpVersion, String dbsnpBuild) {
		this.input = input;
		this.output = output;
		this.dbsnpVersion = dbsnpVersion;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		System.out.println("Converting score file " + input + "...");
		CsvTableReader reader = new CsvTableReader(input, '\t');
		CsvTableWriter writer = new CsvTableWriter(output, '\t', false);

		PGSCatalogFormat format = new PGSCatalogFormat();

		String dbsnpFilename = DbSnp.getFilename(dbsnpVersion, dbsnpBuild);

		DbSnpReader dbSnpReader = new DbSnpReader(dbsnpFilename);

		writer.setColumns(new String[] { format.getChromosome(), format.getPosition(), format.getEffect_allele(),
				format.getEffect_weight(), format.getAllele_b(), "rsId" });
		while (reader.next()) {
			String rsId = reader.getString("rsId");
			String effectAllele = reader.getString("effect_allele");
			String effectWeight = reader.getString("effect_weight");
			DbSnpReader.Snp snp = dbSnpReader.getByRsId(rsId);
			if (snp != null) {
				found++;
				writer.setString(format.getChromosome(), snp.getChromosome().replaceAll("chr", ""));
				writer.setString(format.getPosition(), snp.getPosition() + "");
				writer.setString(format.getEffect_allele(), effectAllele);
				writer.setString(format.getEffect_weight(), effectWeight);
				writer.setString(format.getAllele_b(), snp.getReference());
				writer.setString("rsId", rsId);
				writer.next();
			} else {
				writer.setString(format.getChromosome(), "-");
				writer.setString(format.getPosition(), "");
				writer.setString(format.getEffect_allele(), "-");
				writer.setString(format.getEffect_weight(), "-");
				writer.setString(format.getAllele_b(), "-");
				writer.setString("rsId", rsId);
			}
			total++;
		}
		writer.close();
		reader.close();

		System.out.println("File converted. Snps found: " + found + "/" + total);

	}

	public int getTotal() {
		return total;
	}

	public int getFound() {
		return found;
	}

}
