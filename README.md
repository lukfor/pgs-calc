# PGS Calculator

[![Build Status](https://travis-ci.org/lukfor/pgs-calc.svg?branch=master)](https://travis-ci.org/lukfor/pgs-calc)
[![codecov](https://codecov.io/gh/lukfor/pgs-calc/branch/master/graph/badge.svg)](https://codecov.io/gh/lukfor/pgs-calc)
[![GitHub release](https://img.shields.io/github/release/lukfor/pgs-calc.svg)](https://GitHub.com/lukfor/pgs-calc/releases/)


> Applying polygenic risk scores (PGS) on imputed genotypes

## Features

- command line program (works on linux or MacOS)
- supports vcf.gz files (imputed or genotyped)
- supports different filters (e.g. r2 or variant list)
- supports PGS Catalog format (https://www.pgscatalog.org, currently over 190 scores)
- supports custom file formats with simple mapping mechanism


## Installation

- Download `installer.sh` from [latest release](https://github.com/lukfor/pgs-calc/releases/latest)
- Execute `bash installer.sh`
- Installer creates a file `pgs-calc` in current directory
- Validate installation with `./pgs-calc --version`

## Usage

Applying polygenic risk scores (PRS) on imputed genotypes

```
./pgs-calc --ref PGS000018.txt.gz --out PGS000018.scores.txt chr*.dose.noID.vcf.gz
```

### Optional parameters

- `--minR2 <value>` - Use only variants with an imputation quality (R2) >= `<value>`:
- `--writeVariants <file>` - Writes csv file with all variants used in calculation
- `--includeVariants <file>` - Restrict calculation to use only variants from this csv file
- `--genotypes GT|DS` - Use genotypes or dosage


### Input files

#### Genotypes

- VCF file format (`*.vcf` and `*.vcf.gz`)
- one VCF file per chromosome (e.g. output of Imputationserver)
- works out of the box with imputed genotypes from [Michigan Imputation Server](http://imputationserver.sph.umich.edu)

#### Scores

`./pgs-calc` supports [PGSCatalog](https://www.pgscatalog.org) out of the box: open the website, find your score of interest and download the provided `txt.gz` files.


### Examples

#### Single chromosome

Apply PRS to a single file (e.g. one chromosome):

```
./pgs-calc --ref PGS000018.txt.gz test.chr1.vcf.gz --out scores.txt
```

All risk scores are written to file `scores.txt`

#### Multiple chromosomes

Apply PRS to multiple files (e.g. multiple chromosomes):

```
./pgs-calc --ref PGS000018.txt.gz test.chr1.vcf.gz test.chr2.vcf.gz test.chr3.vcf.gz test.chr4.vcf.gz --out scores.txt
```

Apply PRS to multiple files by using file patterns:

```
./pgs-calc --ref PGS000018.txt.gz test.chr*.vcf.gz --out scores.txt
```

#### Multiple scores

Apply multiple score files:

```
./pgs-calc --ref PGS000018.txt.gz,PGS000027.txt.gz test.chr*.vcf.gz --out scores.txt
```


#### Filter by Imputation Qualitity

Use only variants with an imputation quality (R2) >= 0.9:

```
./pgs-calc --ref PGS000018.txt.gz test.chr*.vcf.gz --minR2 0.9 --out scores.txt
```

## Contact

Lukas Forer, Institute of Genetic Epidemiology, Medical University of Innsbruck
