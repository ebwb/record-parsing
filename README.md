# record-parsing

A small utility app for parsing records in a variety of formats. All
records from the specified input file will be printed to stdout,
sorted according to the sort specified. If no sort is specified,
records will be sorted by last name, descending.

## Input Format

Files are expected to adhere to a specified format:
1. Last name
2. First name
3. Email
4. Favorite color
5. Date of Birth (expected in M/d/YYYY format)

Input files may contain comments. All comment lines must begin with a
`#` character. Blank lines are allowed, but skipped.

### Sample Input

The following is an example of an input file:
```
# This is a comment line. It will be skipped. The next line is
# blank, and will also be skipped. Normal input follows.

Tirekicker,Ruth,ruth.tirekicker@yopmail.com,black,2/7/1984
Homeowner,John,john.homeowner@yopmail.com,white,1/1/1980
```

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

Usage: `<java run command> <path to file> [--sort <name of sort>]`

Run the app, assuming `project.clj` version of 0.0.0:
```
java -jar target/record-parsing-0.0.0-standalone.jar resources/comma-delimited.txt
```

Run the app again with the same version, this time using a `--sort` of `last-name-desc`:
```
java -jar target/record-parsing-0.0.0-standalone.jar resources/comma-delimited.txt --sort last-name-desc
```
