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

### Running on Windows

To ensure correct character encoding follow below to develop on Windows.

```
SET PGCLIENTENCODING=UTF8
SET NLS_LANG=American_America.US7ASCII

chcp 65001
mvn clean package
java -jar -Dfile.encoding=UTF-8 target\mod-data-migration-1.0.0-SNAPSHOT.jar
```

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
      "driverClassName": "oracle.jdbc.OracleDriver"
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

## Vendor Migration

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
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "user": "tamu_admin",
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
      "user": "tamu_admin",
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
      "claim": "9491fdad-b97b-45df-9783-337d86abc80f",
      "order": "549d6d85-a51a-4cfc-8cfa-e4d46fb33823",
      "other": "71a3e461-1777-4a6f-b1cc-d4a856d141ec",
      "payment": "62042758-5266-472a-a3e9-ea1ca0ccf056",
      "return": "3c31275e-7ed8-4ec0-9b18-31243461f6ea"
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
    },
    "vendorTypes": {
      "BF": "Backfile vendor",
      "CO": "Continuations vendor",
      "DB": "Database vendor",
      "ER": "Electronic resources vendor",
      "MO": "Monographs vendor",
      "NO": "Converted from NOTIS to Voyager",
      "SR": "Serials vendor"
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
    "pageSql": "SELECT p.patron_id, (SELECT DISTINCT patron_barcode FROM (SELECT barcode_status, to_char(barcode_status_date, 'YYYYMMDD') AS barcode_status_date, DECODE (patron_group_code, ${DECODE}) AS patron_group_level, patron_group_code, patron_barcode FROM (SELECT barcode_status, barcode_status_date, patron_barcode, patron_group_code, rank() OVER (PARTITION BY pg.patron_group_id ORDER BY barcode_status, barcode_status_date desc) barcode_rank FROM ${SCHEMA}.patron_barcode pb, ${SCHEMA}.patron_group pg WHERE pb.patron_id = p.patron_id AND pb.patron_group_id = pg.patron_group_id) WHERE barcode_rank = 1 ORDER BY barcode_status asc, barcode_status_date desc, patron_group_level asc OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY)) as patron_barcode, NVL2(p.institution_id, regexp_replace(p.institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || p.patron_id) AS external_system_id FROM ${SCHEMA}.patron p WHERE last_name IS NOT NULL ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "decodeSql": "'fast', 1, 'grad', 2, 'ungr', 3, 'illend', 4, 'libd', 5, 'comm', 6, 'cour', 7, 'texs', 8, 'nonr', 9",
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userBarcodeTypeId": "f2eca16b-a6bd-4688-8424-ef5d47e06750",
        "userToBarcodeTypeId": "3ed9f301-3426-4e7f-8cc9-3044d5e1e192",
        "userExternalTypeId": "0ed6f994-8dbd-4827-94c0-905504169c90",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "decodeSql": "'fac/staff', 1, 'grad/prof', 2, 'undergrad', 3",
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userBarcodeTypeId": "9c5efc8b-4e97-4631-ad6f-6a68d9eb48de",
        "userToBarcodeTypeId": "3ed9f301-3426-4e7f-8cc9-3044d5e1e192",
        "userExternalTypeId": "426ce32f-388c-4edf-9c79-d6b8348148a0",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49"
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
    "countSql": "SELECT COUNT(*) as total FROM ${SCHEMA}.patron WHERE last_name is not null",
    "pageSql": "SELECT patron_id, NVL2(institution_id, regexp_replace(institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || patron_id) AS external_system_id, last_name, first_name, middle_name, NVL2(expire_date, to_char(expire_date,'YYYY-MM-DD'), to_char(purge_date,'YYYY-MM-DD')) AS expire_date, sms_number, current_charges FROM ${SCHEMA}.patron WHERE last_name is not null ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "usernameSql": "SELECT uin, tamu_netid FROM [itsqldev.tamu.edu].cis.patron.person_identifiers WHERE uin = '${EXTERNAL_SYSTEM_ID}'",
    "addressSql": "SELECT address_type, address_desc, address_line1, address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc FROM ( SELECT pa.address_type AS address_type, address_desc, address_line1, trim(address_line2) ||' '|| trim(address_line3) ||' '|| trim(address_line4) ||' '|| trim(address_line5) AS address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc, rank() over (PARTITION BY pa.address_type order by effect_date desc) dest_rank FROM ${SCHEMA}.patron_address pa, ${SCHEMA}.address_type atype, ${SCHEMA}.patron_phone pp, ${SCHEMA}.phone_type pt WHERE pa.patron_id = ${PATRON_ID} AND pa.address_id = pp.address_id(+) AND pp.phone_type = pt.phone_type(+) AND effect_date < sysdate AND atype.address_type = pa.address_type AND pa.address_type in (2,3) ) WHERE dest_rank = 1 UNION SELECT pa.address_type AS address_type, address_desc, address_line1, trim(address_line2) ||' '|| trim(address_line3) ||' '|| trim(address_line4) ||' '|| trim(address_line5) AS address_line2, city, state_province, zip_postal, country, address_status, phone_number, phone_desc FROM ${SCHEMA}.patron_address pa, ${SCHEMA}.address_type atype, ${SCHEMA}.patron_phone pp, ${SCHEMA}.phone_type pt WHERE pa.address_type = 1 AND pa.patron_id = ${PATRON_ID} AND atype.address_type = pa.address_type AND pa.address_id = pp.address_id(+) AND pp.phone_type = pt.phone_type(+)",
    "patronGroupSql": "SELECT barcode_status, to_char(barcode_status_date,'YYYYMMDD') AS barcode_status_date, DECODE (patron_group_code, ${DECODE}) as patron_group_level, patron_group_code, patron_barcode FROM (select barcode_status, barcode_status_date, patron_barcode, patron_group_code, rank() OVER (PARTITION BY pg.patron_group_id ORDER BY barcode_status, barcode_status_date desc) barcode_rank from ${SCHEMA}.patron_barcode pb, ${SCHEMA}.patron_group pg WHERE pb.patron_id = ${PATRON_ID} and pb.patron_group_id = pg.patron_group_id) WHERE barcode_rank = 1 ORDER BY barcode_status asc, barcode_status_date desc, patron_group_level asc",
    "patronNoteSql": "SELECT patron_id, patron_note_id, note FROM ${SCHEMA}.patron_notes WHERE ${WHERE_CLAUSE} AND patron_id = ${PATRON_ID} ORDER BY patron_note_id",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    },
    "usernameDatabase": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }
  },
  "preActions": [],
  "postActions": [
    "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"",
    "WITH temp AS (SELECT id AS userId, uuid_generate_v4() AS permId, to_char (now()::timestamp at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS createdDate, (SELECT id FROM ${TENANT}_mod_users.users WHERE jsonb->>'username' = '${TENANT}_admin') AS createdBy FROM ${TENANT}_mod_users.users WHERE jsonb->>'username' NOT IN ('${TENANT}_admin','backup_admin','pub-sub','edgeuser','vufind')) INSERT INTO ${TENANT}_mod_permissions.permissions_users (id,jsonb,creation_date,created_by) SELECT permId AS id, concat('{\"id\": \"', permId, '\", \"userId\": \"', userId, '\", \"metadata\": {\"createdDate\": \"', createdDate, '\", \"updatedDate\": \"', createdDate, '\", \"createdByUserId\": \"', createdBy, '\", \"updatedByUserId\": \"', createdBy, '\"}, \"permissions\": []}')::jsonb AS jsonb, now()::timestamp at time zone 'UTC' AS creation_date, createdBy AS created_by FROM temp",
    "UPDATE ${TENANT}_mod_users.users SET jsonb = jsonb_set(jsonb, '{personal, email}', '\"folio_user@library.tamu.edu\"') WHERE jsonb->'personal'->>'email' != 'folio_user@library.tamu.edu' AND jsonb->>'username' NOT IN ('${TENANT}_admin','backup_admin','pub-sub','edgeuser','vufind')"
  ],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "decodeSql": "'fast', 1, 'grad', 2, 'ungr', 3, 'illend', 4, 'libd', 5, 'comm', 6, 'cour', 7, 'texs', 8, 'nonr', 9",
      "user": "tamu_admin",
      "dbCode": "Evans",
      "noteWhereClause": "note IS NOT NULL",
      "noteTypeId": "659ee423-2b5c-4146-a45e-8c36ec3ad42c",
      "skipDuplicates": false,
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ],
      "barcodeReferenceTypeIds": [
        "f2eca16b-a6bd-4688-8424-ef5d47e06750",
        "9c5efc8b-4e97-4631-ad6f-6a68d9eb48de"
      ],
      "alternativeExternalReferenceTypeIds": {
        "MSDB": "426ce32f-388c-4edf-9c79-d6b8348148a0"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "decodeSql": "'fac/staff', 1, 'grad/prof', 2, 'undergrad', 3",
      "user": "tamu_admin",
      "dbCode": "MSL",
      "noteWhereClause": "note IS NOT NULL AND lower(note) NOT LIKE '%patron has graduated%'",
      "noteTypeId": "659ee423-2b5c-4146-a45e-8c36ec3ad42c",
      "skipDuplicates": true,
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ],
      "barcodeReferenceTypeIds": [
        "f2eca16b-a6bd-4688-8424-ef5d47e06750",
        "9c5efc8b-4e97-4631-ad6f-6a68d9eb48de"
      ],
      "alternativeExternalReferenceTypeIds": {
        "AMDB": "0ed6f994-8dbd-4827-94c0-905504169c90"
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
    },
    "preferredContactType": {
      "Mail": "001",
      "Email": "002",
      "Text message": "003",
      "Phone": "004",
      "Mobile phone": "005"
    }
  },
  "defaults": {
    "primaryAddress": true,
    "preferredContactType": "002",
    "temporaryEmail": "example@example.com",
    "expirationDate": "2021-09-01"
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
      "driverClassName": "oracle.jdbc.OracleDriver"
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
      "driverClassName": "oracle.jdbc.OracleDriver"
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
      "user": "tamu_admin",
      "instanceStatusId": "c8d51dc6-2f84-4220-8aef-3c4894d53b93",
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
      "user": "tamu_admin",
      "instanceStatusId": "c8d51dc6-2f84-4220-8aef-3c4894d53b93",
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
  ],
  "maps": {
    "statisticalCode": {
      "ybpebook": "ybpebooks"
    }
  }
}
```

## MARC Holdings Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/holdings

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.mfhd_master",
    "pageSql": "WITH holdings AS ( SELECT mfhd_id, suppress_in_opac, location_id, display_call_no, call_no_type, record_type, field_008 FROM ${SCHEMA}.mfhd_master ORDER BY mfhd_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY ), operators AS ( SELECT mfhd_id, operator_id FROM ${SCHEMA}.mfhd_history WHERE action_type_id = 1 AND mfhd_id IN ( SELECT mfhd_id FROM holdings ) ) SELECT h.mfhd_id, h.suppress_in_opac, h.location_id, h.display_call_no, h.call_no_type, h.record_type, substr(h.field_008,7,1) AS receipt_status, substr(h.field_008,8,1) AS acq_method, substr(h.field_008,13,1) AS retention, o.operator_id FROM holdings h LEFT JOIN operators o ON h.mfhd_id = o.mfhd_id",
    "marcSql": "SELECT mfhd_id, seqnum, record_segment FROM ${SCHEMA}.mfhd_data WHERE mfhd_id = ${MFHD_ID}",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 48,
      "user": "tamu_admin",
      "references": {
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "holdingToBibTypeId": "0ff1680d-caf5-4977-a78f-2a4fd64a2cdc",
        "holdingToCallNumberPrefixTypeId": "bdc5c8b1-7b21-45ea-943b-585764f3715c",
        "holdingToCallNumberSuffixTypeId": "fcd2963b-b75d-4401-8eda-7e91efd8ddc3"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "user": "tamu_admin",
      "references": {
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "holdingToBibTypeId": "f8252895-6bf5-4458-8a3f-57bd8c36c6ba",
        "holdingToCallNumberPrefixTypeId": "78991218-9141-4807-9175-7147c861a596",
        "holdingToCallNumberSuffixTypeId": "be7288c9-7c67-4b6b-b662-a57816569e46"
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
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
        "186": "msl_pda"
      }
    },
    "statisticalCode": {
      "ybpebook": "ybpebooks"
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
      "action": "d6510242-5ec3-42ed-b593-3585d2e48fd6",
      "access": "f453de0f-8b54-4e99-9180-52932529e3a6",
      "provenance": "db9b4787-95f0-4e78-becf-26748ce6bdeb",
      "copy": "c4407cc7-d79f-4609-95bd-1cefb2e2b5c5",
      "binding": "e19eabab-a85c-4aef-a7b2-33bd9acef24e",
      "reproduction": "6a41b714-8574-4084-8d64-a9373c3fbb59",
      "latest_in": "7ca7dc63-c053-4aec-8272-c03aeda4840c",
      "note": "b160f13a-ddba-4053-b9c4-60ec5ea45d56"
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
    },
    "fieldRegexExclusion": {
      "583": "(?i).*[sub pattern created|subpattern created|subscription pattern created].*"
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
    "mfhdSql": "SELECT mi.caption, mi.chron, mi.item_enum, mi.freetext, mi.year, mm.location_id, mm.call_no_type, mm.display_call_no FROM ${SCHEMA}.mfhd_item mi LEFT JOIN ${SCHEMA}.mfhd_master mm ON mi.mfhd_id = mm.mfhd_id WHERE item_id = ${ITEM_ID}",
    "barcodeSql": "SELECT item_barcode FROM ${SCHEMA}.item_barcode WHERE item_id = ${ITEM_ID} AND barcode_status = '1'",
    "itemTypeSql": "SELECT item_type_id, item_type_code FROM ${SCHEMA}.item_type",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "itemStatusSql": "SELECT item_status, TO_CHAR(cast(item_status_date AS timestamp) AT time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS item_status_date, ct.circ_transaction_id AS circtrans, item_status_desc FROM ${SCHEMA}.item_status istat, ${SCHEMA}.item_status_type itype, ${SCHEMA}.circ_transactions ct WHERE istat.item_id = ${ITEM_ID} AND istat.item_status = itype.item_status_type AND istat.item_id = ct.item_id(+) ORDER BY case when item_status_desc ='Charged' then '01' when item_status_desc ='Renewed' then '02' when item_status_desc ='Overdue' then '03' when item_status_desc ='Recall Request' then '04' when item_status_desc ='Hold Request' then '05' when item_status_desc ='On Hold' then '06' when item_status_desc ='Missing' then '07' when item_status_desc ='Lost--Library Applied' then '08' when item_status_desc ='Lost--System Applied' then '09' when item_status_desc ='In Transit' then '10' when item_status_desc ='In Transit Discharged' then '11' when item_status_desc ='In Transit On Hold' then '12' when item_status_desc ='Withdrawn' then '13' when item_status_desc ='Claims Returned' then '16' when item_status_desc ='Damaged' then '17' when item_status_desc ='At Bindery' then '18' when item_status_desc ='Cataloging Review' then '19' when item_status_desc ='Circulation Review' then '20' when item_status_desc ='Scheduled' then '21' when item_status_desc ='In Process' then '22' when item_status_desc ='Call Slip Request' then '23' when item_status_desc ='Short Loan Request' then '24' when item_status_desc ='Remote Storage Request' then '25' end",
    "noteSql": "SELECT item_note, item_note_type FROM ${SCHEMA}.item_note WHERE item_note.item_id = ${ITEM_ID}",
    "materialTypeSql": "SELECT lower(normal_heading) AS mtype_code FROM ${SCHEMA}.bib_index bi, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.mfhd_item mi WHERE bi.bib_id = bm.bib_id AND bm.mfhd_id = mi.mfhd_id AND index_code = '338B' AND mi.item_id = ${ITEM_ID}",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 48,
      "user": "tamu_admin",
      "itemNoteTypeId": "8d0a5eca-25de-4391-81a9-236eeefdd20b",
      "itemDamagedStatusId": "54d1dd76-ea33-4bcb-955b-6b29df4f7930",
      "references": {
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2",
        "itemToHoldingTypeId": "39670cf7-de23-4473-b5e3-abf6d79735e1",
        "holdingToCallNumberPrefixTypeId": "bdc5c8b1-7b21-45ea-943b-585764f3715c",
        "holdingToCallNumberSuffixTypeId": "fcd2963b-b75d-4401-8eda-7e91efd8ddc3"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "user": "tamu_admin",
      "itemNoteTypeId": "8d0a5eca-25de-4391-81a9-236eeefdd20b",
      "itemDamagedStatusId": "54d1dd76-ea33-4bcb-955b-6b29df4f7930",
      "references": {
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0",
        "itemToHoldingTypeId": "492fea54-399a-4822-8d4b-242096c2ab12",
        "holdingToCallNumberPrefixTypeId": "78991218-9141-4807-9175-7147c861a596",
        "holdingToCallNumberSuffixTypeId": "be7288c9-7c67-4b6b-b662-a57816569e46"
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
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
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
      "HSCjournal": "4_hour",
      "HSCmedia": "media",
      "HSCreserve": "reserve",
      "rare": "noncirc",
      "HSCnocirc": "noncirc",
      "preserv": "preservation",
      "HSCtablet": "tablet",
      "7day": "7_day",
      "4hour": "4_hour",
      "14day": "14_day",
      "30day": "30_day"
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
      "7": "Available",
      "8": "In transit",
      "9": "In transit",
      "10": "In transit",
      "11": "Available",
      "12": "Missing",
      "13": "Declared lost",
      "14": "Aged to lost",
      "15": "Claimed returned",
      "17": "Withdrawn",
      "22": "In process"
    },
    "custodianStatisticalCode": {
      "AMDB": "38c86b2b-8156-4b7e-943e-460e15ea0dc0",
      "MSDB": "f5d1069d-f718-4f0e-8ff8-308f55540daf"
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
    }
  },
  "defaults": {
    "permanentLoanTypeId": "42865fba-5fd1-4275-85ff-7c5865c6990a",
    "permanentLocationId": "2b8f7d63-706a-4b56-8a5e-50ad24e33e4c",
    "materialTypeId": "3212b3e9-bce7-4ff1-88e2-8def758ba977",
    "callNumberTypeId": "24badefa-4456-40c5-845c-3f45ffbc4c03"
  }
}
```

