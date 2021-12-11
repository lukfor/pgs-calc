package genepi.riskscore.tasks;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.dbsnp.DbSnpReader;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class ConvertRsIdsTask implements ITaskRunnable {

	private String input = null;

	private String output = null;

	private int found = 0;

	private int total = 0;

	private String dbsnpFilename;

	public ConvertRsIdsTask(String input, String output, String dbsnpFilename) {
		this.input = input;
		this.output = output;
		this.dbsnpFilename = dbsnpFilename;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		PGSCatalogFormat format = new PGSCatalogFormat(input);

		System.out.println("Converting score file " + input + "...");
		CsvTableReader reader = new CsvTableReader(input, '\t');
		CsvTableWriter writer = new CsvTableWriter(output, '\t', false);

		DbSnpReader dbSnpReader = new DbSnpReader(dbsnpFilename);

		writer.setColumns(new String[] { format.getChromosome(), format.getPosition(), format.getEffectAllele(),
				format.getEffectWeight(), format.getOtherAllele(), "rsId" });
		while (reader.next()) {
			String rsId = reader.getString("rsId");
			String effectAllele = reader.getString("effect_allele");
			String effectWeight = reader.getString("effect_weight");
			DbSnpReader.Snp snp = dbSnpReader.getByRsId(rsId);
			if (snp != null) {
				found++;
				writer.setString(format.getChromosome(), snp.getChromosome().replaceAll("chr", ""));
				writer.setString(format.getPosition(), snp.getPosition() + "");
				writer.setString(format.getEffectAllele(), effectAllele);
				writer.setString(format.getEffectWeight(), effectWeight);
				writer.setString(format.getOtherAllele(), snp.getReference());
				writer.setString("rsId", rsId);
				writer.next();
			} else {
				writer.setString(format.getChromosome(), "-");
				writer.setString(format.getPosition(), "");
				writer.setString(format.getEffectAllele(), "-");
				writer.setString(format.getEffectWeight(), "-");
				writer.setString(format.getOtherAllele(), "-");
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
