# mod-data-migration

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Additional information

## Inventory Reference Link Migration

POST to http://localhost:9003/migrate/inventory-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "pageSql": "WITH bib_holdings_items AS (SELECT b.bib_id, bm.mfhd_id, mi.item_id FROM ${SCHEMA}.bib_master b LEFT JOIN ${SCHEMA}.bib_mfhd bm ON b.bib_id = bm.bib_id LEFT JOIN ${SCHEMA}.mfhd_item mi ON bm.mfhd_id = mi.mfhd_id) SELECT bhi.bib_id, CAST(COLLECT(bhi.mfhd_id || '::' || bhi.item_id) AS sys.odcivarchar2list) AS holding_items FROM bib_holdings_items bhi GROUP BY bhi.bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 11,
      "references": {
        "sourceRecordTypeId": "96017110-47c5-4d55-8324-7dab1771749b",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672",
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2",
        "holdingToBibTypeId": "0ff1680d-caf5-4977-a78f-2a4fd64a2cdc",
        "itemToHoldingTypeId": "39670cf7-de23-4473-b5e3-abf6d79735e1"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "references": {
        "sourceRecordTypeId": "b9f633b3-22e4-4bad-8785-da09d9eaa6c8",
        "instanceTypeId": "fb6db4f0-e5c3-483b-a1da-3edbb96dc8e8",
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0",
        "holdingToBibTypeId": "f8252895-6bf5-4458-8a3f-57bd8c36c6ba",
        "itemToHoldingTypeId": "492fea54-399a-4822-8d4b-242096c2ab12"
      }
    }
  ]
}
```

## MARC Bibliographic Migration

POST to http://localhost:9003/migrate/bibs

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "pageSql": "SELECT bib_id, suppress_in_opac FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "marcSql": "SELECT bib_id, seqnum, record_segment FROM ${SCHEMA}.bib_data WHERE bib_id = ${BIB_ID}",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "parallelism": 12,
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
