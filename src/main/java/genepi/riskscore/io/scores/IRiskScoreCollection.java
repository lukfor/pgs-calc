package genepi.riskscore.io.scores;

import java.util.Set;
import java.util.SortedSet;

import genepi.riskscore.io.Chunk;
import genepi.riskscore.model.ReferenceVariant;
import genepi.riskscore.model.RiskScoreSummary;

public interface IRiskScoreCollection {

	public String getBuild();
	
	public String getName(); 
	
	public String getVersion();

	public void buildIndex(String chromosome, Chunk chunk, String dbsnp) throws Exception;
	
	public Set<String> getAllChromosomes(String dbsnp) throws Exception;

	public RiskScoreSummary getSummary(int index);

	public boolean contains(int index, int position);

	public ReferenceVariant getVariant(int index, int position);

	public int getSize();
	
	public boolean isEmpty();

	public RiskScoreSummary[] getSummaries();
	
	public SortedSet<Integer> getAllPositions();

}