## Bound-With Instance Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/boundwith

```
{
  "extraction": {
    "countSql": "WITH boundwith AS (SELECT DISTINCT mfhd_id FROM ${SCHEMA}.bib_mfhd WHERE mfhd_id IN (SELECT mfhd_id FROM ${SCHEMA}.bib_mfhd GROUP BY mfhd_id HAVING COUNT(rownum) > 1)) SELECT COUNT(*) AS total FROM boundwith",
    "pageSql": "SELECT DISTINCT mfhd_id, LISTAGG(bib_id, ',') WITHIN GROUP(ORDER BY mfhd_id) AS bound_with FROM ${SCHEMA}.bib_mfhd WHERE mfhd_id IN (SELECT mfhd_id FROM ${SCHEMA}.bib_mfhd GROUP BY mfhd_id HAVING COUNT(rownum) > 1) GROUP BY mfhd_id ORDER BY mfhd_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 10,
      "references": {
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672"
      },
      "statusId": "daf2681c-25af-4202-a3fa-e58fdf806183",
      "instanceTypeId": "6312d172-f0cf-40f6-b27d-9fa8feaf332f",
      "modeOfIssuanceId": "612bbd3d-c16b-4bfb-8517-2afafc60204a",
      "instanceRelationshipTypeId": "758f13db-ffb4-440e-bb10-8a364aa6cb4a",
      "holdingsTypeId": "61155a36-148b-4664-bb7f-64ad708e0b32"
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "references": {
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "instanceTypeId": "fb6db4f0-e5c3-483b-a1da-3edbb96dc8e8"
      },
      "statusId": "daf2681c-25af-4202-a3fa-e58fdf806183",
      "instanceTypeId": "6312d172-f0cf-40f6-b27d-9fa8feaf332f",
      "modeOfIssuanceId": "612bbd3d-c16b-4bfb-8517-2afafc60204a",
      "instanceRelationshipTypeId": "758f13db-ffb4-440e-bb10-8a364aa6cb4a",
      "holdingsTypeId": "61155a36-148b-4664-bb7f-64ad708e0b32"
    }
  ]
}
```

