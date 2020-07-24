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

### Reference Link Types

Reference link types are loaded before a reference link migraiton. The reference link types are loaded from `src/main/resources/referenceLinkTypes/{migration}/*.json`. The reference link type ids should be used within the context requests below.

### Reference Data

Reference data is loaded before a migration begins. The reference data is loaded from `src/main/resources/referenceData/{migration}/*.json`. The filenames are used in a convention to resolve dependent reference data. If reference data exists it will fail silently unless debug logging. The reference data ids or values should be used within the context requests below.

> It is possible to have infinite recursion if unable to resolve dependent reference data. This will happen with circular dependencies. i.e. (x depends on y and y depdends on x), (x depends on y, y depends on z, z depends on x), ...

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
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "references": {
        "vendorTypeId": "08c7dd18-dbaf-11e9-8a34-2a2ae2dbcce4"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 2,
      "references": {
        "vendorTypeId": "b427aa0a-96f2-4338-8b3c-2ddcdca6cfe4"
      }
    }
  ]
}
```

## MARC Vendor Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/vendors

> If vendors have been completely cleared out for given tenant, the ignore codes can be empty. The organization codes below are default in folio/release and will need to be ignored for all batches to succeed.

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ( (SELECT DISTINCT v.vendor_id AS vendor_id, v.vendor_code AS vendor_code, v.vendor_name AS vendor_name, v.federal_tax_id AS federal_tax_id, v.institution_id AS institution_id, v.default_currency AS default_currency, v.claim_interval AS claim_interval, v.claim_count AS claim_count, v.cancel_interval AS cancel_interval, v.ship_via AS ship_via, v.create_date AS create_date, v.create_opid AS create_opid, v.update_date AS update_date, v.update_opid AS update_opid, v.vendor_type AS vendor_type FROM ${SCHEMA}.vendor v, ${SCHEMA}.invoice inv WHERE inv.vendor_id = v.vendor_id AND inv.invoice_date > SYSDATE - 1825) UNION (SELECT DISTINCT v.vendor_id AS vendor_id, v.vendor_code AS vendor_code, v.vendor_name AS vendor_name, v.federal_tax_id AS federal_tax_id, v.institution_id AS institution_id, v.default_currency AS default_currency, v.claim_interval AS claim_interval, v.claim_count AS claim_count, v.cancel_interval AS cancel_interval, v.ship_via AS ship_via, v.create_date AS create_date, v.create_opid AS create_opid, v.update_date AS update_date, v.update_opid AS update_opid, v.vendor_type AS vendor_type FROM ${SCHEMA}.purchase_order po, ${SCHEMA}.location shipto, ${SCHEMA}.po_status stat, ${SCHEMA}.po_type, ${SCHEMA}.vendor v WHERE po.po_type = po_type.po_type AND po.ship_location = shipto.location_id AND shipto.location_code IN ( ${LOCATIONS} ) AND po.po_status = stat.po_status AND po_status_desc IN ( ${STATUSES} ) AND po_type_desc IN ( ${TYPES} ) AND po.vendor_id = v.vendor_id) ORDER BY vendor_id )",
    "pageSql": "(SELECT DISTINCT v.vendor_id AS vendor_id, v.vendor_code AS vendor_code, v.vendor_name AS vendor_name, v.federal_tax_id AS federal_tax_id, v.institution_id AS institution_id, v.default_currency AS default_currency, v.claim_interval AS claim_interval, v.claim_count AS claim_count, v.cancel_interval AS cancel_interval, v.ship_via AS ship_via, v.create_date AS create_date, v.create_opid AS create_opid, v.update_date AS update_date, v.update_opid AS update_opid, v.vendor_type AS vendor_type FROM ${SCHEMA}.vendor v, ${SCHEMA}.invoice inv WHERE inv.vendor_id = v.vendor_id AND inv.invoice_date > SYSDATE - 1825) UNION (SELECT DISTINCT v.vendor_id AS vendor_id, v.vendor_code AS vendor_code, v.vendor_name AS vendor_name, v.federal_tax_id AS federal_tax_id, v.institution_id AS institution_id, v.default_currency AS default_currency, v.claim_interval AS claim_interval, v.claim_count AS claim_count, v.cancel_interval AS cancel_interval, v.ship_via AS ship_via, v.create_date AS create_date, v.create_opid AS create_opid, v.update_date AS update_date, v.update_opid AS update_opid, v.vendor_type AS vendor_type FROM ${SCHEMA}.purchase_order po, ${SCHEMA}.location shipto, ${SCHEMA}.po_status stat, ${SCHEMA}.po_type, ${SCHEMA}.vendor v WHERE po.po_type = po_type.po_type AND po.ship_location = shipto.location_id AND shipto.location_code IN ( ${LOCATIONS} ) AND po.po_status = stat.po_status AND po_status_desc IN ( ${STATUSES} ) AND po_type_desc IN ( ${TYPES} ) AND po.vendor_id = v.vendor_id) ORDER BY vendor_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "accountSql": "SELECT account_number, account_name, deposit, account_status, note FROM ${SCHEMA}.vendor_account va, account_note an WHERE va.vendor_id = ${VENDOR_ID} AND va.account_id = an.account_id(+) ORDER BY va.account_id",
    "addressSql": "SELECT address_id, order_address, payment_address, return_address, claim_address, email_address, other_address, contact_name, contact_title, nvl2(contact_title, contact_title ||' '|| address_line1, address_line1) AS address_line1_full, address_line1, TRIM(address_line2) ||' '|| TRIM(address_line3) ||' '|| TRIM(address_line4) ||' '|| TRIM(address_line5) AS address_line2, city, state_province, zip_postal, country, std_address_number FROM ${SCHEMA}.vendor_address WHERE vendor_id = ${VENDOR_ID} ORDER BY address_id",
    "aliasSql": "SELECT alt_vendor_name FROM ${SCHEMA}.alt_vendor_names WHERE vendor_id = ${VENDOR_ID} ORDER BY vendor_id",
    "noteSql": "SELECT note FROM ${SCHEMA}.vendor_note vn WHERE vn.vendor_id = ${VENDOR_ID}",
    "phoneSql": "SELECT phone_number, phone_type FROM ${SCHEMA}.vendor_phone WHERE address_id = ${ADDRESS_ID}",
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
      "partitions": 12,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "vendorTypeId": "08c7dd18-dbaf-11e9-8a34-2a2ae2dbcce4"
      },
      "locations": "'SR', 'SRDB', 'SRDBProcar', 'SRDIR', 'SRDIRM', 'SRDIRMP', 'SRDIRN', 'SRDIRO', 'SRDIRP', 'SRGFT', 'SRMSV', 'SRMSVM', 'SRMSVMO', 'SRMSVO', 'SRMSVP', 'SRMSVPM', 'SRMSVW', 'SRMSV WM', 'SRProcard', 'SRSOV', 'SRSOVM', 'SRVSVO'",
      "statuses": "'Approved/Sent', 'Pending'",
      "types": "'Approval', 'Firm Order', 'Gift', 'Exchange', 'Depository', 'Continuation'"
    },
    {
      "schema": "MSDB",
      "partitions": 2,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "references": {
        "vendorTypeId": "b427aa0a-96f2-4338-8b3c-2ddcdca6cfe4"
      },
      "locations": "'AcqCleanUp'",
      "statuses": "'Approved/Sent', 'Pending', 'Received Complete'",
      "types": "'Continuation'"
    }
  ],
  "maps": {
    "categories": {
      "claim": "d931bdc4-ef47-4871-98d7-2c48f5ff4fe0",
      "order": "9718aa38-8fb4-49e4-910b-bbdc2b1aa579",
      "other": "04f39c67-b212-4fe7-87f0-0875c8995d21",
      "payment": "ac6528cc-8ba0-4678-9b08-627ca2314ffd",
      "return": "544459af-fc5e-4e64-9b40-acb84ac4d3aa"
    },
    "countryCodes": {
      "AFGHANISTAN": "AFG",
      "ALA ALAND ISLANDS": "ALA",
      "ALBANIA": "ALB",
      "ALGERIA": "DZA",
      "AMERICAN SAMOA": "ASM",
      "ANDORRA": "AND",
      "ANGOLA": "AGO",
      "ANGUILLA": "AIA",
      "ANTARCTICA": "ATA",
      "ANTIGUA AND BARBUDA": "ATG",
      "ARGENTINA": "ARG",
      "ARMENIA": "ARM",
      "ARUBA": "ABW",
      "AUSTRALIA": "AUS",
      "AUSTRIA": "AUT",
      "AZERBAIJAN": "AZE",
      "BAHAMAS": "BHS",
      "BAHRAIN": "BHR",
      "BANGLADESH": "BGD",
      "BARBADOS": "BRB",
      "BELARUS": "BLR",
      "BELGIUM": "BEL",
      "BELIZE": "BLZ",
      "BENIN": "BEN",
      "BERMUDA": "BMU",
      "BHUTAN": "BTN",
      "BOLIVIA": "BOL",
      "BOSNIA AND HERZEGOVINA": "BIH",
      "BOSNIA": "BIH",
      "BOTSWANA": "BWA",
      "BOUVET ISLAND": "BVT",
      "BRAZIL": "BRA",
      "BRITISH VIRGIN ISLANDS": "VGB",
      "BRITISH INDIAN OCEAN TERRITORY": "IOT",
      "BRUNEI DARUSSALAM": "BRN",
      "BULGARIA": "BGR",
      "BURKINA FASO": "BFA",
      "BURUNDI": "BDI",
      "CAMBODIA": "KHM",
      "CAMEROON": "CMR",
      "CANADA": "CAN",
      "CAPE VERDE": "CPV",
      "CAYMAN ISLANDS": "CYM",
      "CENTRAL AFRICAN REPUBLIC": "CAF",
      "CHAD": "TCD",
      "CHILE": "CHL",
      "CHINA": "CHN",
      "HONG KONG" : "HKG",
      "MACAO": "MAC",
      "CHRISTMAS ISLAND": "CXR",
      "COCOS (KEELING) ISLANDS": "CCK",
      "COLOMBIA": "COL",
      "COMOROS": "COM",
      "CONGO (BRAZZAVILLE)": "COG",
      "CONGO": "COD",
      "COOK ISLANDS": "COK",
      "COSTA RICA": "CRI",
      "CROATIA": "HRV",
      "CUBA": "CUB",
      "CYPRUS": "CYP",
      "CZECH REPUBLIC": "CZE",
      "DENMARK": "DNK",
      "DJIBOUTI": "DJI",
      "DOMINICA": "DMA",
      "DOMINICAN REPUBLIC": "DOM",
      "ECUADOR": "ECU",
      "EGYPT": "EGY",
      "EL SALVADOR": "SLV",
      "ENGLAND": "GBR",
      "EQUATORIAL GUINEA": "GNQ",
      "ERITREA": "ERI",
      "ESPANA": "ESP",
      "ESTONIA": "EST",
      "ETHIOPIA": "ETH",
      "FALKLAND ISLANDS (MALVINAS)": "FLK",
      "FAROE ISLANDS": "FRO",
      "FIJI": "FJI",
      "FINLAND": "FIN",
      "FRANCE": "FRA",
      "FRA": "FRA",
      "FRENCH GUIANA": "GUF",
      "FRENCH POLYNESIA": "PYF",
      "FRENCH SOUTHERN TERRITORIES": "ATF",
      "GABON": "GAB",
      "GAMBIA": "GMB",
      "GBR": "GBR",
      "GEORGIA": "GEO",
      "GERMANY": "DEU",
      "GHANA": "GHA",
      "GIBRALTAR": "GIB",
      "GREAT BRITAIN": "GBR",
      "GREECE": "GRC",
      "GREENLAND": "GRL",
      "GRENADA": "GRD",
      "GUADELOUPE": "GLP",
      "GUAM": "GUM",
      "GUATEMALA": "GTM",
      "GUERNSEY": "GGY",
      "GUINEA": "GIN",
      "GUINEA-BISSAU": "GNB",
      "GUYANA": "GUY",
      "HAITI": "HTI",
      "HEARD AND MCDONALD ISLANDS": "HMD",
      "HOLY SEE (VATICAN CITY STATE)": "VAT",
      "HONDURAS": "HND",
      "HUNGARY": "HUN",
      "ICELAND": "ISL",
      "INDIA": "IND",
      "INDONESIA": "IDN",
      "IRAN": "IRN",
      "IRAQ": "IRQ",
      "IRELAND": "IRL",
      "ISLE OF MAN": "IMN",
      "ISRAEL": "ISR",
      "ITALY": "ITA",
      "JAMAICA": "JAM",
      "JAPAN": "JPN",
      "JERSEY": "JEY",
      "JORDAN": "JOR",
      "KAZAKHSTAN": "KAZ",
      "KENYA": "KEN",
      "KIRIBATI": "KIR",
      "KOREA (NORTH)": "PRK",
      "KOREA": "KOR",
      "S. KOREA": "KOR",
      "KUWAIT": "KWT",
      "KYRGYZSTAN": "KGZ",
      "LAO PDR": "LAO",
      "LATVIA": "LVA",
      "LEBANON": "LBN",
      "LESOTHO": "LSO",
      "LIBERIA": "LBR",
      "LIBYA": "LBY",
      "LIECHTENSTEIN": "LIE",
      "LITHUANIA": "LTU",
      "LUXEMBOURG": "LUX",
      "MACEDONIA": "MKD",
      "MADAGASCAR": "MDG",
      "MALAWI": "MWI",
      "MALAYSIA": "MYS",
      "MALDIVES": "MDV",
      "MALI": "MLI",
      "MALTA": "MLT",
      "MARSHALL ISLANDS": "MHL",
      "MARTINIQUE": "MTQ",
      "MAURITANIA": "MRT",
      "MAURITIUS": "MUS",
      "MAYOTTE": "MYT",
      "MEXICO": "MEX",
      "MICRONESIA": "FSM",
      "MOLDOVA": "MDA",
      "MONACO": "MCO",
      "MONGOLIA": "MNG",
      "MONTENEGRO": "MNE",
      "MONTSERRAT": "MSR",
      "MOROCCO": "MAR",
      "MOZAMBIQUE": "MOZ",
      "MYANMAR": "MMR",
      "NAMIBIA": "NAM",
      "NAURU": "NRU",
      "NEPAL": "NPL",
      "NETHERLANDS": "NLD",
      "NETHERLANDS ANTILLES": "ANT",
      "NEW CALEDONIA": "NCL",
      "NEW ZEALAND": "NZL",
      "NICARAGUA": "NIC",
      "NIGER": "NER",
      "NIGERIA": "NGA",
      "NIUE": "NIU",
      "NORFOLK ISLAND": "NFK",
      "NORTHERN IRELAND": "GBR",
      "NORTHERN MARIANA ISLANDS": "MNP",
      "NORWAY": "NOR",
      "OMAN": "OMN",
      "PAKISTAN": "PAK",
      "PALAU": "PLW",
      "PALESTINIAN TERRITORY": "PSE",
      "PANAMA": "PAN",
      "PAPUA NEW GUINEA": "PNG",
      "PARAGUAY": "PRY",
      "PERU": "PER",
      "PHILIPPINES": "PHL",
      "PITCAIRN": "PCN",
      "POLAND": "POL",
      "PORTUGAL": "PRT",
      "PUERTO RICO": "PRI",
      "QATAR": "QAT",
      "REPUBLIC OF KOREA": "KOR",
      "ROMANIA": "ROU",
      "RUSSIA": "RUS",
      "RUSSIAN FEDERATION": "RUS",
      "RWANDA": "RWA",
      "SAINT HELENA": "SHN",
      "SAINT KITTS AND NEVIS": "KNA",
      "SAINT LUCIA": "LCA",
      "SAINT-MARTIN (FRENCH PART)": "MAF",
      "SAINT PIERRE AND MIQUELON": "SPM",
      "SAINT VINCENT AND GRENADINES": "VCT",
      "SAMOA": "WSM",
      "SAN MARINO": "SMR",
      "SAO TOME AND PRINCIPE": "STP",
      "SAUDI ARABIA": "SAU",
      "SCOTLAND": "GBR",
      "SENEGAL": "SEN",
      "SERBIA": "SRB",
      "SEYCHELLES": "SYC",
      "SIERRA LEONE": "SLE",
      "SINGAPORE": "SGP",
      "SLOVAKIA": "SVK",
      "SLOVENIA": "SVN",
      "SOLOMON ISLANDS": "SLB",
      "SOMALIA": "SOM",
      "SOUTH AFRICA": "ZAF",
      "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS": "SGS",
      "SOUTH SUDAN": "SSD",
      "SPAIN": "ESP",
      "SRI LANKA": "LKA",
      "SUDAN": "SDN",
      "SURINAME": "SUR",
      "SVALBARD AND JAN MAYEN ISLANDS": "SJM",
      "SWAZILAND": "SWZ",
      "SWEDEN": "SWE",
      "SWITZERLAND": "CHE",
      "SYRIA": "SYR",
      "TAIWAN": "TWN",
      "TAJIKISTAN": "TJK",
      "TANZANIA": "TZA",
      "THAILAND": "THA",
      "THE NETHERLANDS": "NLD",
      "TIMOR-LESTE": "TLS",
      "TOGO": "TGO",
      "TOKELAU": "TKL",
      "TONGA": "TON",
      "TRINIDAD AND TOBAGO": "TTO",
      "TRINIDAD & TOBAGO": "TTO",
      "TUNISIA": "TUN",
      "TURKEY": "TUR",
      "TURKMENISTAN": "TKM",
      "TURKS AND CAICOS ISLANDS": "TCA",
      "TUVALU": "TUV",
      "UAE": "UAE",
      "UGANDA": "UGA",
      "UKRAINE": "UKR",
      "UNITED ARAB EMIRATES": "ARE",
      "U.A.E.": "ARE",
      "U.K.": "GBR",
      "UK": "GBR",
      "UNITED KINGDOM": "GBR",
      "US": "USA",
      "U.S.A.": "USA",
      "UNITED STATES": "USA",
      "URUGUAY": "URY",
      "UZBEKISTAN": "UZB",
      "VANUATU": "VUT",
      "VENEZUELA": "VEN",
      "VIET NAM": "VNM",
      "VIRGIN ISLANDS": "VIR",
      "WALES": "GBR",
      "WALLIS AND FUTUNA ISLANDS": "WLF",
      "WESTERN SAHARA": "ESH",
      "YEMEN": "YEM",
      "ZAMBIA": "ZMB",
      "ZIMBABWE": "ZWE"
    }
  },
  "defaults": {
    "country": "USA",
    "language": "en",
    "paymentMethod": "EFT",
    "phoneType": "Other",
    "status": "active"
  }
}
```

