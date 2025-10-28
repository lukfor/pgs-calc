package genepi.riskscore.io.vcf;

import genepi.io.reader.IReader;

import java.util.List;

public interface IVCFFileReader extends IReader<MinimalVariantContext> {

    public List<String> getGenotypedSamples();

}