## Request Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/requests

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ${SCHEMA}.hold_recall hr, ${SCHEMA}.hold_recall_items hri, ${SCHEMA}.hold_recall_status hrs, ${SCHEMA}.bib_text bt, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.mfhd_item mi, ${SCHEMA}.item_barcode ib, ${SCHEMA}.patron p, ${SCHEMA}.location pickup_loc WHERE hr.hold_recall_id = hri.hold_recall_id AND hri.hold_recall_status = hrs.hr_status_type AND hri.item_id = mi.item_id AND mi.mfhd_id = bm.mfhd_id AND bm.bib_id = bt.bib_id AND mi.item_id = ib.item_id AND p.patron_id = hr.patron_id AND hr.pickup_location = pickup_loc.location_id",
    "pageSql": "SELECT hr.hold_recall_id AS hr_id, CASE WHEN hr.hold_recall_type = 'H' AND ct.item_id IS NOT NULL THEN 'Hold' WHEN hr.hold_recall_type = 'R' AND ct.item_id IS NOT NULL THEN 'Recall' ELSE 'Page' END AS requesttype, TO_CHAR(cast(hr.create_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS requestdate, hri.item_id AS item_id, CASE WHEN hrs.hr_status_desc = 'Pending' THEN 'Open - Awaiting pickup' WHEN hrs.hr_status_desc = 'Active' THEN 'Open - Not yet filled' END AS status, hri.queue_position AS position, bt.title_brief AS item_title, ib.item_barcode AS item_barcode, NVL2(p.institution_id, regexp_replace(p.institution_id, '([[:digit:]]{3})-([[:digit:]]{2})-([[:digit:]]{4})', '\\1\\2\\3'), '${SCHEMA}_' || p.patron_id) AS requester_external_system_id, p.last_name AS requester_lastname, p.first_name AS requester_firstname, 'Hold Shelf' AS fulfilmentpreference, CASE WHEN hrs.hr_status_desc = 'Pending' AND hr.expire_date IS NOT NULL THEN TO_CHAR(cast(hr.expire_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') END AS holdshelfexpirationdate, CASE WHEN hrs.hr_status_desc = 'Active' AND hr.expire_date IS NOT NULL THEN TO_CHAR(cast(hr.expire_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') END AS requestexpirationdate, pickup_loc.location_id AS location_id FROM ${SCHEMA}.hold_recall hr, ${SCHEMA}.hold_recall_items hri, ${SCHEMA}.hold_recall_status hrs, ${SCHEMA}.bib_text bt, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.mfhd_item mi, ${SCHEMA}.item_barcode ib, ${SCHEMA}.patron p, ${SCHEMA}.location pickup_loc, ${SCHEMA}.circ_transactions ct WHERE hr.hold_recall_id = hri.hold_recall_id AND hri.hold_recall_status = hrs.hr_status_type AND hri.item_id = mi.item_id AND mi.mfhd_id = bm.mfhd_id AND bm.bib_id = bt.bib_id AND mi.item_id = ib.item_id AND p.patron_id = hr.patron_id AND hr.pickup_location = pickup_loc.location_id AND hri.item_id = ct.item_id(+) ORDER BY position, hr_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 2,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 1,
      "references": {
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "references": {
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0"
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
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
        "186": "msl_pda"
      }
    },
    "locationCode": {
      "media,res": "media",
      "ils,borr": "ils",
      "ils,lend": "ils",
      "circ,schk": "circ",
      "psel,ref": "psel,circ",
      "psel,res": "psel,circ",
      "blcc,res": "blcc,circ",
      "west,res": "blcc,circ",
      "msl,circ": "CircDesk",
      "ResDesk": "CircDesk",
      "CircDesk": "CircDesk",
      "Mobile": "CircDesk",
      "kingsvl": "kingsville"
    }
  }
}
```

## Loan Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/loans

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM (SELECT DISTINCT ct.patron_id AS patron_id, ct.item_id AS item_id, item_barcode, charge_location, circ_transaction_id, renewal_count FROM ${SCHEMA}.circ_transactions ct, ${SCHEMA}.patron_barcode pb, ${SCHEMA}.item_barcode ib WHERE ct.item_id = ib.item_id AND ct.patron_id = pb.patron_id AND pb.patron_group_id != 14)",
    "pageSql": "SELECT DISTINCT ct.patron_id AS patron_id, ct.item_id AS item_id, item_barcode, TO_CHAR(cast(charge_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS loan_date, TO_CHAR(cast(current_due_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS due_date, charge_location, circ_transaction_id, renewal_count FROM ${SCHEMA}.circ_transactions ct, ${SCHEMA}.patron_barcode pb, ${SCHEMA}.item_barcode ib WHERE ct.item_id = ib.item_id AND ct.patron_id = pb.patron_id AND pb.patron_group_id != 14 ORDER BY ct.circ_transaction_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [
    "DELETE FROM ${TENANT}_mod_circulation_storage.patron_action_session",
    "DELETE FROM ${TENANT}_mod_circulation_storage.scheduled_notice WHERE (jsonb->>'nextRunTime')::timestamp < NOW()"
  ],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49"
      },
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ]
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49"
      },
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ]
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
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
        "186": "msl_pda"
      }
    },
    "locationCode": {
      "media,res": "media",
      "ils,borr": "ils",
      "ils,lend": "ils",
      "circ,schk": "circ",
      "psel,ref": "psel,circ",
      "psel,res": "psel,circ",
      "blcc,res": "blcc,circ",
      "west,res": "blcc,circ",
      "msl,circ": "CircDesk",
      "ResDesk": "CircDesk",
      "CircDesk": "CircDesk",
      "Mobile": "CircDesk",
      "kingsvl": "kingsville"
    }
  }
}
```

