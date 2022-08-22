package genepi.riskscore.commands;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import genepi.io.table.writer.CsvTableWriter;
import genepi.riskscore.App;
import genepi.riskscore.io.vcf.FastVCFFileReader;
import genepi.riskscore.io.vcf.MinimalVariantContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create-chunks", version = App.VERSION)
public class CreateChunksCommand implements Callable<Integer> {

	@Parameters(description = "VCF files")
	List<String> vcfs;

	@Option(names = { "--size" }, description = "Chunk Size", required = true, showDefaultValue = Visibility.ALWAYS)
	int chunkSize = 0;

	@Option(names = {
			"--out" }, description = "Output Chunk Filename)", required = true, showDefaultValue = Visibility.ALWAYS)
	String output = "";

	public Integer call() throws Exception {

		CsvTableWriter writer = new CsvTableWriter(output);
		writer.setColumns(new String[] { "CONTIG", "START", "END", "VARIANTS", "FILENAME" });

		int countChunks = 0;

		for (String vcf : vcfs) {

			System.out.println("Reading file '" + vcf + "'...");

			FastVCFFileReader reader = new FastVCFFileReader(vcf);
			String contig = null;

			Map<Integer, VcfChunk> chunks = new ConcurrentHashMap<Integer, VcfChunk>();

			while (reader.next()) {
				MinimalVariantContext variant = reader.get();

				if (contig != null && !variant.getContig().equals(contig)) {
					System.out.println("Error: Multiple contigs in file " + vcf + ". Found contigs '" + contig
							+ "' and '" + variant.getContig() + "'");
					return 1;
				}
				contig = variant.getContig();

				int chunkNumber = variant.getStart() / chunkSize;
				if (variant.getStart() % chunkSize == 0) {
					chunkNumber = chunkNumber - 1;
				}
				VcfChunk chunk = chunks.get(chunkNumber);
				if (chunk == null) {
					int chunkStart = chunkNumber * chunkSize + 1;
					int chunkEnd = chunkStart + chunkSize - 1;
					chunk = new VcfChunk(contig, chunkStart, chunkEnd);
					chunks.put(chunkNumber, chunk);
				}

				chunk.incVariants();

			}
			reader.close();
			for (VcfChunk chunk : chunks.values()) {
				writer.setString("CONTIG", chunk.getContig());
				writer.setInteger("START", chunk.getStart());
				writer.setInteger("END", chunk.getEnd());
				writer.setInteger("VARIANTS", chunk.getVariants());
				writer.setString("FILENAME", vcf);
				writer.next();
				countChunks++;
			}
		}

		writer.close();

		System.out.println("Done. Wrote " + countChunks + " to file '" + output + "'.");

		return 0;
	}

	class VcfChunk {

		private String contig;

		private int start;

		private int end;

		private int variants = 0;

		public VcfChunk(String contig, int start, int end) {
			super();
			this.contig = contig;
			this.start = start;
			this.end = end;
		}

		public String getContig() {
			return contig;
		}

		public void setContig(String contig) {
			this.contig = contig;
		}

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		public int getVariants() {
			return variants;
		}

		public void setVariants(int variants) {
			this.variants = variants;
		}

		public void incVariants() {
			this.variants++;
		}

	}

}
