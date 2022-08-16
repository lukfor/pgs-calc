package genepi.riskscore.commands;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import genepi.io.text.LineReader;
import genepi.io.text.LineWriter;
import genepi.riskscore.App;
import genepi.riskscore.io.dbsnp.DbSnpReader;
import genepi.riskscore.io.dbsnp.DbSnpReader.Snp;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;
import lukfor.progress.util.CountingInputStream;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help.Visibility;

@Command(name = "proxy", version = App.VERSION)
public class ProcessHaploRegCommand implements Callable<Integer> {

	@Option(names = { "--in" }, description = "Input filename", required = true)
	String input;

	@Option(names = { "--out" }, description = "Output filename", required = true)
	String output;

	@Option(names = { "--r2" }, description = "LD threshold", required = false)
	double r2 = 0.95;

	@Option(names = { "--dbsnp" }, description = "dbsnp index", required = true)
	String dbsnp = "";

	@Option(names = {
			"--no-ansi" }, description = "Disable ANSI output", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean noAnsi = false;

	@Option(names = { "--ids" }, description = "rsIds of snps used in index", required = false)
	String ids = null;

	
	@Override
	public Integer call() throws Exception {

		if (noAnsi) {
			TaskService.setAnimated(false);
			TaskService.setAnsiColors(false);
		}

		ProcessHaploRegTask task = new ProcessHaploRegTask();
		TaskService.monitor(App.STYLE_LONG_TASK).run(task);

		return 0;

	}

	public static String join(List<String> alleles) {
		String result = "";
		for (int i = 0; i < alleles.size(); i++) {
			if (i > 0) {
				result += ";";
			}
			result += alleles.get(i);
		}
		return result;
	}

	class ProcessHaploRegTask implements ITaskRunnable {

		@Override
		public void run(ITaskMonitor monitor) throws Exception {

			monitor.begin("Parse file", new File(input).length());

			CountingInputStream countingStream = new CountingInputStream(new FileInputStream(input), monitor);

			DbSnpReader dbSnpReader = new DbSnpReader(dbsnp);

			LineWriter writer = new LineWriter(output);

			LineReader reader = new LineReader(new DataInputStream(countingStream));

			Set<String> rsIds = new HashSet<String>();
			if (ids != null) {
				LineReader rsIdsReader = new LineReader(ids);
				while(rsIdsReader.next()) {
					String id = rsIdsReader.get();
					if (!id.trim().isEmpty()) {
					rsIds.add(id.trim());
					}
				}
				rsIdsReader.close();
			}
			
			while (reader.next()) {
				if (monitor.isCanceled()) {
					writer.close();
					reader.close();
					return;
				}

				List<String> finalProxies = new Vector<String>();

				String line = reader.get();
				String[] tiles = line.split("\t");
				String id = tiles[0];
				
				if (!rsIds.isEmpty()) {
					if (!rsIds.contains(id)) {
						continue;
					}
				}
				
				Snp snp = dbSnpReader.getByRsId(id);
				if (snp != null) {
					// Ignore multi allelic
					if (!snp.getAlternate().contains(",")) {
						String[] proxies = tiles[1].split(";");
						for (String proxy : proxies) {
							String[] proxyDetails = proxy.split(",");
							double proxyR2 = Double.parseDouble(proxyDetails[1]);
							if (proxyR2 >= r2) {
								Snp proxySnp = dbSnpReader.getByRsId(proxyDetails[0]);
								if (proxySnp != null) {
									// Remove multi allelic
									if (!proxySnp.getAlternate().contains(",")) {
										finalProxies.add(proxySnp.getChromosome() + ":" + proxySnp.getPosition() + ":"
												+ proxySnp.getReference() + ":" + proxySnp.getAlternate());
									}
								}
							}
						}

						if (!finalProxies.isEmpty()) {
							writer.write(snp.getChromosome() + "\t" + snp.getPosition() + "\t" + snp.getReference()
									+ "\t" + snp.getAlternate() + "\t" + join(finalProxies));
						}
					}
				}

			}
			reader.close();
			writer.close();

		}

	}

}