## Fee/Fine Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/feesfines

```
{
  "extraction": {
    "countSql": "SELECT COUNT(*) AS total FROM ( SELECT distinct patron_id, ff.item_id AS item_id, item_barcode, fine_fee_id, fine_fee_amount/100 AS amount, fine_fee_balance/100 AS remaining, fine_fee_type, fine_fee_note, to_char(cast(ff.create_date AS timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS create_date, mi.mfhd_id AS mfhd_id, display_call_no, item_enum, chron, CASE WHEN i.temp_location > 0 then i.temp_location ELSE i.perm_location end AS effective_location, ff.fine_fee_location AS fine_location, substr(title,1,80) AS title, bm.bib_id AS bib_id FROM ${SCHEMA}.fine_fee ff, ${SCHEMA}.item_barcode ib, ${SCHEMA}.mfhd_item mi, ${SCHEMA}.mfhd_master mm, ${SCHEMA}.item i, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.bib_text bt WHERE ff.item_id = ib.item_id(+) AND ff.item_id = mi.item_id AND mi.mfhd_id = mm.mfhd_id AND ff.item_id = i.item_id AND mm.mfhd_id = bm.mfhd_id AND bm.bib_id = bt.bib_id AND (ib.barcode_status = 1 or ib.item_id is null) AND fine_fee_type IN (1,2,3,6,7) AND fine_fee_balance > 0 AND ff.create_date > '31-Dec-2012' UNION SELECT distinct patron_id, null AS item_id, null AS item_barcode, fine_fee_id, fine_fee_amount/100 AS amount, fine_fee_balance/100 AS remaining, fine_fee_type, fine_fee_note, to_char(cast(ff.create_date AS timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS create_date, null AS mfhd_id, null AS display_call_no, null AS item_enum, null AS chron, null AS effective_location, ff.fine_fee_location AS fine_location, null AS title, null AS bib_id FROM ${SCHEMA}.fine_fee ff WHERE ff.item_id = 0 AND fine_fee_type IN (1,2,3,6,7) AND fine_fee_balance > 0 AND ff.create_date > '31-Dec-2012' ORDER BY patron_id, create_date )",
    "pageSql": "SELECT distinct patron_id, ff.item_id AS item_id, item_barcode, fine_fee_id, fine_fee_amount/100 AS amount, fine_fee_balance/100 AS remaining, fine_fee_type, fine_fee_note, to_char(cast(ff.create_date AS timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS create_date, mi.mfhd_id AS mfhd_id, display_call_no, item_enum, chron, CASE WHEN i.temp_location > 0 then i.temp_location ELSE i.perm_location end AS effective_location, ff.fine_fee_location AS fine_location, substr(title,1,80) AS title, bm.bib_id AS bib_id FROM ${SCHEMA}.fine_fee ff, ${SCHEMA}.item_barcode ib, ${SCHEMA}.mfhd_item mi, ${SCHEMA}.mfhd_master mm, ${SCHEMA}.item i, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.bib_text bt WHERE ff.item_id = ib.item_id(+) AND ff.item_id = mi.item_id AND mi.mfhd_id = mm.mfhd_id AND ff.item_id = i.item_id AND mm.mfhd_id = bm.mfhd_id AND bm.bib_id = bt.bib_id AND (ib.barcode_status = 1 or ib.item_id is null) AND fine_fee_type IN (1,2,3,6,7) AND fine_fee_balance > 0 AND ff.create_date > '31-Dec-2012' UNION SELECT distinct patron_id, null AS item_id, null AS item_barcode, fine_fee_id, fine_fee_amount/100 AS amount, fine_fee_balance/100 AS remaining, fine_fee_type, fine_fee_note, to_char(cast(ff.create_date AS timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS create_date, null AS mfhd_id, null AS display_call_no, null AS item_enum, null AS chron, null AS effective_location, ff.fine_fee_location AS fine_location, null AS title, null AS bib_id FROM ${SCHEMA}.fine_fee ff WHERE ff.item_id = 0 AND fine_fee_type IN (1,2,3,6,7) AND fine_fee_balance > 0 AND ff.create_date > '31-Dec-2012' ORDER BY patron_id, create_date OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "materialTypeSql": "SELECT distinct lower(normal_heading) AS mtype_code FROM ${SCHEMA}.bib_index bi, ${SCHEMA}.bib_mfhd bm, ${SCHEMA}.mfhd_item mi, ${SCHEMA}.fine_fee ff WHERE bi.bib_id = bm.bib_id AND bm.mfhd_id = mi.mfhd_id AND mi.item_id = ff.item_id AND index_code = '338B' AND ff.item_id = ${ITEM_ID}",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 12,
      "user": "tamu_admin",
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672",
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5",
        "itemTypeId": "53e72510-dc82-4caa-a272-1522cca70bc2"
      },
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ]
    },
    {
      "schema": "MSDB",
      "partitions": 4,
      "user": "tamu_admin",
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45",
        "userToExternalTypeId": "6d451e5d-371a-48ec-b59d-28be5508df49",
        "instanceTypeId": "fb6db4f0-e5c3-483b-a1da-3edbb96dc8e8",
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12",
        "itemTypeId": "0014559d-39f6-45c7-9406-03643459aaf0"
      },
      "userExternalReferenceTypeIds": [
        "0ed6f994-8dbd-4827-94c0-905504169c90",
        "426ce32f-388c-4edf-9c79-d6b8348148a0"
      ]
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
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
        "186": "msl_pda"
      }
    },
    "feefineTypeLabels": {
      "1": "Overdue (migrated-do not use)",
      "2": "Lost item replacement (migrated-do not use)",
      "3": "Lost item processing (migrated-do not use)",
      "6": "Lost item replacement (migrated-do not use)",
      "7": "Lost item processing (migrated-do not use)"
    },
    "feefineOwner": {
      "AMDB": {
        "^(24|36|37|47|48|51|223|242)$": {
          "ownerId": "e7942c89-74f1-419f-ae7c-56336e0c4ff0",
          "feeFineOwner": "AskUs Services",
          "fineFeeType": {
            "1": "f55678e3-b8a2-43a5-a8ca-8d89c99df283",
            "2": "eead2e33-4784-4b50-9e86-4101c16e8b25",
            "6": "eead2e33-4784-4b50-9e86-4101c16e8b25",
            "3": "869ae91d-1960-4746-a561-f63e2b429f97",
            "7": "869ae91d-1960-4746-a561-f63e2b429f97"
          }
        },
        "^(132|136)$": {
          "ownerId": "014416f2-7609-4222-a812-1a3deb0591b8",
          "feeFineOwner": "Business Library & Collaboration Commons",
          "fineFeeType": {
            "1": "3b188387-dbd4-4fb3-9a9b-847f72cab0d7",
            "2": "09e14083-c85b-4f41-86ac-b24c47a81b7b",
            "6": "09e14083-c85b-4f41-86ac-b24c47a81b7b",
            "3": "672fd96c-e175-4a4f-82e8-8d5362da34f7",
            "7": "672fd96c-e175-4a4f-82e8-8d5362da34f7"
          }
        },
        "^(191)$": {
          "ownerId": "26c4ddaf-95ad-44d6-bd93-8492e278a41e",
          "feeFineOwner": "Qatar Library (TAMUQ)",
          "fineFeeType": {
            "1": "69482cfd-efce-4a32-a001-a6d9bdd217ec",
            "2": "11a3e001-9762-41a2-9aa0-8f373f0270ca",
            "6": "11a3e001-9762-41a2-9aa0-8f373f0270ca",
            "3": "3761b4f5-c1a9-46b9-b12f-6a1c76fb5eb1",
            "7": "3761b4f5-c1a9-46b9-b12f-6a1c76fb5eb1"
          }
        },
        "^(166)$": {
          "ownerId": "7e6832e2-b047-471e-9194-7e4106e0af1e",
          "feeFineOwner": "Policy Sciences & Economics Library",
          "fineFeeType": {
            "1": "8d252cda-dda5-4b5c-9d59-be06e47066fb",
            "2": "75426640-40d2-460a-86d5-7691bf1c6d2a",
            "6": "75426640-40d2-460a-86d5-7691bf1c6d2a",
            "3": "8e0913e4-4569-4250-9d34-5139c5705884",
            "7": "8e0913e4-4569-4250-9d34-5139c5705884"
          }
        }
      },
      "MSDB": {
        "^.+$": {
          "ownerId": "2eb797c3-8309-4831-a84b-3ca2eeeb2876",
          "feeFineOwner": "Medical Sciences Library",
          "fineFeeType": {
            "1": "bdef6fb7-9380-40a2-9c5b-4ab4e3cbe7ff",
            "2": "aa9183b3-ed54-4c09-a5d6-0d8e574dcc43",
            "6": "aa9183b3-ed54-4c09-a5d6-0d8e574dcc43",
            "3": "a4b170b9-f572-4bc0-b39a-9426093dc280",
            "7": "a4b170b9-f572-4bc0-b39a-9426093dc280"
          }
        }
      }
    }
  },
  "defaults": {
    "materialTypeId": "3212b3e9-bce7-4ff1-88e2-8def758ba977",
    "itemId": "0"
  }
}
```

