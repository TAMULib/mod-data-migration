# mod-data-migration

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Git Submodules
Developers wanting to work on this project must initialize the submodules before the module can correctly build.

More information is available on the [developer site](https://dev.folio.org/guides/developer-setup/#update-git-submodules).

```
git submodule update --init --recursive
```

## Additional information

Other [modules](https://dev.folio.org/source-code/#server-side).

Other FOLIO Developer documentation is at [dev.folio.org](https://dev.folio.org/).

## Inventory Reference Link Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/inventory-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "pageSql": "SELECT bib_id FROM ${SCHEMA}.bib_master OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "holdingSql": "SELECT mfhd_id FROM ${SCHEMA}.bib_mfhd WHERE bib_id = ${BIB_ID}",
    "itemSql": "SELECT item_id FROM ${SCHEMA}.mfhd_item WHERE mfhd_id = ${MFHD_ID}",
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

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/bibs

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "pageSql": "SELECT bib_id, suppress_in_opac FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "marcSql": "SELECT bib_id, seqnum, record_segment FROM ${SCHEMA}.bib_data WHERE bib_id = ${BIB_ID}",
    "bibHistorySql": "SELECT operator_id FROM ${SCHEMA}.bib_history WHERE action_type_id = 1 AND bib_id = ${BIB_ID}",
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
      "instanceStatusId": "9634a5ab-9228-4703-baf2-4d12ebc77d56",
      "profile": {
        "name": "TAMU AMDB Bibligraphic Migration",
        "description": "Voyager migration profile",
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
      "instanceStatusId": "9634a5ab-9228-4703-baf2-4d12ebc77d56",
      "profile": {
        "name": "TAMU MSDB Bibligraphic Migration",
        "description": "Voyager migration profile",
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

## MARC Holding Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/holdings

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.mfhd_master",
    "pageSql": "SELECT mfhd_id, suppress_in_opac, location_id, display_call_no, call_no_type, record_type, field_008 FROM ${SCHEMA}.mfhd_master ORDER BY mfhd_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "marcSql": "SELECT mfhd_id, seqnum, record_segment FROM ${SCHEMA}.mfhd_data WHERE mfhd_id = ${MFHD_ID}",
    "locationSql": "SELECT DISTINCT location_id AS id, location_code AS code FROM ${SCHEMA}.location",
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
        "name": "TAMU Holding Migration",
        "dataType": "MARC"
      },
      "useReferenceLinks": true,
      "references": {
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "holdingToBibTypeId": "0ff1680d-caf5-4977-a78f-2a4fd64a2cdc"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "9b909401-96be-484e-8efe-158521718114",
      "profileInfo": {
        "id": "043ab092-d5f9-454a-87e1-8d879404367c",
        "name": "TAMU Holding Migration",
        "dataType": "MARC"
      },
      "useReferenceLinks": true,
      "references": {
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "holdingToBibTypeId": "f8252895-6bf5-4458-8a3f-57bd8c36c6ba"
      }
    }
  ],
  "maps": {
    "callNumberType": {
      " ": "24badefa-4456-40c5-845c-3f45ffbc4c03"
    },
    "holdingsType": {
      "u": "61155a36-148b-4664-bb7f-64ad708e0b32"
    },
    "receiptStatus": {
      "0": "Unknown"
    },
    "acqMethod": {
      "c": "Cooperative or consortial purchase"
    },
    "retentionPolicy": {
      " ": "Unknown"
    }
  },
  "defaults": {
    "acqMethod": "Cooperative or consortial purchase",
    "callNumberTypeId": "24badefa-4456-40c5-845c-3f45ffbc4c03",
    "discoverySuppress": true,
    "holdingsTypeId": "61155a36-148b-4664-bb7f-64ad708e0b32",
    "instanceId": "d1e8c984-f28c-48e0-b847-5ab35e957990",
    "permanentLocationId": "2b8f7d63-706a-4b56-8a5e-50ad24e33e4c",
    "receiptStatus": "Unknown",
    "retentionPolicy": "Unknown"
  }
}
```

## Migration Notes

The `parallelism` property designates the number of processes executed in parallel.
Ideally this can be set to the number of the available cores but should not be set higher than the `spring.datasource.hikari.maximumPoolSize` from the `applications.yml` configuration file.

Each *job* has a `partitions` property designating the number of partitions to divide a result set by when querying.
Increasing this number increases the number of jobs waiting in the queue, for all jobs, and therefore the total `partitions` for all jobs should be no higher than `1024` (the max queue size).
For a set of 200 rows of data with *job* `partitions` set to 10, there would be a total of 10 queries, each of which would retrieve 20 rows of data in their respective result sets.

## Docker deployment

When deploying docker, be sure to set the appropriate ports for your environment (make the guest port matches the port specified in `src/main/resources/application.yml` file).
In the example below, the `-p 9000:9000` represents `-p [host port]:[guest port]` with *guest* referring to the docker image and *host* referring to the system running docker.

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