## User Reference Link Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/user-reference-links
```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.patron WHERE last_name IS NOT NULL",
    "pageSql": "SELECT patron_id, NVL2(institution_id, regexp_replace(institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || patron_id) AS external_system_id FROM ${SCHEMA}.patron WHERE last_name IS NOT NULL ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
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
      "partitions": 12,
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userExternalTypeId": "0ed6f994-8dbd-4827-94c0-905504169c90"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
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

> There is something with user migration that is breaking parallelization. Parallelism is set to 1 for it to succeed.

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) as total FROM ${SCHEMA}.patron WHERE last_name is not null",
    "pageSql": "SELECT patron_id, NVL2(institution_id, regexp_replace(institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || patron_id) AS external_system_id, last_name, first_name, middle_name, NVL2(expire_date, to_char(expire_date,'YYYYMMDD'), to_char(purge_date,'YYYYMMDD')) AS active_date, NVL2(expire_date, to_char(expire_date,'YYYY-MM-DD'), to_char(purge_date,'YYYY-MM-DD')) AS expire_date, sms_number, current_charges FROM ${SCHEMA}.patron WHERE last_name is not null ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "usernameSql": "SELECT uin, tamu_netid FROM PATRON.person_identifiers WHERE uin = '${EXTERNAL_SYSTEM_ID}'",
    "addressSql": "SELECT address_type, address_desc, address_line1, address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc FROM ( SELECT pa.address_type AS address_type, address_desc, address_line1, trim(address_line2) ||' '|| trim(address_line3) ||' '|| trim(address_line4) ||' '|| trim(address_line5) AS address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc, rank() over (PARTITION BY pa.address_type order by effect_date desc) dest_rank FROM ${SCHEMA}.patron_address pa, ${SCHEMA}.address_type atype, ${SCHEMA}.patron_phone pp, ${SCHEMA}.phone_type pt WHERE pa.patron_id = ${PATRON_ID} AND pa.address_id = pp.address_id(+) AND pp.phone_type = pt.phone_type(+) AND effect_date < sysdate AND atype.address_type = pa.address_type AND pa.address_type in (2,3) ) WHERE dest_rank = 1 UNION SELECT pa.address_type AS address_type, address_desc, address_line1, trim(address_line2) ||' '|| trim(address_line3) ||' '|| trim(address_line4) ||' '|| trim(address_line5) AS address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc FROM ${SCHEMA}.patron_address pa, ${SCHEMA}.address_type atype, ${SCHEMA}.patron_phone pp, ${SCHEMA}.phone_type pt WHERE pa.address_type = 1 AND pa.patron_id = ${PATRON_ID} AND atype.address_type = pa.address_type AND pa.address_id = pp.address_id(+) AND pp.phone_type = pt.phone_type(+)",
    "patronGroupSql": "SELECT barcode_status, to_char(barcode_status_date,'YYYYMMDD') AS barcode_status_date, DECODE (patron_group_code, ${DECODE}) as patron_group_level, patron_group_code, patron_barcode FROM (select barcode_status, barcode_status_date, patron_barcode, patron_group_code, rank() OVER (PARTITION BY pg.patron_group_id ORDER BY barcode_status, barcode_status_date desc) barcode_rank from ${SCHEMA}.patron_barcode pb, ${SCHEMA}.patron_group pg WHERE pb.patron_id = ${PATRON_ID} and pb.patron_group_id = pg.patron_group_id) WHERE barcode_rank = 1 ORDER BY barcode_status asc, barcode_status_date desc, patron_group_level",
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
      "driverClassName": ""
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 1,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "decodeSql": "'fast', 1, 'grad', 2, 'ungr', 3, 'illend', 4, 'libd', 5, 'comm', 6, 'cour', 7, 'texs', 8, 'nonr', 9",
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "skipDuplicates": false,
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userExternalTypeId": "0ed6f994-8dbd-4827-94c0-905504169c90"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "decodeSql": "'fac/staff', 1, 'grad/prof', 2, 'undergrad', 3",
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "skipDuplicates": true,
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userExternalTypeId": "426ce32f-388c-4edf-9c79-d6b8348148a0"
      }
    }
  ],
  "maps": {
    "patronGroup": {
      "fac/staff": "fast",
      "grad/prof": "grad",
      "undergrad": "ungr",
      "texshare": "texs",
      "other": "cour",
      "ill": "illend"
    }
  },
  "defaults": {
    "primaryAddress": true,
    "preferredContactType": "email",
    "temporaryEmail": "example@example.com"
  }
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
      "partitions": 12,
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
      "partitions": 4,
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
      "controlNumberIdentifier": "evans",
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
      "controlNumberIdentifier": "msl",
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
    "marcSql": "SELECT mfhd_id, seqnum, record_segment FROM ${SCHEMA}.mfhd_data WHERE mfhd_id = ${MFHD_ID}",
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
      "AMDB": {
        "36": "media",
        "37": "media,res",
        "47": "ils,borr",
        "48": "ils,lend",
        "132": "blcc,circ",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "201": "blcc,stand",
        "225": "blcc,nbs",
        "228": "blcc,audio",
        "241": "blcc,udoc",
        "244": "blcc,schk",
        "264": "evans_pda",
        "278": "learn_outreach",
        "285": "blcc,ebc",
        "288": "evans_withdrawn"
      },
      "MSDB": {
        "5": "AbstractIndex",
        "40": "www_msl",
        "44": "msl_withdrawn",
        "68": "Mobile",
        "126": "rs,hdr",
        "127": "rs,hdr",
        "186": "msl_pda"
      }
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
    "holdingsNotesType": {
      "note": "b160f13a-ddba-4053-b9c4-60ec5ea45d56",
      "latest_in": "b160f13a-ddba-4053-b9c4-60ec5ea45d56",
      "access": "f453de0f-8b54-4e99-9180-52932529e3a6",
      "provenance": "db9b4787-95f0-4e78-becf-26748ce6bdeb",
      "copy": "c4407cc7-d79f-4609-95bd-1cefb2e2b5c5",
      "action": "d6510242-5ec3-42ed-b593-3585d2e48fd6"
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
    "pageSql": "SELECT item_id, copy_number, item_type_id, perm_location, pieces, price, spine_label, temp_location, temp_item_type_id, magnetic_media, sensitize FROM ${SCHEMA}.item ORDER BY item_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "mfhdSql": "SELECT caption, chron, item_enum, freetext, year FROM ${SCHEMA}.mfhd_item WHERE item_id = ${ITEM_ID}",
    "barcodeSql": "SELECT item_barcode FROM ${SCHEMA}.item_barcode WHERE item_id = ${ITEM_ID}",
    "itemTypeSql": "SELECT item_type_id, item_type_code FROM ${SCHEMA}.item_type",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "itemStatusSql": "SELECT item_status, TO_CHAR(cast(item_status_date AS timestamp) AT time ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS item_status_date, ct.circ_transaction_id AS circtrans, item_status_desc FROM ${SCHEMA}.item_status istat, ${SCHEMA}.item_status_type itype, ${SCHEMA}.circ_transactions ct WHERE istat.item_id = ${ITEM_ID} AND istat.item_status = itype.item_status_type AND istat.item_id = ct.item_id(+)",
    "noteSql": "SELECT item_note, item_note_type FROM ${SCHEMA}.item_note WHERE item_note.item_id = ${ITEM_ID}",
    "materialTypeSql": "SELECT lower(normal_heading) AS mtype_code FROM ${SCHEMA}.bib_index bi, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.mfhd_item mi WHERE bi.bib_id = bm.bib_id AND bm.mfhd_id = mi.mfhd_id AND index_code = '338B' AND mi.item_id = ${ITEM_ID}",
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
      "itemNoteTypeId": "8d0a5eca-25de-4391-81a9-236eeefdd20b",
      "itemDamagedStatusId": "54d1dd76-ea33-4bcb-955b-6b29df4f7930",
      "references": {
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2",
        "itemToHoldingTypeId": "39670cf7-de23-4473-b5e3-abf6d79735e1"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "userId": "e0ffac53-6941-56e1-b6f6-0546edaf662e",
      "itemNoteTypeId": "8d0a5eca-25de-4391-81a9-236eeefdd20b",
      "itemDamagedStatusId": "54d1dd76-ea33-4bcb-955b-6b29df4f7930",
      "references": {
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0",
        "itemToHoldingTypeId": "492fea54-399a-4822-8d4b-242096c2ab12"
      }
    }
  ],
  "maps": {
    "location": {
      "AMDB": {
        "36": "media",
        "37": "media,res",
        "47": "ils,borr",
        "48": "ils,lend",
        "132": "blcc,circ",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "201": "blcc,stand",
        "225": "blcc,nbs",
        "228": "blcc,audio",
        "241": "blcc,udoc",
        "244": "blcc,schk",
        "264": "evans_pda",
        "278": "learn_outreach",
        "285": "blcc,ebc",
        "288": "evans_withdrawn"
      },
      "MSDB": {
        "5": "AbstractIndex",
        "40": "www_msl",
        "44": "msl_withdrawn",
        "68": "Mobile",
        "126": "rs,hdr",
        "127": "rs,hdr",
        "186": "msl_pda"
      }
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
    },
    "itemStatus": {
      "Not Charged": 1,
      "Charged": 2,
      "Renewed": 3,
      "Overdue": 4,
      "Recall Request": 5,
      "Hold Request": 6,
      "On Hold": 7,
      "In Transit": 8,
      "In Transit Discharged": 9,
      "In Transit On Hold": 10,
      "Discharged": 11,
      "Missing": 12,
      "Lost--Library Applied": 13,
      "Lost--System Applied": 14,
      "Claims Returned": 15,
      "Damaged": 16,
      "Withdrawn": 17,
      "At Bindery": 18,
      "Cataloging Review": 19,
      "Circulation Review": 20,
      "Scheduled": 21,
      "In Process": 22,
      "Call Slip Request": 23,
      "Short Loan Request": 24,
      "Remote Storage Request": 25
    },
    "statusName": {
     "1": "Available",
     "2": "Available",
     "3": "Available",
     "4": "Available",
     "7": "Awaiting pickup",
     "8": "In transit",
     "9": "In transit",
     "10": "In transit",
     "11": "Available",
     "12": "Missing",
     "13": "Declared lost",
     "14": "Declared lost",
     "15": "Claimed returned",
     "17": "Missing",
     "22": "In process"
    }
  },
  "defaults": {
    "permanentLoanTypeId": "dcdb0cef-c30f-4a3b-b0b6-757d1400535d",
    "permanentLocationId": "2b8f7d63-706a-4b56-8a5e-50ad24e33e4c",
    "materialTypeId": "d9acad2f-2aac-4b48-9097-e6ab85906b25"
  }
}
```

## MARC Bibliographic Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/bibs

```
{
  "extraction": {
    "countSql": "SELECT count(*) AS total FROM ${SCHEMA}.circ_transactions ct, ${SCHEMA}.patron_barcode pb, ${SCHEMA}.item_barcode ib WHERE ct.item_id = ib.item_id AND ct.patron_id = pb.patron_id",
    "pageSql": "SELECT ct.patron_id AS patron_id, patron_barcode, ct.item_id AS item_id, item_barcode, TO_CHAR(cast(charge_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS loan_date, TO_CHAR(cast(current_due_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS due_date, charge_location, circ_transaction_id, renewal_count FROM ${SCHEMA}.circ_transactions ct, ${SCHEMA}.patron_barcode pb, ${SCHEMA}.item_barcode ib WHERE ct.item_id = ib.item_id AND ct.patron_id = pb.patron_id ORDER BY ct.circ_transaction_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
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
      "partitions": 48
    },
    {
      "schema": "MSDB",
      "partitions": 4
    }
  ]
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