## Proxy For User Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/proxyfor

```
{
  "extraction": {
    "countSql": "WITH proxies AS (SELECT DISTINCT sponsor.patron_id AS patron_id, proxy_bar.patron_id AS proxy_patron_id, to_char(proxy.expiration_date,'YYYY-MM-DD') AS expiration_date FROM ${SCHEMA}.patron_barcode sponsor, ${SCHEMA}.patron_barcode proxy_bar, ${SCHEMA}.proxy_patron proxy WHERE sponsor.patron_barcode_id = proxy.patron_barcode_id AND proxy_bar.patron_barcode_id = proxy.patron_barcode_id_proxy) SELECT COUNT(*) AS total FROM proxies",
    "pageSql": "SELECT DISTINCT sponsor.patron_id AS patron_id, proxy_bar.patron_id AS proxy_patron_id, to_char(proxy.expiration_date,'YYYY-MM-DD') AS expiration_date FROM ${SCHEMA}.patron_barcode sponsor, ${SCHEMA}.patron_barcode proxy_bar, ${SCHEMA}.proxy_patron proxy WHERE sponsor.patron_barcode_id = proxy.patron_barcode_id AND proxy_bar.patron_barcode_id = proxy.patron_barcode_id_proxy ORDER BY patron_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 1,
      "user": "tamu_admin",
      "references": {
        "userTypeId": "fb86289b-001d-4a6f-8adf-5076b162a6c7"
      }
    },
    {
      "schema": "MSDB",
      "partitions": 1,
      "user": "tamu_admin",
      "references": {
        "userTypeId": "7a244692-dc96-48f1-9bf8-39578b8fee45"
      }
    }
  ]
}
```

## DivIT Patron Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/divitpatron

