# record-parsing

A small server application for parsing records in a variety of formats
with local, in-memory storage. Records may be requested in a number of
different sorts, described below.

## API

Supported endpoints include:
- `POST /records` - Adds the record to the in-memory data store,
  returning a 201 if successful.
- `GET /records/name` - Returns all stored records sorted by
  `last-name`, descending.
- `GET /records/birthdate` - Returns all stored records sorted by
  `birthdate`, ascending.
- `GET /records/name` - Returns all stored records sorted by favorite
  color, ascending, then by last name ascending.

## Running Locally

As an RESTful HTTP server, this can be run locally rather simply with
`lein uberjar`.

From there, one may add records using the `POST /records` endpoint:
```
curl -X POST localhost:3000/records \
    -H "Content-Type: text/plain" \
	-d "Bernard,Evan,ebwbernard@gmail.com,green,11/2/1992"
```

## Out of Scope

The following updates may be considered in the future, but are
non-goals at this time:
- Authentication/authorization
- Persistent data stores
- Containerization
