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

## Additional Information

Other [modules](https://dev.folio.org/source-code/#server-side).

Other FOLIO Developer documentation is at [dev.folio.org](https://dev.folio.org/).

## Migration Information

Be sure to use the correct user id and other various ids from reference data.

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
    "usernameSql": "SELECT uin, tamu_netid FROM PATRON.person_identifiers WHERE uin = ${UIN}",
    "addressesSql": "WITH patron_phone_with_desc AS (SELECT pp.*, pt.phone_desc FROM ${SCHEMA}.patron_phone pp LEFT JOIN phone_type pt ON pp.phone_type = pt.phone_type), patron_addresses_full AS (SELECT pa.patron_id AS id, pa.address_id, pa.address_type, at.address_desc, pa.address_line1, NVL2(pa.address_line2,NULL,NVL2(address_line3,TRIM(pa.address_line2),NVL2(address_line4,TRIM(pa.address_line2 || ' ' || pa.address_line3),NVL2(address_line5,TRIM(pa.address_line2 || ' ' || pa.address_line3 || ' ' || pa.address_line4),TRIM(pa.address_line2 || ' ' || pa.address_line3 || ' ' || pa.address_line4 || ' ' || pa.address_line5))))) AS address_line2,pa.city,pa.state_province,pa.zip_postal,pa.country,pa.address_status,pa.effect_date,pp.phone_number,pp.phone_desc FROM ${SCHEMA}.patron_address pa LEFT JOIN address_type at ON pa.address_type = at.address_type LEFT JOIN patron_phone_with_desc pp ON pa.address_id = pp.address_id WHERE pa.effect_date < sysdate AND pa.PATRON_ID = ${PATRON_ID} ORDER BY pa.patron_id, pa.address_id),patron_addresses_ranked AS (SELECT id,address_id,address_type,address_desc,address_line1,address_line2,city,state_province,zip_postal,country,address_status,effect_date,phone_number,phone_desc,RANK() OVER (PARTITION BY id, address_type ORDER BY effect_date DESC) dest_rank FROM ${SCHEMA}.patron_addresses_full WHERE address_type IN (2, 3) ORDER BY id, address_id),patron_addresses_ranked_email AS (SELECT id,address_id,address_line1 as email FROM ${SCHEMA}.patron_addresses_ranked WHERE address_type = 3 AND dest_rank = 1 ORDER BY id, address_id),patron_addresses_union AS (SELECT id,address_id,address_type,address_desc,address_line1,address_line2,city,state_province,zip_postal,country,address_status,effect_date,phone_number,phone_desc FROM ${SCHEMA}.patron_addresses_ranked WHERE dest_rank = 1 AND address_type = 2 UNION SELECT id,address_id,address_type,address_desc,address_line1,address_line2,city,state_province,zip_postal,country,address_status,effect_date,phone_number,phone_desc FROM ${SCHEMA}.patron_addresses_full WHERE address_type = 1),patron_addresses_aggregate AS (SELECT DISTINCT(id),LISTAGG(pau.address_id || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id) OVER (PARTITION BY pau.id) AS address_id,LISTAGG(pau.address_id || '::' || pau.address_type || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.address_type) OVER (PARTITION BY pau.id) AS address_type,LISTAGG(pau.address_id || '::' || pau.address_desc || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.address_desc) OVER (PARTITION BY pau.id) AS address_desc,LISTAGG(pau.address_id || '::' || pau.address_line1 || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.address_line1) OVER (PARTITION BY pau.id) AS address_line1,LISTAGG(pau.address_id || '::' || pau.address_line2 || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.address_line2) OVER (PARTITION BY pau.id) AS address_line2,LISTAGG(pau.address_id || '::' || pau.city || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.city) OVER (PARTITION BY pau.id) AS city,LISTAGG(pau.address_id || '::' || pau.state_province || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.state_province) OVER (PARTITION BY pau.id) AS state_province,LISTAGG(pau.address_id || '::' || pau.zip_postal || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.zip_postal) OVER (PARTITION BY pau.id) AS zip_postal,LISTAGG(pau.address_id || '::' || pau.country || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.country) OVER (PARTITION BY pau.id) AS country,LISTAGG(pau.address_id || '::' || pau.address_status || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.address_status) OVER (PARTITION BY pau.id) AS address_status,LISTAGG(pau.address_id || '::' || pau.effect_date || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.effect_date) OVER (PARTITION BY pau.id) AS effect_date, LISTAGG(pau.address_id || '::' || pau.phone_number || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.phone_number) OVER (PARTITION BY pau.id) AS phone_number, LISTAGG(pau.address_id || '::' || pau.phone_desc || ';;') WITHIN GROUP (ORDER BY pau.id, pau.address_id, pau.phone_desc) OVER (PARTITION BY pau.id) AS phone_desc FROM patron_addresses_union pau ORDER BY id) SELECT paa.id, paa.address_id, paa.address_type, paa.address_desc, paa.address_line1, paa.address_line2, paa.city, paa.state_province, paa.zip_postal, paa.country, paa.address_status, paa.effect_date, paa.phone_number, paa.phone_desc, pare.email FROM ${SCHEMA}.patron_addresses_aggregate paa LEFT JOIN patron_addresses_ranked_email pare ON paa.id = pare.id ORDER BY paa.address_id",
    "patronGroupSql": "select	barcode_status,to_char(barcode_status_date,'YYYYMMDD') AS barcode_status_date,${DECODE}patron_group_code,patron_barcodefrom (select	barcode_status,barcode_status_date,patron_barcode,patron_group_code,rank() over (partition by pg.patron_group_id order by barcode_status, barcode_status_date desc) barcode_rank from ${SCHEMA}.patron_barcode pb, ${SCHEMA}.patron_group pg where 	pb.patron_id = ${PATRON_ID} and   	pb.patron_group_id = pg.patron_group_id) where barcode_rank = 1 order by barcode_status asc, barcode_status_date desc, patron_group_level","amdbDecodeString": "DECODE (patron_group_code,'fast', 1,'grad', 2,'ungr', 3,'illend', 4,'libd', 5,'comm', 6,'cour', 7,'texs', 8,'nonr', 9) patron_group_level,",
    "msdbDecodeString": "DECODE (patron_group_code,'fac/staff', 1,'grad/prof', 2,'undergrad', 3) patron_group_level,",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    },
    "usernameDatabase": {
      "url": "",
      "username": "",
      "password": "",
      driverClassName": ""
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
    "pageSql": "WITH bibs AS (SELECT bib_id, suppress_in_opac FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY), operators AS (SELECT bib_id, operator_id FROM ${SCHEMA}.bib_history WHERE action_type_id = 1 AND bib_id IN (SELECT bib_id FROM bibs)) SELECT b.bib_id, b.suppress_in_opac, o.operator_id FROM bibs b LEFT JOIN operators o ON b.bib_id = o.bib_id",
    "marcSql": "SELECT bib_id, seqnum, record_segment FROM ${SCHEMA}.bib_data WHERE bib_id = ${BIB_ID}",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "preActions": [],
  "postActions": [],
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
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "preActions": [],
  "postActions": [],
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
    "location": {
      "AMDB-36": "media",
      "AMDB-37": "media,res",
      "AMDB-47": "ils,borr",
      "AMDB-48": "ils,lend",
      "AMDB-132": "blcc,circ",
      "AMDB-134": "blcc,stk",
      "AMDB-135": "blcc,ref",
      "AMDB-136": "blcc,res",
      "AMDB-137": "blcc,rndx",
      "AMDB-138": "www_evans",
      "AMDB-182": "media,arcv",
      "AMDB-201": "blcc,stand",
      "AMDB-225": "blcc,nbs",
      "AMDB-228": "blcc,audio",
      "AMDB-241": "blcc,udoc",
      "AMDB-244": "blcc,schk",
      "AMDB-264": "evans_pda",
      "AMDB-278": "learn_outreach",
      "AMDB-285": "blcc,ebc",
      "AMDB-288": "evans_withdrawn",
      "MSDB-5": "AbstractIndex",
      "MSDB-40": "www_msl",
      "MSDB-44": "msl_withdrawn",
      "MSDB-68": "Mobile",
      "MSDB-126": "rs,hdr",
      "MSDB-127": "rs,hdr",
      "MSDB-186": "msl_pda"
    },
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

## Item Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/items

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.item",
    "pageSql": "SELECT item_id, item_type_id, perm_location, pieces, temp_location, temp_item_type_id FROM ${SCHEMA}.item ORDER BY item_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "mfhdSql": "SELECT chron, item_enum FROM ${SCHEMA}.mfhd_item WHERE item_id = ${ITEM_ID}",
    "barcodeSql": "SELECT item_barcode FROM ${SCHEMA}.item_barcode WHERE item_id = ${ITEM_ID}",
    "itemTypeSql": "SELECT item_type_id, item_type_code FROM ${SCHEMA}.item_type",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": ""
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 48,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "materialTypeId": "d9acad2f-2aac-4b48-9097-e6ab85906b25",
      "references": {
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2",
        "itemToHoldingTypeId": "39670cf7-de23-4473-b5e3-abf6d79735e1"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "materialTypeId": "d9acad2f-2aac-4b48-9097-e6ab85906b25",
      "references": {
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0",
        "itemToHoldingTypeId": "492fea54-399a-4822-8d4b-242096c2ab12"
      }
    }
  ],
  "maps": {
    "location": {
      "AMDB-36": "media",
      "AMDB-37": "media,res",
      "AMDB-47": "ils,borr",
      "AMDB-48": "ils,lend",
      "AMDB-132": "blcc,circ",
      "AMDB-134": "blcc,stk",
      "AMDB-135": "blcc,ref",
      "AMDB-136": "blcc,res",
      "AMDB-137": "blcc,rndx",
      "AMDB-138": "www_evans",
      "AMDB-182": "media,arcv",
      "AMDB-201": "blcc,stand",
      "AMDB-225": "blcc,nbs",
      "AMDB-228": "blcc,audio",
      "AMDB-241": "blcc,udoc",
      "AMDB-244": "blcc,schk",
      "AMDB-264": "evans_pda",
      "AMDB-278": "learn_outreach",
      "AMDB-285": "blcc,ebc",
      "AMDB-288": "evans_withdrawn",
      "MSDB-5": "AbstractIndex",
      "MSDB-40": "www_msl",
      "MSDB-44": "msl_withdrawn",
      "MSDB-68": "Mobile",
      "MSDB-126": "rs,hdr",
      "MSDB-127": "rs,hdr",
      "MSDB-186": "msl_pda"
    },
    "loanType": {
      "non": "noncirc",
      "ser": "serial",
      "14d": "14_day",
      "4h": "4_hour",
      "3d": "3_day",
      "2h": "2_hour",
      "24h": "1_day",
      "1h": "1_hour",
      "curr": "normal",
      "120d": "120_day",
      "7d": "7_day",
      "1d": "1_day",
      "blu": "bluray",
      "cass": "cassette",
      "calc": "calculator",
      "head": "headphones",
      "acc": "accessory",
      "proj": "projector",
      "mic": "microphone",
      "cam": "camera",
      "voice": "voice_recorder",
      "norm": "normal",
      "book": "normal",
      "reference": "4_hour",
      "archives": "noncirc",
      "reserve": "4_hour",
      "CoP2wk": "14_day",
      "CoP24hr": "24_hour",
      "CoP2hr": "2_hour",
      "CoPnocirc": "noncirc",
      "eReader": "reader",
      "HSCbook": "normal",
      "HSCjournal": "journal",
      "HSCmedia": "media",
      "HSCreserve": "reserve",
      "rare": "noncirc",
      "HSCnocirc": "noncirc",
      "preserv": "preservation",
      "HSCtablet": "tablet",
      "7day": "7_day",
      "4hour": "4_hour"
    }
  },
  "defaults": {
    "permanentLoanTypeId": "dcdb0cef-c30f-4a3b-b0b6-757d1400535d",
    "permanentLocationId": "2b8f7d63-706a-4b56-8a5e-50ad24e33e4c"
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