# record-parsing

A small utility app for parsing records in a variety of formats

TODO(ebwb): specify format of input files
- Birth Date is expected to be in the format `M/d/YYYY`

## Runtime Options

| Option name | Details                                                                                                 |
|:------------|---------------------------------------------------------------------------------------------------------|
| `sort`      | Various sort options supported. Includes: `last-name-desc`, `birth-date-asc`, `color-asc-last-name-asc` |


## Running Locally

Intended as a command line tool, this tool can be run at your
terminal.

Installation:
```
lein uberjar
```

Running, assuming `project.clj` version of 0.0.0:
```
java -jar target/record-parsing-0.0.0-standalone.jar resources/comma-delimited.txt
```

Running again with the same version, this time using a `--sort` of `last-name-desc`:
```
java -jar target/record-parsing-0.0.0-standalone.jar resources/comma-delimited.txt --sort last-name-desc
```
