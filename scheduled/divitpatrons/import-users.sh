#!/bin/sh

echo "Curl in request to mod-data-migration to import users from DivIT"

curl --location --request POST "$DIVIT_MIGRATION_URL" \
--header 'Content-Type: application/json' \
--header "X-Okapi-Tenant: $TENANT_ID" \
--data-raw '{
  "database": {
    "url": "'"$DIVIT_DB_URL"'",
    "username": "'"$DIVIT_DB_USERNAME"'",
    "password": "'"$DIVIT_DB_PASSWORD"'",
    "driverClassName": "'"$DIVIT_DB_DRIVER"'"
  },
  "preActions": [],
  "postActions": [
    "UPDATE ${TENANT}_mod_users.users SET jsonb = jsonb_set(jsonb, '\''{personal, email}'\'', '\''\"folio_user@library.tamu.edu\"'\'') WHERE jsonb->'\''personal'\''->>'\''email'\'' != '\''folio_user@library.tamu.edu'\'' AND jsonb->>'\''username'\'' NOT IN ('\''${TENANT}_admin'\'','\''backup_admin'\'','\''system-user'\'','\''mod-search'\'','\''data-export-system-user'\'','\''pub-sub'\'','\''edgeuser'\'','\''vufind'\'')"
  ],
  "parallelism": 1,
  "jobs": [
    {
      "name": "employee",
      "sql": "SELECT DISTINCT CASE WHEN emp.tamu_netid IS NOT NULL THEN emp.tamu_netid ELSE emp.uin END AS username, emp.uin AS externalSystemId, CASE WHEN pi.id_card_num IS NOT NULL THEN pi.id_card_num ELSE emp.uin END AS barcode, '\''true'\'' AS active, '\''fast'\'' AS patronGroup, emp.last_name AS personal_lastName, emp.first_name AS personal_firstName, emp.middle_name AS personal_middleName, emp.tamu_preferred_alias AS personal_email, emp.office_phone AS personal_phone, '\''Permanent'\'' AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, emp.mail_street AS addresses_permanent_addressLine1, emp.mail_street2 AS addresses_permanent_addressLine2, emp.mail_city AS addresses_permanent_city, emp.mail_state AS addresses_permanent_region, emp.mail_zip AS addresses_permanent_postalCode, '\''Temporary'\'' AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine2, emp.adloc_dept_name AS addresses_temporary_addressLine1, emp.work_city AS addresses_temporary_city, emp.work_state AS addresses_temporary_region, emp.work_zip AS addresses_temporary_postalCode, emp.adloc_dept AS departments_0, format(getdate() + 500, '\''yyyy-MM-dd'\'') AS expirationDate FROM patron.employees_retirees emp LEFT OUTER JOIN patron.person_identifiers pi ON emp.uin = pi.uin WHERE upper(emp.adloc_system_member_name) IN( '\''TEXAS A&M AGRILIFE EXTENSION SERVICE'\'', '\''TEXAS A&M AGRILIFE RESEARCH'\'', '\''TEXAS A&M ENGINEERING EXPERIMENT STATION'\'', '\''TEXAS A&M ENGINEERING EXTENSION SERVICE'\'', '\''TEXAS A&M FOREST SERVICE'\'', '\''TEXAS A&M HEALTH'\'', '\''TEXAS A&M SYSTEM OFFICES'\'', '\''TEXAS A&M SYSTEM SHARED SERVICE CENTER'\'', '\''TEXAS A&M SYSTEM SPONSORED RESEARCH SERVICES'\'', '\''TEXAS A&M SYSTEM TECHNOLOGY COMMERCIALIZATION'\'', '\''TEXAS A&M TRANSPORTATION INSTITUTE'\'', '\''TEXAS A&M UNIVERSITY'\'', '\''TEXAS A&M UNIVERSITY AT GALVESTON'\'', '\''TEXAS A&M VETERINARY MEDICAL DIAGNOSTIC LABORATORY'\'') AND employment_status_name NOT IN ( '\''Affiliate Non-Employee'\'', '\''Deceased'\'', '\''Terminated'\'' ) AND emp.employee_type_name != '\''Student'\'' AND emp.employee_type_name IS NOT NULL AND ( emp.last_updated > getdate() - 1 OR pi.last_updated > getdate() - 1 ) AND emp.last_name IS NOT NULL"
    },
    {
      "name": "student",
      "sql": "SELECT DISTINCT CASE WHEN stu.tamu_netid IS NOT NULL THEN stu.tamu_netid ELSE stu.uin END AS username, stu.uin AS externalSystemId, CASE WHEN pi.id_card_num IS NOT NULL THEN pi.id_card_num ELSE stu.uin END AS barcode, '\''true'\'' AS active, CASE WHEN substring(stu.classification, 1, 1) IN('\''D'\'', '\''G'\'', '\''L'\'', '\''M'\'', '\''P'\'', '\''V'\'') THEN '\''grad'\'' WHEN substring(stu.classification, 1, 1) IN ('\''I'\'', '\''U'\'') THEN '\''ungr'\'' END AS patronGroup, stu.last_name AS personal_lastName, stu.first_name AS personal_firstName, stu.middle_name AS personal_middleName, stu.tamu_preferred_alias AS personal_email, stu.local_phone AS personal_phone, '\''Permanent'\'' AS addresses_permanent_addressTypeId, stu.perm_country AS addresses_permanent_countryId, stu.perm_street1 AS addresses_permanent_addressLine1, stu.perm_street2 + '\'' '\'' + stu.perm_street3 AS addresses_permanent_addressLine2, stu.perm_city AS addresses_permanent_city, stu.perm_state AS addresses_permanent_region, stu.perm_zip AS addresses_permanent_postalCode, '\''Temporary'\'' AS addresses_temporary_addressTypeId, stu.local_street1 AS addresses_temporary_addressLine1, stu.local_street2 + '\'' '\'' + stu.local_street3 AS addresses_temporary_addressLine2, stu.local_city AS addresses_temporary_city, stu.local_state AS addresses_temporary_region, stu.local_zip AS addresses_temporary_postalCode, stu.acad_dept AS departments_0, format(getdate() + 200, '\''yyyy-MM-dd'\'') AS expirationDate FROM patron.students stu LEFT OUTER JOIN patron.employees_retirees emp ON stu.uin = emp.uin LEFT OUTER JOIN patron.person_identifiers pi ON stu.uin = pi.uin WHERE stu.enroll_status_name IN ('\''Enrolled'\'', '\''Not Enrolled'\'') AND ( emp.employee_type = 1 OR emp.uin IS NULL OR emp.employee_type IS NULL) AND ( stu.last_updated > getdate() - 1 OR pi.last_updated > getdate() - 1 ) AND stu.last_name IS NOT NULL"
    },
    {
      "name": "other people",
      "sql": "SELECT DISTINCT CASE WHEN op.tamu_netid IS NOT NULL THEN op.tamu_netid ELSE op.uin END AS username, op.uin AS externalSystemId, CASE WHEN pi.id_card_num IS NOT NULL THEN pi.id_card_num ELSE op.uin END AS barcode, '\''true'\'' AS active, '\''fast'\'' AS patronGroup, op.last_name AS personal_lastName, op.first_name AS personal_firstName, op.middle_name AS personal_middleName, op.tamu_preferred_alias AS personal_email, op.office_phone AS personal_phone, NULL AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, NULL AS addresses_permanent_addressLine1, NULL AS addresses_permanent_addressLine2, NULL AS addresses_permanent_city, NULL AS addresses_permanent_region, NULL AS addresses_permanent_postalCode, NULL AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine2, NULL AS addresses_temporary_addressLine1, NULL AS addresses_temporary_city, NULL AS addresses_temporary_region, NULL AS addresses_temporary_postalCode, NULL AS departments_0, format(getdate() + 200, '\''yyyy-MM-dd'\'') AS expirationDate FROM patron.other_people op LEFT OUTER JOIN patron.person_identifiers pi ON op.uin = pi.uin LEFT OUTER JOIN patron.students stu ON op.uin = stu.uin LEFT OUTER JOIN patron.employees_retirees emp ON op.uin = emp.uin WHERE stu.uin IS NULL AND emp.uin IS NULL AND op.tamu_preferred_alias IS NOT NULL AND( ( affiliate_role IN ( '\''affiliate:continuingeducationstudent'\'', '\''affiliate:clinicaltrainee'\'', '\''affiliate:faculty:future'\'', '\''affiliate:graduateassistant:future'\'', '\''affiliate:librarian'\'', '\''affiliate:medicalresident'\'', '\''affiliate:regent'\'', '\''affiliate:staff:future'\'', '\''affiliate:usda'\'', '\''affiliate:veteransprogram'\'', '\''affiliate:visitingscholar'\'', '\''employee:faculty:retired'\'', '\''faculty:adjunct'\'') AND system_member IN ( '\''01'\'', '\''02'\'', '\''06'\'', '\''07'\'', '\''09'\'', '\''10'\'', '\''11'\'', '\''12'\'', '\''20'\'', '\''23'\'', '\''26'\'', '\''28'\'' ) ) OR (op.data_provider IN ('\''HSCAFFILIATES'\'', '\''QATAR'\'')) ) AND ( op.last_updated > getdate() - 1 OR pi.last_updated > getdate() - 1 ) AND op.last_name IS NOT NULL"
    }
  ],
  "defaults": {
    "preferredContactType": "002",
    "temporaryEmail": "folio_user@library.tamu.edu"
  }
}'
