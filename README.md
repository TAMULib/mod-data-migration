# mod-data-migration

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Additional information

## MARC Bibliographic migration

POST to http://localhost:9003/migrate/bibs

```
{
  "extraction": {
    "count": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "page": "SELECT bib_id, suppress_in_opac FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "additional": "SELECT bib_id, seqnum, record_segment FROM ${SCHEMA}.bib_data WHERE bib_id = ${BIB_ID}"
  },
  "parallelism": 12,
  "timeout": 120,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 48,
      "userId": "9b909401-96be-484e-8efe-158521718114",
      "profileInfo": {
        "id": "043ab092-d5f9-454a-87e1-8d879404367c",
        "name": "TAMU Bibligraphic Migration",
        "dataType": "MARC"
      },
      "useReferenceLinks": true,
      "references": {
        "sourceRecordTypeId": "96017110-47c5-4d55-8324-7dab1771749b",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "9b909401-96be-484e-8efe-158521718114",
      "profileInfo": {
        "id": "043ab092-d5f9-454a-87e1-8d879404367c",
        "name": "TAMU Bibligraphic Migration",
        "dataType": "MARC"
      },
      "useReferenceLinks": true,
      "references": {
        "sourceRecordTypeId": "b9f633b3-22e4-4bad-8785-da09d9eaa6c8",
        "instanceTypeId": "fb6db4f0-e5c3-483b-a1da-3edbb96dc8e8"
      }
    }
  ]
}
```

## Docker deployment

```
docker build -t folio/mod-data-migration .
docker run -d -p 9000:9000 folio/mod-data-migration
```

### Publish docker image

```
docker login [docker repo]
docker build -t [docker repo]/folio/mod-data-migration:[version] .
docker push [docker repo]/folio/mod-data-migration:[version]
```

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).
