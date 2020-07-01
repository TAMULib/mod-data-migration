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

## Vendor Reference Link Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/vendor-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.vendor",
    "pageSql": "SELECT vendor_id FROM ${SCHEMA}.vendor ORDER BY vendor_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "parallelism": 2,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 1,
      "references": {
        "vendorTypeId": "08c7dd18-dbaf-11e9-8a34-2a2ae2dbcce4"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "references": {
        "vendorTypeId": "b427aa0a-96f2-4338-8b3c-2ddcdca6cfe4"
      }
    }
  ]
}
```

## User Reference Link Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/user-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.patron WHERE last_name IS NOT NULL",
    "pageSql": "SELECT patron_id, NVL2(institution_id, regexp_replace(institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || patron_id) AS external_system_id FROM ${SCHEMA}.patron ap WHERE last_name IS NOT NULL ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "parallelism": 2,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 1,
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userExternalTypeId": "0ed6f994-8dbd-4827-94c0-905504169c90"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userExternalTypeId": "426ce32f-388c-4edf-9c79-d6b8348148a0"
      }
    }
  ]
}
```

## User Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/users

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.patron WHERE last_name IS NOT NULL",
    "pageSql": "SELECT patron_id, nvl2(institution_id, regexp_replace(institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || patron_id) AS external_system_id, last_name, first_name, middle_name, nvl2(expire_date, to_char(expire_date,'YYYYMMDD'), to_char(purge_date,'YYYYMMDD')) AS active_date, nvl2(expire_date, to_char(expire_date,'YYYY-MM-DD'), to_char(purge_date,'YYYY-MM-DD')) AS expire_date, sms_number, current_charges FROM ${SCHEMA}.patron WHERE last_name IS NOT NULL ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
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
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userExternalTypeId": "0ed6f994-8dbd-4827-94c0-905504169c90"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userExternalTypeId": "426ce32f-388c-4edf-9c79-d6b8348148a0"
      }
    }
  ]
}
```

## Inventory Reference Link Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/inventory-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "pageSql": "SELECT bib_id FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
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
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "instanceStatusId": "9634a5ab-9228-4703-baf2-4d12ebc77d56",
      "profile": {
        "name": "TAMU AMDB Bibligraphic Migration",
        "description": "Voyager migration profile",
        "dataType": "MARC"
      },
      "references": {
        "sourceRecordTypeId": "96017110-47c5-4d55-8324-7dab1771749b",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "instanceStatusId": "9634a5ab-9228-4703-baf2-4d12ebc77d56",
      "profile": {
        "name": "TAMU MSDB Bibligraphic Migration",
        "description": "Voyager migration profile",
        "dataType": "MARC"
      },
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
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "holdingToBibTypeId": "0ff1680d-caf5-4977-a78f-2a4fd64a2cdc"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "holdingToBibTypeId": "f8252895-6bf5-4458-8a3f-57bd8c36c6ba"
      }
    }
  ],
  "maps": {
    "callNumberType": {
      " ": "24badefa-4456-40c5-845c-3f45ffbc4c03",
      "0": "95467209-6d7b-468b-94df-0f5d7ad2747d",
      "1": "03dd64d0-5626-4ecd-8ece-4531e0069f35",
      "2": "054d460d-d6b9-4469-9e37-7a78a2266655",
      "3": "fc388041-6cd0-4806-8a74-ebe3b9ab4c6e",
      "4": "28927d76-e097-4f63-8510-e56f2b7a3ad0",
      "5": "5ba6b62e-6858-490a-8102-5b1369873835",
      "6": "cd70562c-dd0b-42f6-aa80-ce803d24d4a1",
      "8": "6caca63e-5651-4db6-9247-3205156e9699"
    },
    "holdingsType": {
      "u": "61155a36-148b-4664-bb7f-64ad708e0b32",
      "v": "dc35d0ae-e877-488b-8e97-6e41444e6d0a",
      "x": "03c9c400-b9e3-4a07-ac0e-05ab470233ed",
      "y": "e6da6c98-6dd0-41bc-8b4b-cfd4bbd9c3ae"
    },
    "receiptStatus": {
      "0": "Unknown",
      "1": "Other receipt or acquisition status",
      "2": "Received and complete or ceased",
      "3": "On order",
      "4": "Currently received",
      "5": "Not currently received",
      " ": "Unknown",
      "|": "Unknown"
    },
    "acqMethod": {
      "c": "Cooperative or consortial purchase",
      "d": "Deposit",
      "e": "Exchange",
      "f": "Free",
      "g": "Gift",
      "l": "Legal deposit",
      "m": "Membership",
      "n": "Non-library purchase",
      "p": "Purchase",
      "q": "Lease",
      "u": "Unknown",
      "z": "Other method of acquisition",
      "|": "Unknown"
    },
    "retentionPolicy": {
      " ": "Unknown",
      "|": "Unknown",
      "0": "Unknown",
      "1": "Other general retention policy",
      "2": "Retained except as replaced by updates",
      "3": "Sample issue retained",
      "4": "Retained until replaced by microform",
      "5": "Retained until replaced by cumulation, replacement volume, or revision",
      "6": "Retained for a limited period",
      "7": "Not retained",
      "8": "Permanently retained"
    }
  },
  "defaults": {
    "acqMethod": "Cooperative or consortial purchase",
    "callNumberTypeId": "24badefa-4456-40c5-845c-3f45ffbc4c03",
    "discoverySuppress": false,
    "holdingsTypeId": "61155a36-148b-4664-bb7f-64ad708e0b32",
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
