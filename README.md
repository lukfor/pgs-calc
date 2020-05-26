# riskscore

Applying polygenic risk scores (PRS) on imputed genotypes

## Features

- command line programm (works on linux or MacOS)
- supports vcf.gz files (imputed or genotyped)
- supports different filters (e.g. r2 or variant list)
- supports PGS Catalog format (https://www.pgscatalog.org, currently over 190 scores)
- supports custom file formats with simple mapping mechanism
- fast, scalable and well documented


## Installation

- Download `install.sh` from latest release
- Execute `bash install.sh`
- Installer creates a file `riskscore` in current directory
- Validate installation with `./riskscore --version`

## Usage

Applying polygenic risk scores (PRS) on imputed genotypes

```
riskscore --ref PGS000018.txt --out PGS000018.scores.txt chr*.dose.noID.vcf.gz
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

#### Risk score weights

`riskscore` supports the file-format of [PGSCatalog](https://www.pgscatalog.org) out of the box: open the website, find your score of interest and download the provided `txt.gz` files.


### Examples

#### Single chromosome

Apply PRS to a single file (e.g. one chromosome):

```
./riskscore --ref PGS000018.txt test.chr1.vcf.gz --out scores.txt
```

All risk scores are written to file `scores.txt`

#### Multiple chromosomes

Apply PRS to multiple files (e.g. multiple chromosomes):

```
./riskscore --ref PGS000018.txt test.chr1.vcf.gz test.chr2.vcf.gz test.chr3.vcf.gz test.chr4.vcf.gz --out scores.txt
```

Apply PRS to multiple files by using file patterns:

```
./riskscore --ref PGS000018.txt test.chr*.vcf.gz --out scores.txt
```


#### Filter by Imputation Qualitity

Use only variants with an imputation quality (R2) >= 0.9:

```
./riskscore --ref PGS000018.txt test.chr*.vcf.gz --minR2 0.9 --out scores.txt
```

## Contact

Lukas Forer, Institute of Genetic Epidemiology, Medical University of Innsbruck