```
{
  "database": {
    "url": "",
    "username": "",
    "password": "",
    "driverClassName": "com.microsoft.sqlserver.jdbc.SQLServerDriver"
  },
  "preActions": [],
  "postActions": [
    "UPDATE ${TENANT}_mod_users.users SET jsonb = jsonb_set(jsonb, '{personal, email}', '\"folio_user@library.tamu.edu\"') WHERE jsonb->'personal'->>'email' != 'folio_user@library.tamu.edu' AND jsonb->>'username' NOT IN ('${TENANT}_admin','backup_admin','pub-sub','edgeuser','vufind')"
  ],
  "parallelism": 12,
  "jobs": [
    {
      "name": "employee",
      "sql": "SELECT DISTINCT CASE WHEN emp.tamu_netid IS NOT NULL THEN emp.tamu_netid ELSE emp.uin END AS username, emp.uin AS externalSystemId, CASE WHEN pi.id_card_num IS NOT NULL THEN pi.id_card_num ELSE emp.uin END AS barcode, 'true' AS active, 'fast' AS patronGroup, emp.last_name AS personal_lastName, emp.first_name AS personal_firstName, emp.middle_name AS personal_middleName, emp.tamu_preferred_alias AS personal_email, emp.office_phone AS personal_phone, 'Permanent' AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, emp.mail_street AS addresses_permanent_addressLine1, NULL AS addresses_permanent_addressLine2, emp.mail_city AS addresses_permanent_city, emp.mail_state AS addresses_permanent_region, emp.mail_zip AS addresses_permanent_postalCode, 'Temporary' AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine2, emp.adloc_dept_name AS addresses_temporary_addressLine1, emp.work_city AS addresses_temporary_city, emp.work_state AS addresses_temporary_region, emp.work_zip AS addresses_temporary_postalCode, emp.adloc_dept AS departments_0, format(getdate() + 500, 'yyyy-MM-dd') AS expirationDate FROM [itsqldev.tamu.edu].cis.patron.employees_retirees emp LEFT OUTER JOIN [itsqldev.tamu.edu].cis.patron.person_identifiers pi ON emp.uin = pi.uin WHERE upper(emp.adloc_system_member_name) IN( 'TEXAS A&M AGRILIFE EXTENSION SERVICE', 'TEXAS A&M AGRILIFE RESEARCH', 'TEXAS A&M ENGINEERING EXPERIMENT STATION', 'TEXAS A&M ENGINEERING EXTENSION SERVICE', 'TEXAS A&M FOREST SERVICE', 'TEXAS A&M HEALTH', 'TEXAS A&M SYSTEM OFFICES', 'TEXAS A&M SYSTEM SHARED SERVICE CENTER', 'TEXAS A&M SYSTEM SPONSORED RESEARCH SERVICES', 'TEXAS A&M SYSTEM TECHNOLOGY COMMERCIALIZATION', 'TEXAS A&M TRANSPORTATION INSTITUTE', 'TEXAS A&M UNIVERSITY', 'TEXAS A&M UNIVERSITY AT GALVESTON', 'TEXAS A&M VETERINARY MEDICAL DIAGNOSTIC LABORATORY') AND employment_status_name NOT IN ( 'Affiliate Non-Employee', 'Deceased', 'Terminated' ) AND emp.employee_type_name != 'Student' AND emp.employee_type_name IS NOT NULL AND ( emp.last_updated > getdate() - 1 OR pi.last_updated > getdate() - 1 ) AND emp.last_name IS NOT NULL"
    },
    {
      "name": "student",
      "sql": "SELECT DISTINCT CASE WHEN stu.tamu_netid IS NOT NULL THEN stu.tamu_netid ELSE stu.uin END AS username, stu.uin AS externalSystemId, CASE WHEN pi.id_card_num IS NOT NULL THEN pi.id_card_num ELSE stu.uin END AS barcode, 'true' AS active, CASE WHEN substring(stu.classification, 1, 1) IN('D', 'G', 'L', 'M', 'P', 'V') THEN 'grad' WHEN substring(stu.classification, 1, 1) IN ('I', 'U') THEN 'ungr' END AS patronGroup, stu.last_name AS personal_lastName, stu.first_name AS personal_firstName, stu.middle_name AS personal_middleName, stu.tamu_preferred_alias AS personal_email, stu.local_phone AS personal_phone, 'Permanent' AS addresses_permanent_addressTypeId, stu.perm_country AS addresses_permanent_countryId, stu.perm_street1 AS addresses_permanent_addressLine1, stu.perm_street2 + ' ' + stu.perm_street3 AS addresses_permanent_addressLine2, stu.perm_city AS addresses_permanent_city, stu.perm_state AS addresses_permanent_region, stu.perm_zip AS addresses_permanent_postalCode, 'Temporary' AS addresses_temporary_addressTypeId, stu.local_street1 AS addresses_temporary_addressLine1, stu.local_street2 + ' ' + stu.local_street3 AS addresses_temporary_addressLine2, stu.local_city AS addresses_temporary_city, stu.local_state AS addresses_temporary_region, stu.local_zip AS addresses_temporary_postalCode, stu.acad_dept AS departments_0, format(getdate() + 200, 'yyyy-MM-dd') AS expirationDate FROM [itsqldev.tamu.edu].cis.patron.students stu LEFT OUTER JOIN [itsqldev.tamu.edu].cis.patron.employees_retirees emp ON stu.uin = emp.uin LEFT OUTER JOIN [itsqldev.tamu.edu].cis.patron.person_identifiers pi ON stu.uin = pi.uin WHERE stu.enroll_status_name IN ('Enrolled', 'Not Enrolled') AND ( emp.employee_type = 1 OR emp.uin IS NULL OR emp.employee_type IS NULL) AND ( stu.last_updated > getdate() - 1 OR pi.last_updated > getdate() - 1 ) AND stu.last_name IS NOT NULL"
    },
    {
      "name": "other people",
      "sql": "SELECT DISTINCT nvl2(op.tamu_netid, op.tamu_netid, op.uin) AS username, op.uin AS externalSystemId, NVL2(pi.id_card_num, pi.id_card_num, op.uin) AS barcode, 'true' AS active, 'fast' AS patronGroup, op.last_name AS personal_lastName, op.first_name AS personal_firstName, op.middle_name AS personal_middleName, op.tamu_preferred_alias AS personal_email, op.office_phone AS personal_phone, NULL AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, NULL AS addresses_permanent_addressLine1, NULL AS addresses_permanent_addressLine2, NULL AS addresses_permanent_city, NULL AS addresses_permanent_region, NULL AS addresses_permanent_postalCode, NULL AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine1, NULL AS addresses_temporary_addressLine2, NULL AS addresses_temporary_city, NULL AS addresses_temporary_region, NULL AS addresses_temporary_postalCode, NULL AS departments_0, to_char(sysdate+200, 'yyyy-MM-dd') AS expirationDate FROM patron.other_people op, patron.person_identifiers pi, patron.employees_retirees emp, students stu WHERE op.uin = pi.uin(+) AND op.uin = stu.uin(+) AND op.uin = emp.uin(+) AND stu.uin IS NULL AND emp.uin IS NULL AND op.tamu_preferred_alias IS NOT NULL AND((affiliate_role in ('affiliate:continuingeducationstudent', 'affiliate:clinicaltrainee', 'affiliate:faculty:future', 'affiliate:graduateassistant:future', 'affiliate:librarian', 'affiliate:medicalresident', 'affiliate:regent', 'affiliate:staff:future', 'affiliate:usda', 'affiliate:veteransprogram', 'affiliate:visitingscholar', 'employee:faculty:retired', 'faculty:adjunct') AND system_member in ('01', '02', '06', '07', '09', '10', '11', '12', '20', '23', '26', '28')) OR (op.data_provider in ('HSCAFFILIATES', 'QATAR'))) AND (op.last_updated > sysdate-1 OR pi.last_updated > sysdate-1) AND op.last_name IS NOT NULL"
    }
  ]
}
```

## Purchase Order Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/purchaseorders

