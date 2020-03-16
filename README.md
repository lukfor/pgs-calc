# riskscore

Applying polygenic risk scores (PRS) on imputed genotypes


## Installation

- Download `install.sh` from latest release
- Execute `bash install.sh`
- Installer creates a file `riskscore` in current directory
- Validate installation with `./riskscore --version` 

## Usage

### Input files

#### Genotypes

- VCF file format (`*.vcf` and `*.vcf.gz`)
- one VCF file per chromosome (e.g. output of Imputationserver)
- the genotype dosage is read from tag `DS`

Example file: https://github.com/lukfor/riskscore/blob/master/test-data/test.chr1.vcf

#### Risk score weights

- Tab delimited file
- Required Columns
  - chr
  - position_hg19
  - effect_weight
  - A1
  - A2
  - effect_allele

Example file: https://github.com/lukfor/riskscore/blob/master/test-data/test.scores.csv


### Single chromosome

Apply PRS to a single file (e.g. one chromosome):

```
./riskscore --ref Khera.et.al_GPS_BMI_Cell_2019.txt test.chr1.vcf.gz --out scores.txt
```

All risk scores are written to file `scores.txt`

### Multipl chromosomes

Apply PRS to multiple files (e.g. multiple chromsomes):

```
./riskscore --ref Khera.et.al_GPS_BMI_Cell_2019.txt test.chr1.vcf.gz test.chr2.vcf.gz test.chr3.vcf.gz test.chr4.vcf.gz --out scores.txt
```

Apply PRS to multiple files by using file patterns:

```
./riskscore --ref Khera.et.al_GPS_BMI_Cell_2019.txt test.chr*.vcf.gz --out scores.txt
```


### Filter

#### `--minR2`

Use only variants with an imputation quality (R2) >= 0.9:

```
./riskscore --ref Khera.et.al_GPS_BMI_Cell_2019.txt test.chr*.vcf.gz --minR2 0.9 --out scores.txt
```