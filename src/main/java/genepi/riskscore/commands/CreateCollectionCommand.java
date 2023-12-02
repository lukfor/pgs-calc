package genepi.riskscore.commands;

import java.util.*;
import java.util.concurrent.Callable;

import genepi.io.table.reader.ITableReader;
import genepi.io.table.writer.ITableWriter;
import genepi.riskscore.App;
import genepi.riskscore.io.RiskScoreFile;
import genepi.riskscore.io.csv.CsvWithHeaderTableReader;
import genepi.riskscore.io.csv.CsvWithHeaderTableWriter;
import genepi.riskscore.io.formats.PGSCatalogFormat;
import genepi.riskscore.io.formats.PGSCatalogHarmonizedFormat;
import genepi.riskscore.io.formats.RiskScoreFormatImpl;
import genepi.riskscore.io.scores.MergedRiskScoreCollection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create-collection", version = App.VERSION)
public class CreateCollectionCommand implements Callable<Integer> {

    @Option(names = "--out", description = "output score file", required = false)
    private String output = null;

    @Parameters(description = "score files")
    private String[] filenames;

    public static String[] chromosomeOrder = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "XY"};

    public static Map<String, Integer> chromosomeOrderIndex = new HashMap<String, Integer>();

    static {
        for (int i = 0; i < chromosomeOrder.length; i++) {
            chromosomeOrderIndex.put(chromosomeOrder[i], i);
        }
    }

    @Override
    public Integer call() throws Exception {

        String[] names = new String[filenames.length];
        CsvWithHeaderTableReader[] readers = new CsvWithHeaderTableReader[filenames.length];
        RiskScoreFormatImpl[] formats = new RiskScoreFormatImpl[filenames.length];
        Variant[] variants = new Variant[filenames.length];

        for (int i = 0; i < filenames.length; i++) {
            names[i] = RiskScoreFile.getName(filenames[i]);
            formats[i] = new PGSCatalogHarmonizedFormat();
            readers[i] = new CsvWithHeaderTableReader(filenames[i], formats[i].getSeparator());
            try {
                variants[i] = readVariant(readers[i], formats[i]);
            } catch (Exception e) {
                throw new RuntimeException("File " + filenames[i], e);
            }
        }

        List<String> header = new Vector<String>();
        header.add(MergedRiskScoreCollection.HEADER);
        header.add("#Date=" + new Date());
        header.add("#Scores=" + filenames.length);

        CsvWithHeaderTableWriter writer = null;
        if (output != null) {
            writer = new CsvWithHeaderTableWriter(output, '\t', header);
        } else {
            writer = new CsvWithHeaderTableWriter('\t', header);
        }

        String[] variantColumns = new String[]{MergedRiskScoreCollection.COLUMN_CHROMOSOME,
                MergedRiskScoreCollection.COLUMN_POSITION, MergedRiskScoreCollection.COLUMN_EFFECT_ALLELE,
                MergedRiskScoreCollection.COLUMN_OTHER_ALLELE};
        String[] columns = merge(variantColumns, names);
        writer.setColumns(columns);

        int variantsWritten = 0;

        while (!isEmpty(variants)) {
            Variant variant = findMinVariant(variants);
            addVariant(writer, variant);
            for (int i = 0; i < variants.length; i++) {
                if (variants[i] != null && variants[i].matches(variant)) {
                    writeVariant(writer, names[i], variants[i].getNormalizedEffect(variant));
                    Variant nextVariant = null;
                    try {
                        nextVariant = readVariant(readers[i], formats[i]);
                    } catch (Exception e) {
                        throw new RuntimeException("File " + filenames[i], e);
                    }
                    if (nextVariant != null && nextVariant.isBefore(variants[i])) {
                        throw new RuntimeException(filenames[i] + ": Not sorted. " + nextVariant + " is before " + variants[i]);
                    }
                    variants[i] = nextVariant;
                } else {
                    writeMissing(writer, names[i]);
                }
            }
            writer.next();
            variantsWritten++;
        }


        writer.close();

        for (ITableReader reader : readers) {
            reader.close();
        }

        System.err.println("Wrote " + variantsWritten + " unique variants and " + filenames.length + " scores.");

        return 0;

    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setFilenames(String[] filenames) {
        this.filenames = filenames;
    }


    public boolean isEmpty(Variant[] variants) {
        for (Variant variant : variants) {
            if (variant != null) {
                return false;
            }
        }
        return true;
    }

    public Variant findMinVariant(Variant[] variants) {
        Variant minVariant = variants[0];
        for (int i = 1; i < variants.length; i++) {
            if (minVariant == null) {
                minVariant = variants[i];
                continue;
            }
            if (variants[i] == null) {
                continue;
            }
            if (variants[i].isBefore(minVariant)) {
                minVariant = variants[i];
            }
        }
        return minVariant;
    }

    private String[] merge(String[] first, String[] second) {
        int fal = first.length;
        int sal = second.length;
        String[] result = new String[fal + sal];
        System.arraycopy(first, 0, result, 0, fal);
        System.arraycopy(second, 0, result, fal, sal);
        return result;
    }

    public Variant readVariant(ITableReader reader, RiskScoreFormatImpl format) {
        if (!reader.next()) {
            return null;
        }
        Variant variant = new Variant();
        variant.setChromosome(reader.getString(format.getChromosome()));
        if (reader.getString(format.getPosition()).isEmpty()) {
            throw new RuntimeException("Not position found.");
        }
        variant.setPosition(reader.getInteger(format.getPosition()));
        variant.setEffectAllele(reader.getString(format.getEffectAllele()));
        variant.setOtherAllele(reader.getString(format.getOtherAllele()));
        variant.setEffect(reader.getDouble(format.getEffectWeight()));
        return variant;
    }


    public void addVariant(ITableWriter writer, Variant variant) {
        writer.setString(MergedRiskScoreCollection.COLUMN_CHROMOSOME, variant.getChromosome());
        writer.setInteger(MergedRiskScoreCollection.COLUMN_POSITION, variant.getPosition());
        writer.setString(MergedRiskScoreCollection.COLUMN_EFFECT_ALLELE, variant.getEffectAllele());
        writer.setString(MergedRiskScoreCollection.COLUMN_OTHER_ALLELE, variant.getOtherAllele());
    }

    public void writeVariant(ITableWriter writer, String score, double effect) {
        writer.setDouble(score, effect);
    }

    public void writeMissing(ITableWriter writer, String score) {
        writer.setString(score, "");
    }

    public class Variant {
        private int position = 0;
        private String chromosome = null;
        private double effect = 0;
        private String effectAllele = null;
        private String otherAllele = null;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getChromosome() {
            return chromosome;
        }

        public void setChromosome(String chromosome) {
            this.chromosome = chromosome;
        }

        public double getEffect() {
            return effect;
        }

        public void setEffect(double effect) {
            this.effect = effect;
        }

        public String getEffectAllele() {
            return effectAllele;
        }

        public void setEffectAllele(String effectAllele) {
            this.effectAllele = effectAllele;
        }

        public String getOtherAllele() {
            return otherAllele;
        }

        public void setOtherAllele(String otherAllele) {
            this.otherAllele = otherAllele;
        }

        public double getNormalizedEffect(Variant variant) {
            if (this.hasSameAlleles(variant)) {
                return effect;
            }

            if (this.hasSwappedAlleles(variant)) {
                return -effect;
            }

            throw new RuntimeException("Error. Wrong alleles!!");
        }

        private boolean hasSameAlleles(Variant variant) {
            return this.effectAllele.equals(variant.effectAllele) && this.otherAllele.equals(variant.otherAllele);
        }

        private boolean hasSwappedAlleles(Variant variant) {
            return this.effectAllele.equals(variant.otherAllele) && this.otherAllele.equals(variant.effectAllele);
        }

        public boolean hasSamePosition(Variant variant) {
            return this.getPosition() == variant.getPosition() && this.getChromosome().equals(variant.getChromosome());
        }

        private int getChromosomeOrder(String chr) {
            if (chromosomeOrderIndex.containsKey(chr)) {
                return chromosomeOrderIndex.get(chr);
            }

            throw new RuntimeException("Unknown Chromosome: " + chr);
        }

        public int compare(Variant variant) {
            int chrOrderA = getChromosomeOrder(getChromosome());
            int chrOrderB = getChromosomeOrder(variant.getChromosome());

            if (chrOrderA == chrOrderB) {
                return Integer.compare(getPosition(), variant.getPosition());
            } else if (chrOrderA < chrOrderB) {
                return -1;
            } else {
                return 1;
            }

        }

        public boolean isBefore(Variant variant) {
            return this.compare(variant) < 0;
        }

        @Override
        public String toString() {
            return chromosome + ":" + position;
        }

        public boolean matches(Variant variant) {
            return hasSamePosition(variant) && (hasSameAlleles(variant) || hasSwappedAlleles(variant));
        }
    }

}

