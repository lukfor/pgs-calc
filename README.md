# PGS Calculator

[![Java CI with Maven](https://github.com/lukfor/pgs-calc/actions/workflows/maven.yml/badge.svg)](https://github.com/lukfor/pgs-calc/actions/workflows/maven.yml)[![codecov](https://codecov.io/gh/lukfor/pgs-calc/branch/master/graph/badge.svg)](https://codecov.io/gh/lukfor/pgs-calc)
[![GitHub release](https://img.shields.io/github/release/lukfor/pgs-calc.svg)](https://GitHub.com/lukfor/pgs-calc/releases/)

> Applying polygenic scores (PGS) on imputed genotypes

## Features

- command line program (works on linux or MacOS)
- supports vcf.gz files (imputed or genotyped)
- supports different filters (e.g. r2 or variant list)
- supports PGS Catalog format (https://www.pgscatalog.org, currently over 2,000 scores)
- creates an interactive html report
- supports liftover of score files
- supports converting rsID to positions

## Installation

- Download `pgs-calc-*.tar.gz` from [latest release](https://github.com/lukfor/pgs-calc/releases/latest)
- Extract the downloaded archive (e.g `tar -xf pgs-calc-*.tar.gz`)
- Validate installation with `pgs-calc --version`

## Usage

Applying polygenic scores (PGS) on imputed genotypes:

```
pgs-calc apply --ref PGS000018 --out PGS000018.scores.txt chr*.dose.noID.vcf.gz  --report-html PGS000018.html
```

The weights for score `PGS000018` are downloaded automatically from PGSCatalog and all scores are written to file `PGS000018.scores.txt`. An interactive report html report is created.

### Required parameters

- `--ref <file(s) or PGS-ID>` - score file with weights or a PGS ID. Multiple scores are separated by `,` (.e.g `score1.txt.gz,score2.txt.gz` or `PGS000018,PGS000027`)
- `--out <file>` - Output file name

### Optional parameters

- `--minR2 <value>` - Use only variants with an imputation quality (R2) >= `<value>`
- `--writeVariants <file>` - Writes csv file with all variants used in calculation
- `--includeVariants <file>` - Restrict calculation to use only variants from this csv file
- `--genotypes GT|DS` - Use genotypes or dosage
- `--report-html <file>` - Creates an interactive html report. The report includes summary statistics (like coverage) for each score and can be filtered by e.g. id or trait.
- `--samples` - Restrict calculation to use only samples from this csv file
- `--meta <file>` - Use this meta file to annotate scores

## Input files

### Genotypes

- VCF file format (`*.vcf` and `*.vcf.gz`)
- one VCF file per chromosome (e.g. output of Imputationserver)
- works out of the box with imputed genotypes from [Michigan Imputation Server](http://imputationserver.sph.umich.edu)

### Scores

`pgs-calc` supports [PGSCatalog](https://www.pgscatalog.org) out of the box: open the website, find your score of interest and download the provided `txt.gz` files.

As `pgs-calc` works with genomic positions and not with marker ids, the following requirements must be fulfilled:

1. The build of your genotypes and the score must be the same. If the score is on a different build, you can use the `pgs-calc resolve` command to lift over to the build of the genotypes.
2. The score file needs `chr_name` and `chr_position` columns. If there is only `rsID` present, you need to set the parameter `--dbsnp` and the correct index to convert rsIDs on the fly to the correct position. Depending on the build of your genotypes (hg19 or hg38) you can download the dbsnp index from [here](https://imputationserver.sph.umich.edu/resources/dbsnp-index/).
3. The column `other_allele` is mandatory to handle multi-allelic variants in an unified way.

If you want to create your own weight files, you need a tab-delimited text file with the following columns:

```
chr_name  chr_position  effect_allele other_allele  effect_weight
```

## Examples

### Single chromosome

Apply PGS to a single file (e.g. one chromosome):

```
pgs-calc apply --ref PGS000018.txt.gz test.chr1.vcf.gz --out scores.txt
```

All scores are written to file `scores.txt`

### Multiple chromosomes

Apply PGS to multiple files (e.g. multiple chromosomes):

```
pgs-calc apply --ref PGS000018.txt.gz test.chr1.vcf.gz test.chr2.vcf.gz test.chr3.vcf.gz test.chr4.vcf.gz --out scores.txt
```

Apply PGS to multiple files by using file patterns:

```
pgs-calc apply --ref PGS000018.txt.gz test.chr*.vcf.gz --out scores.txt
```

### Multiple scores

Apply multiple score files:

```
pgs-calc apply --ref PGS000018.txt.gz,PGS000027.txt.gz test.chr*.vcf.gz --out scores.txt
```

You can also create a file `scores_filenames.txt` that lists all paths to your score files:

```
scores
PGS000018.txt.gz
PGS000027.txt.gz
```

```
pgs-calc apply --ref scores_filenames.txt test.chr*.vcf.gz --out scores.txt
```

Attention: All paths inside the file are relative to the location of the file itself.

### Filter by Imputation Quality

Use only variants with an imputation quality (R2) >= 0.9:

```
pgs-calc apply --ref PGS000018.txt.gz test.chr*.vcf.gz --minR2 0.9 --out scores.txt
```

### PGSCatalog support

If a PGS id is provided, pgs-calc downloads the file from PGSCatalog automatically:

```
pgs-calc apply --ref PGS000018 test.chr1.vcf.gz --out scores.txt
```

All scores are written to file `scores.txt`.

You can also use the `download` command to download a specific PGS id:

```
pgs-calc download PGS000018 --out PGS000018.txt.gz
```

The weights are saved in file `PGS002297.txt.gz`.

### Scores with rsIDs

If the `--dbsnp` parameter is set, pgs-calc converts on the fly all rsID automatically to their positions. Depending on the build of your genotypes (hg19 or hg38) you can download the dbsnp index from [here](https://imputationserver.sph.umich.edu/resources/dbsnp-index/).

```
pgs-calc apply --ref PGS002297 test.chr1.vcf.gz --out scores.txt --dbsnp dbsnp154_hg19.txt.gz
```

All scores are written to file `scores.txt`

### Different Builds

The build of your genotypes and the score must be the same. If the score is on a different build, you can use the `pgs-calc resolve` command to lift over the score file to the build of the genotypes. You need a [dbsnp-index](https://imputationserver.sph.umich.edu/resources/dbsnp-index/) file and a [chain](https://imputationserver.sph.umich.edu/resources/chain/) file.

```
pgs-calc resolve --in PGS002297 --out PGS002297.hg38.txt.gz --dbsnp dbsnp154_hg38.txt.gz --chain hg19_to_hg38.over.chain.gz
```

The new positions are written to file `PGS002297.hg38.txt.gz` and this file can the be used by `pgs-calc apply`.

## Resources

- dbsnp-index files to resolve rsIDs: [https://imputationserver.sph.umich.edu/resources/chain/](https://imputationserver.sph.umich.edu/resources/dbsnp-index/)
- Chain files: [https://imputationserver.sph.umich.edu/resources/chain/](https://imputationserver.sph.umich.edu/resources/chain/)

## Contact

Lukas Forer, Institute of Genetic Epidemiology, Medical University of Innsbruck