```
{
  "extraction": {
    "countSql": "WITH orders AS (SELECT DISTINCT ${COLUMNS} FROM ${TABLES} WHERE ${CONDITIONS}) SELECT COUNT(*) AS total FROM orders",
    "pageSql": "SELECT DISTINCT ${COLUMNS} FROM ${TABLES} WHERE ${CONDITIONS} ORDER BY po.po_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "lineItemNotesSql": "SELECT DISTINCT note FROM ${SCHEMA}.line_item_notes lin, ${SCHEMA}.line_item li WHERE li.line_item_id = lin.line_item_id AND li.po_id = ${PO_ID}",
    "poLinesSql": "SELECT DISTINCT ${COLUMNS} FROM ${TABLES} WHERE ${CONDITIONS}",
    "receivingHistorySql": "SELECT component.predict AS predict, opac_suppressed, component.note AS receiving_note, enumchron, TO_CHAR(cast(ir.receipt_date as timestamp) at time zone 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS received_date, lics.mfhd_id AS mfhd_id FROM ${SCHEMA}.line_item_copy_status lics, ${SCHEMA}.issues_received ir, ${SCHEMA}.component, ${SCHEMA}.serial_issues si WHERE lics.line_item_id = ${LINE_ITEM_ID} AND ir.copy_id = lics.copy_id AND ir.component_id = component.component_id AND si.component_id = component.component_id AND si.issue_id = ir.issue_id AND component.predict = 'Y' AND opac_suppressed = 1 ORDER BY lics.line_item_id, ir.receipt_date desc",
    "locationSql": "SELECT location_id, location_code FROM ${SCHEMA}.location",
    "database": {
      "url": "",
      "username": "",
      "password": "",
      "driverClassName": "oracle.jdbc.OracleDriver"
    }
  },
  "preActions": [],
  "postActions": [],
  "parallelism": 12,
  "jobs": [
    {
      "schema": "AMDB",
      "partitions": 10,
      "pageAdditionalContext": {
        "COLUMNS": "po.po_id, po.po_number, po.po_status, po.vendor_id, shipto.location_code AS shiploc, billto.location_code AS billloc",
        "TABLES": "AMDB.purchase_order po, AMDB.po_status stat, AMDB.location shipto, AMDB.location billto",
        "CONDITIONS": "po.ship_location = shipto.location_id AND po.bill_location = billto.location_id AND po.po_status = stat.po_status AND shipto.location_code IN ('SR', 'SRDB', 'SRDBProcar', 'SRDIR', 'SRDIRM', 'SRDIRMP', 'SRDIRN', 'SRDIRO', 'SRDIRP', 'SRGFT', 'SRMSV', 'SRMSVM', 'SRMSVMO', 'SRMSVO', 'SRMSVP', 'SRMSVPM', 'SRMSVW', 'SRMSVWM', 'SRProcard', 'SRSOV', 'SRSOVM', 'SRVSVO', 'SRSUSPENDED')"
      },
      "poNumberPrefix": "evans",
      "includeAddresses": true,
      "references": {
        "vendorTypeId": "08c7dd18-dbaf-11e9-8a34-2a2ae2dbcce4",
        "instanceTypeId": "43efa217-2d57-4d75-82ef-4372507d0672",
        "holdingTypeId": "67c65ccb-02b1-4f15-8278-eb5b029cdcd5"
      },
      "poLinesAdditionalContext": {
        "COLUMNS": "li.bib_id AS bib_id, li.line_item_id AS line_item_id, po_type, li.line_price AS line_price, line_item_loc.location_code AS location_code, line_item_loc.location_id AS location_id, bt.title AS title, trim(bt.issn) AS issn, lics.line_item_status AS line_item_status, li.requestor AS requester, li.vendor_title_num AS vendor_title_num, li.vendor_ref_qual AS vendor_ref_qual, li.vendor_ref_num AS vendor_ref_num, va.account_name AS account_name, fund.fund_code AS fund_code, lin.note AS note",
        "TABLES": "AMDB.purchase_order po, AMDB.line_item li, AMDB.line_item_copy_status lics, AMDB.location line_item_loc, AMDB.bib_text bt, AMDB.vendor_account va, AMDB.line_item_funds lif, AMDB.fund, AMDB.line_item_notes lin",
        "CONDITIONS": "po.po_id = li.po_id AND li.bib_id = bt.bib_id AND li.line_item_id = lics.line_item_id(+) AND li.line_item_id = lin.line_item_id(+) AND lics.location_id = line_item_loc.location_id(+) AND po.account_id = va.account_id(+) AND lics.copy_id = lif.copy_id(+) AND lif.fund_id = fund.fund_id(+) AND lif.ledger_id = fund.ledger_id(+) AND po.po_id = ${PO_ID}"
      },
      "productIdType": "913300b2-03ed-469a-8179-c1092c991227",
      "holdingsNoteTypeId": "0fe80632-2616-4626-a316-c3bba0e3eeb9",
      "defaultLocationId": "480f367b-bf19-4266-b38f-4df0650c94ce"
    },
    {
      "schema": "MSDB",
      "partitions": 2,
      "pageAdditionalContext": {
        "COLUMNS": "po.po_id, po.po_number, po.po_status, po.vendor_id, null as shiploc, null as billloc",
        "TABLES": "MSDB.purchase_order po, MSDB.po_status stat, MSDB.po_type, MSDB.location shipto",
        "CONDITIONS": "po.po_type = po_type.po_type AND po.ship_location = shipto.location_id AND shipto.location_code = 'AcqCleanUp' AND stat.po_status = po.po_status AND po_type_desc = 'Continuation' AND po_status_desc in ('Approved/Sent','Pending','Received Complete') AND po.po_number not in ('1AAA4132','1AAA4766','1AAA5586','1AAF8902')"
      },
      "poNumberPrefix": "msl",
      "includeAddresses": false,
      "references": {
        "vendorTypeId": "b427aa0a-96f2-4338-8b3c-2ddcdca6cfe4",
        "instanceTypeId": "fb6db4f0-e5c3-483b-a1da-3edbb96dc8e8",
        "holdingTypeId": "e7fbdcf5-8fb0-417e-b477-6ee9d6832f12"
      },
      "poLinesAdditionalContext": {
        "COLUMNS": "li.bib_id AS bib_id, li.line_item_id AS line_item_id, po_type, li.line_price AS line_price, mfhdloc.location_code AS location_code, mfhdloc.location_id AS location_id, bt.title_brief AS title, trim(bt.issn) AS issn, lics.line_item_status AS line_item_status, li.requestor AS requester, li.vendor_title_num AS vendor_title_num, li.vendor_ref_qual AS vendor_ref_qual, li.vendor_ref_num AS vendor_ref_num, va.account_name AS account_name, fund.fund_code AS fund_code, null AS note",
        "TABLES": "MSDB.bib_mfhd bm, MSDB.purchase_order po, MSDB.line_item li, MSDB.line_item_copy_status lics, MSDB.location mfhdloc, MSDB.mfhd_master mm, MSDB.bib_text bt, MSDB.vendor_account va, MSDB.line_item_funds lif, MSDB.fund",
        "CONDITIONS": "po.po_id = li.po_id AND li.bib_id = bt.bib_id AND li.line_item_id = lics.line_item_id(+) AND lics.mfhd_id = bm.mfhd_id(+) AND bm.mfhd_id = mm.mfhd_id(+) AND mm.location_id = mfhdloc.location_id(+) AND po.account_id = va.account_id(+) AND lics.copy_id = lif.copy_id(+) AND lif.fund_id = fund.fund_id(+) AND lif.ledger_id = fund.ledger_id(+) AND po.po_id = ${PO_ID}"
      },
      "productIdType": "913300b2-03ed-469a-8179-c1092c991227",
      "holdingsNoteTypeId": "0fe80632-2616-4626-a316-c3bba0e3eeb9",
      "holdingsNoteToElide": "current issues shelved in current journals section",
      "additionalHoldingsNotes": [{
          "id": "7ca7dc63-c053-4aec-8272-c03aeda4840c",
          "note": "Ask the MSL AskUs Desk about latest journal issues",
          "staffOnly": false
      }]
    }
  ],
  "maps": {
    "acqAddresses": {
      "SerArea": "aac86edc-e379-45f9-98da-5885a11f3f72",
      "AcqMoMO": "337e92c0-b1ae-40f4-8d59-3b2430437ea6",
      "AcqMoNY": "d16248ac-2828-4da8-bf12-3d8e06288e8c",
      "AcqMoPC": "24bfa0cc-c097-4000-b96a-24fa9385018a",
      "AcqMoRU": "0dbf0624-f495-461e-a3f2-372fca09af24",
      "AcqMoUR": "600775d6-9aea-427e-a75c-6b7a9342339e",
      "AcqProcess": "3e7c5cc3-ac27-46e7-b81b-b838a38a3158",
      "SR": "a84ab456-028b-4d8b-9e66-22f4591e7e4b",
      "SRDB": "bf6934e7-40e6-4807-a262-31f84c110c64",
      "SRDIR": "6d64426c-dff3-473a-b349-a34dda533433",
      "SRDIRM": "f981f253-cdbf-4022-8b28-f9d00859552f",
      "SRDIRMP": "5439ee2f-f903-437a-b49b-84c3b921c669",
      "SRDIRN": "e4e13666-58cf-491f-8fbf-bd5aa20db019",
      "SRDIRO": "e98b8612-80c5-482b-a680-9d05b762fbef",
      "SRDIRP": "d247e071-a2c8-4d8e-8faa-1b59f0e1387d",
      "SRGFT": "b4bc5bc4-f37b-4b75-a6a4-bfba38e1811d",
      "SRMSV": "73e5ec11-eb68-480e-b66c-7384fd5feee2",
      "SRMSVM": "ca394adf-d362-4cb7-8251-82c7a96df1ad",
      "SRMSVMO": "0e81addc-a3ec-49d3-9973-108fc313db8b",
      "SRMSVO": "fb5ad323-b9e1-487b-bfce-84171254b2e0",
      "SRMSVP": "b34e29a2-bcfc-4388-baa1-36142731688b",
      "SRMSVPM": "72d68939-4cff-4885-a623-c16aa352fa8e",
      "SRMSVW": "8c8616c8-b45b-408d-932e-e98ab528d95e",
      "SRMSVWM": "3e121647-2251-4286-bc25-4e6b40f911e6",
      "SRSOV": "06e9d410-30ed-46ba-89f4-395fa519eb9c",
      "SRSOVM": "b7bc7a7e-b026-485f-bdbf-705d70523291",
      "SRVSVO": "5013287f-1e34-477e-bfe5-6e21298f455b",
      "acq_admin": "68041a19-8c02-449f-ac54-7e8d35bd9575",
      "acq_bind": "ba5359cf-bd9b-441f-b54a-46992dbcff06",
      "west_res": "eb7252b6-b125-4ab8-9b1c-b73b15a32b25"
    },
    "poLineAcqMethods": {
      "0": "Approval Plan",
      "1": "Purchase",
      "2": "Gift",
      "3": "Exchange",
      "4": "Depository",
      "5": "Purchase"
    },
    "poLineReceiptStatus": {
      "0": "Pending",
      "1": "Fully Received",
      "2": "Awaiting Receipt",
      "3": "Cancelled",
      "4": "Awaiting Receipt",
      "5": "Pending",
      "6": "Fully Paid",
      "7": "Cancelled",
      "8": "Awaiting Receipt",
      "9": "Partially Received",
      "10": "Fully Received"
    },
    "location": {
      "AMDB": {
        "36": "media",
        "37": "media,res",
        "47": "ils,borr",
        "48": "ils,lend",
        "132": "blcc,circ",
        "133": "blcc,cpd",
        "134": "blcc,stk",
        "135": "blcc,ref",
        "136": "blcc,res",
        "137": "blcc,rndx",
        "138": "www_evans",
        "182": "media,arcv",
        "185": "blcc,cd",
        "188": "blcc,lan",
        "201": "blcc,stand",
        "210": "blcc,riMSL",
        "217": "blcc,utc",
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
        "135": "kingsvl",
        "141": "kingsvlref",
        "143": "kingsvlrestext",
        "144": "kingsvljrnl",
        "186": "msl_pda"
      }
    },
    "expenseClasses": {
      "AMDB": {
        "access": "012ba30f-1d20-4afb-8029-2d64116e47f0",
        "chargeback": "6dfe92d5-a325-482b-bc78-744bd87920b1",
        "galveston": "2e158b89-4715-4028-8086-af85ca6da0c8",
        "etxtnorm": "0d3c9098-e315-45aa-be46-cf9b642459c2"
      },
      "MSDB": {
        "edbiores": "647c7986-a3ba-40c0-b063-adda5d3d5987",
        "edmedcln": "be541231-61f8-49a0-bafd-8e85e7fe23d3",
        "edmedhum": "be541231-61f8-49a0-bafd-8e85e7fe23d3",
        "edcop": "68d3269f-0d7a-4d49-ae55-ef6381e64b16",
        "edpharm": "68d3269f-0d7a-4d49-ae55-ef6381e64b16",
        "edpubhlt": "8dd52aa3-2182-4fb5-80be-9328018ea6c3",
        "edmedvet": "246f7cda-493e-46bb-83bb-1f411a921caa",
        "ejag": "47551b78-9082-4827-93da-96e487776cf2",
        "ejbiomed": "9507d669-99cb-4c30-b12f-73ddae3b6759",
        "ejbiores": "9507d669-99cb-4c30-b12f-73ddae3b6759",
        "ejmedcln": "f21e409d-03de-43c8-bb42-de42796035b1",
        "ejmedhum": "f21e409d-03de-43c8-bb42-de42796035b1",
        "ejmedimp": "f21e409d-03de-43c8-bb42-de42796035b1",
        "ejmisc": "de02ccbb-afc9-4393-aa09-bbfcf531e3a4",
        "ejnrs": "27ddca39-01d3-4cd7-91fa-4f1b034c9df3",
        "ejnurse": "27ddca39-01d3-4cd7-91fa-4f1b034c9df3",
        "ejpharm": "4e005163-6eb2-46a6-a7d4-b894f3d1b5ee",
        "ejpubhlt": "2e3553cd-e7ab-4a69-8ffb-7ba564b9221c",
        "ejmedvet": "96dc22d8-2870-464e-b059-c216eca711e1",
        "ejvetcln": "96dc22d8-2870-464e-b059-c216eca711e1",
        "ejvetimp": "96dc22d8-2870-464e-b059-c216eca711e1",
        "ebbiores": "58462e8f-7391-4095-a182-840981763bd4",
        "ebmedcln": "f1f1d1af-5078-4b90-8461-58b2a8200775",
        "ebmedimp": "f1f1d1af-5078-4b90-8461-58b2a8200775",
        "emmedhum": "f1f1d1af-5078-4b90-8461-58b2a8200775",
        "ebmedhum": "f1f1d1af-5078-4b90-8461-58b2a8200775",
        "ebnurse": "a67628e0-dd84-4fd8-9508-4041ed9e31e0",
        "ebpharm": "aa6723ba-f4e7-436b-82da-7c12dcf5313d",
        "ebmedvet": "c3798940-c25d-4713-a9ac-b55656924309",
        "ednurse": "c2911bac-0765-4f7d-8316-28b9460eb5b5",
        "emmedvet": "c3798940-c25d-4713-a9ac-b55656924309",
        "pjbiores": "85673c7d-d865-4a52-a6eb-5eea4c9ec9d0",
        "pjmisc": "bf4299b5-ac72-4509-b921-822c3c74ce6c",
        "pjpharm": "8d1d5118-e848-4fee-b254-676afc4d6504",
        "pjpubhlt": "773e4920-f787-4132-a668-613de4ec764d",
        "pjpubjlt": "773e4920-f787-4132-a668-613de4ec764d",
        "pjmedvet": "b8fe686e-5b1d-4d45-a21a-34d9aea5e50c",
        "pjvetcln": "b8fe686e-5b1d-4d45-a21a-34d9aea5e50c",
        "pjvetimp": "b8fe686e-5b1d-4d45-a21a-34d9aea5e50c",
        "pjvetmed": "b8fe686e-5b1d-4d45-a21a-34d9aea5e50c",
        "pmbiores": "b9e6408f-5ec9-4d86-8f6a-734701bc735f",
        "pmmedhum": "ee5ea835-8896-45f4-b4ef-c1e9812c2d24",
        "pmpharm": "d20a2068-089f-42d0-9893-0afdcc56b27a",
        "pmpubhlt": "788fd5a9-f4bc-4b65-813b-2eade0637f4d",
        "pmvetimp": "59090265-788d-4e9d-919e-e5443658708c",
        "pmmedvet": "59090265-788d-4e9d-919e-e5443658708c"
      }
    },
    "fundCodes": {
      "AMDB": {},
      "MSDB": {
        "ed": "msledata",
        "eb": "mslemono",
        "ej": "mslejournals",
        "em": "mslemono",
        "pj": "mslpjournals",
        "pm": "mslpmono",
        "ve": "mslvethist"
      }
    },
    "vendorRefQual": {
      "SNA": "Agent's unique subscription reference number",
      "VN": "Internal vendor number",
      "SCO": "Supplier's continuation order",
      "SLI": "Supplier's unique order line reference number",
      "SNP": "Library's continuation order number"
    }
  },
  "defaults": {
    "aqcAddressCode": "SR",
    "vendorRefQual": "VN"
  }
}
```

## Course Reserve Migration

Use an HTTP POST request with the `X-Okapi-Tenant` HTTP Header set to an appropriate Tenant.

POST to http://localhost:9000/migrate/coursereserves

```
{}
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
