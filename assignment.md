* Why the homework?
Welcome! For engineering candidates, we want to see a sample of their
code. It also helps to have a more productive conversation during the
interview. Thank you for putting your time and energy into this!

* The homework
This assignment is designed to be completed on your own time. If you
have any clarification questions, please ask. Submit the project once
fully completed.

The work will determine whether we bring you in for an
interview. Treat this assignment as you would any work assignment that
would result in code being put into production. We encourage you to
put your best foot forward!

* Rules and Guidelines
- We will be looking at the readability and cleanliness of your code.
- Provide unit tests. The cleanliness and readability of tests is just
  as important as your production code.
- Make a github repository making commits how you would normally.

* Step 1 - Build a system to parse and sort a set of records
Create a command line app that takes a single input, the name of a
file containing a set of records in one of the three formats: comma
delimited, pipe delimited, or space delimited (sample files
provided). Your app should output (to the screen) the set of records
sorted in one of three ways.

** Input
A record consists of the following 5 fields: last name, first name,
email, date of birth and favorite color. Sample inputs are also
provided.

You may assume that the delimiters (commas, pipes and spaces) do not
appear anywhere in the data values themselves. Write a program in
Clojure to read in records from these files and combine them into a
single set of records.

** Output
Create and display 3 different views of the data you read in:
- Output 1 - sorted by favorite color then by last name ascending.
- Output 2 - sorted by birth date, ascending.
- Output 3 - sorted by last name, descending.

Display dates in the format M/D/YYYY.

* Step 2 - Build a REST API to access your system
Tests for this section are required as well.

*Within the same code base*, build a standalone REST API with the
 following endpoints:
- POST /records - Post a single data line in any of the 3 formats supported by your existing code
- GET /records/color - returns records sorted by favorite color
- GET /records/birthdate - returns records sorted by birthdate
- GET /records/name - returns records sorted by last name

It's your choice how you render the output from these endpoints as
long as it is well-structured JSON data.

Don't worry about using a persistent datastore.

** Step 3 - Review and Refactor
In the next interview we will sit down with you and walk through your
solution together. Be ready to explain any tradeoffs you made in your
solution. We will also use your solution as an opportunity to pair
program by asking you to modify or extend it in some way.

Please have your development environment ready for the interview.

