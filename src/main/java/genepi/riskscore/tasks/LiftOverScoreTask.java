package genepi.riskscore.tasks;

import java.io.File;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.io.formats.PGSCatalogFormat;
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

	public LiftOverScoreTask(String input, String output, String chainFile) {
		this.input = input;
		this.output = output;
		this.chainFile = chainFile;
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		PGSCatalogFormat format = new PGSCatalogFormat(input);

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
				try {
					originalPosition = reader.getInteger(format.getPosition());

				} catch (NumberFormatException e) {
					log("Warning: Row " + row + ": '" + reader.getString(format.getPosition())
							+ "' is an invalid position. Ignore variant.");
					ignored++;
					ignore = true;
				}

				if (!ignore) {

					String contig = "";
					String newContig = "";
					if (orginalContig.startsWith("chr")) {
						contig = orginalContig;
						newContig = orginalContig.replaceAll("chr", "");

					} else {
						contig = "chr" + orginalContig;
						newContig = orginalContig;
					}

					String id = orginalContig + ":" + originalPosition;

					Interval source = new Interval(contig, originalPosition, originalPosition, false, id);
					Interval target = liftOver.liftOver(source);
					if (target != null) {
						if (source.getContig().equals(target.getContig())) {
							writer.setString(format.getChromosome(), newContig);
							writer.setInteger(format.getPosition(), target.getStart());
							resolved++;
						} else {
							writer.setString(format.getChromosome(), "");
							writer.setString(format.getPosition(), "");
							writer.setString(format.getEffectAllele(), "");
							writer.setString(format.getEffectWeight(), "");
							writer.setString(format.getOtherAllele(), "");
							failed++;
							log(id + " LiftOver: On different chromosome after LiftOver. SNP removed.");
						}
					} else {
						writer.setString(format.getChromosome(), "");
						writer.setString(format.getPosition(), "");
						writer.setString(format.getEffectAllele(), "");
						writer.setString(format.getEffectWeight(), "");
						writer.setString(format.getOtherAllele(), "");
						failed++;
						log(id + " LiftOver failed. SNP removed.");
					}
				} else {
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

	protected void log(String message) {
		System.out.println(message);
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
