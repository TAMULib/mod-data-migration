#!/bin/sh

echo "Curl in request to mod-data-migration to import users from DivIT"

curl --location --request POST 'http://mod-data-migration:8081/migrate/divitpatron' \
--header 'Content-Type: application/json' \
--header "X-Okapi-Tenant: $TENANT_ID" \
--data-raw '{
  "database": {
    "url": "jdbc:oracle:thin:@ora-shared.it.tamu.edu:1521:cis",
    "username": "patron",
    "password": "'"$DIVIT_DB_PASSWORD"'",
    "driverClassName": "oracle.jdbc.OracleDriver"
  },
  "preActions": [],
  "postActions": [
    "UPDATE ${TENANT}_mod_users.users SET jsonb = jsonb_set(jsonb, '\''{personal, email}'\'', '\''\"folio_user@library.tamu.edu\"'\'') WHERE jsonb->'\''personal'\''->>'\''email'\'' != '\''folio_user@library.tamu.edu'\''"
  ],
  "parallelism": 1,
  "jobs": [
    {
      "name": "employee",
      "sql": "SELECT DISTINCT nvl2(emp.tamu_netid, emp.tamu_netid, emp.uin) AS username, emp.uin AS externalSystemId, NVL2(pi.id_card_num, pi.id_card_num, emp.uin) AS barcode, '\''true'\'' AS active, '\''fast'\'' AS patronGroup, emp.last_name AS personal_lastName, emp.first_name AS personal_firstName, emp.middle_name AS personal_middleName, emp.tamu_preferred_alias AS personal_email, emp.office_phone AS personal_phone, '\''Permanent'\'' AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, emp.mail_street AS addresses_permanent_addressLine1, NULL AS addresses_permanent_addressLine2, emp.mail_city AS addresses_permanent_city, emp.mail_state AS addresses_permanent_region, emp.mail_zip AS addresses_permanent_postalCode, '\''Temporary'\'' AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine2, emp.adloc_dept_name AS addresses_temporary_addressLine1, emp.work_city AS addresses_temporary_city, emp.work_state AS addresses_temporary_region, emp.work_zip AS addresses_temporary_postalCode, emp.adloc_dept AS departments_0, to_char(sysdate+500, '\''YYYY-MM-DD'\'') AS expirationDate FROM patron.employees_retirees emp, patron.person_identifiers pi WHERE upper(emp.adloc_system_member_name) in('\''TEXAS A'\'' || chr(38) || '\''M AGRILIFE EXTENSION SERVICE'\'', '\''TEXAS A'\'' || chr(38) || '\''M AGRILIFE RESEARCH'\'', '\''TEXAS A'\'' || chr(38) || '\''M ENGINEERING EXPERIMENT STATION'\'', '\''TEXAS A'\'' || chr(38) || '\''M ENGINEERING EXTENSION SERVICE'\'', '\''TEXAS A'\'' || chr(38) || '\''M FOREST SERVICE'\'', '\''TEXAS A'\'' || chr(38) || '\''M HEALTH'\'', '\''TEXAS A'\'' || chr(38) || '\''M SYSTEM OFFICES'\'', '\''TEXAS A'\'' || chr(38) || '\''M SYSTEM SHARED SERVICE CENTER'\'', '\''TEXAS A'\'' || chr(38) || '\''M SYSTEM SPONSORED RESEARCH SERVICES'\'', '\''TEXAS A'\'' || chr(38) || '\''M SYSTEM TECHNOLOGY COMMERCIALIZATION'\'', '\''TEXAS A'\'' || chr(38) || '\''M TRANSPORTATION INSTITUTE'\'', '\''TEXAS A'\'' || chr(38) || '\''M UNIVERSITY'\'', '\''TEXAS A'\'' || chr(38) || '\''M UNIVERSITY AT GALVESTON'\'', '\''TEXAS A'\'' || chr(38) || '\''M VETERINARY MEDICAL DIAGNOSTIC LABORATORY'\'') AND employment_status_name not in('\''Affiliate Non-Employee'\'', '\''Deceased'\'', '\''Terminated'\'') AND emp.employee_type_name != '\''Student'\'' AND emp.employee_type_name IS NOT NULL AND emp.uin = pi.uin(+) AND (emp.last_updated > sysdate-1 OR pi.last_updated > sysdate-1) AND emp.last_name IS NOT NULL"
    },
    {
      "name": "student",
      "sql": "SELECT DISTINCT nvl2(stu.tamu_netid, stu.tamu_netid, stu.uin) AS username, stu.uin AS externalSystemId, NVL2(pi.id_card_num, pi.id_card_num, stu.uin) AS barcode, '\''true'\'' AS active, CASE WHEN substr(stu.classification, 1, 1) in('\''D'\'', '\''G'\'', '\''L'\'', '\''M'\'', '\''P'\'', '\''V'\'') THEN '\''grad'\'' WHEN substr(stu.classification, 1, 1) in ('\''I'\'', '\''U'\'') THEN '\''ungr'\'' END AS patronGroup, stu.last_name AS personal_lastName, stu.first_name AS personal_firstName, stu.middle_name AS personal_middleName, stu.tamu_preferred_alias AS personal_email, stu.local_phone AS personal_phone, '\''Permanent'\'' AS addresses_permanent_addressTypeId, stu.perm_country AS addresses_permanent_countryId, stu.perm_street1 AS addresses_permanent_addressLine1, stu.perm_street2 || '\'' '\'' || stu.perm_street3 AS addresses_permanent_addressLine2, stu.perm_city AS addresses_permanent_city, stu.perm_state AS addresses_permanent_region, stu.perm_zip AS addresses_permanent_postalCode, '\''Temporary'\'' AS addresses_temporary_addressTypeId, stu.local_street1 AS addresses_temporary_addressLine1, stu.local_street2 || '\'' '\'' || stu.local_street3 AS addresses_temporary_addressLine2, stu.local_city AS addresses_temporary_city, stu.local_state AS addresses_temporary_region, stu.local_zip AS addresses_temporary_postalCode, stu.acad_dept AS departments_0, to_char(sysdate+200, '\''YYYY-MM-DD'\'') AS expirationDate FROM patron.students stu, patron.employees_retirees emp, person_identifiers pi WHERE stu.uin = pi.uin(+) AND stu.enroll_status_name in ('\''Enrolled'\'', '\''Not Enrolled'\'') AND stu.uin = emp.uin(+) AND ((emp.employee_type = 1) OR (emp.uin IS NULL) OR (emp.employee_type IS NULL)) AND (stu.last_updated > sysdate-1 OR pi.last_updated > sysdate-1) AND stu.last_name IS NOT NULL"
    },
    {
      "name": "other people",
      "sql": "SELECT DISTINCT nvl2(op.tamu_netid, op.tamu_netid, op.uin) AS username, op.uin AS externalSystemId, NVL2(pi.id_card_num, pi.id_card_num, op.uin) AS barcode, '\''true'\'' AS active, '\''fast'\'' AS patronGroup, op.last_name AS personal_lastName, op.first_name AS personal_firstName, op.middle_name AS personal_middleName, op.tamu_preferred_alias AS personal_email, op.office_phone AS personal_phone, NULL AS addresses_permanent_addressTypeId, NULL AS addresses_permanent_countryId, NULL AS addresses_permanent_addressLine1, NULL AS addresses_permanent_addressLine2, NULL AS addresses_permanent_city, NULL AS addresses_permanent_region, NULL AS addresses_permanent_postalCode, NULL AS addresses_temporary_addressTypeId, NULL AS addresses_temporary_addressLine2, NULL AS addresses_temporary_addressLine1, NULL AS addresses_temporary_city, NULL AS addresses_temporary_region, NULL AS addresses_temporary_postalCode, NULL AS departments_0, to_char(sysdate+200, '\''YYYY-MM-DD'\'') AS expirationDate FROM patron.other_people op, patron.person_identifiers pi, patron.employees_retirees emp, students stu WHERE op.uin = pi.uin(+) AND op.uin = stu.uin(+) AND op.uin = emp.uin(+) AND stu.uin IS NULL AND emp.uin IS NULL AND op.tamu_preferred_alias IS NOT NULL AND((affiliate_role in ('\''affiliate:continuingeducationstudent'\'', '\''affiliate:clinicaltrainee'\'', '\''affiliate:faculty:future'\'', '\''affiliate:graduateassistant:future'\'', '\''affiliate:librarian'\'', '\''affiliate:medicalresident'\'', '\''affiliate:regent'\'', '\''affiliate:staff:future'\'', '\''affiliate:usda'\'', '\''affiliate:veteransprogram'\'', '\''affiliate:visitingscholar'\'', '\''employee:faculty:retired'\'', '\''faculty:adjunct'\'') AND system_member in ('\''01'\'', '\''02'\'', '\''06'\'', '\''07'\'', '\''09'\'', '\''10'\'', '\''11'\'', '\''12'\'', '\''20'\'', '\''23'\'', '\''26'\'', '\''28'\'')) OR (op.data_provider in ('\''HSCAFFILIATES'\'', '\''QATAR'\''))) AND (op.last_updated > sysdate-1 OR pi.last_updated > sysdate-1) AND op.last_name IS NOT NULL"
    }
  ]
}'
