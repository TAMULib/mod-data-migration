#!/bin/bash

okapi=${OKAPI:-http://localhost:9130}

tenant=${TENANT:-diku}

username=${USERNAME:-diku_admin}
password=${PASSWORD:-admin}

db_host=${DB_HOST:-localhost}
db_port=${DB_PORT:-5432}
db_name=${DB_NAME:-okapi_modules}
db_user=${DB_USER:-folio_admin}
db_password=${DB_PASSWORD:-folio_admin}

echo "Okapu URL: $okapi"
echo "Tenant: $tenant"
echo "Username: $username"
echo "Password: ***"
echo "Database Host: $db_host"
echo "Database Port: $db_port"
echo "Database Name: $db_name"
echo "Database Username: $db_user"
echo "Database Password: ***"

# get psql absolute path
psql=$(which psql)

echo "PSQL: $psql"

# login for token
curl "$okapi/authn/login" -H "x-okapi-tenant: $tenant" -H "Content-Type: application/json" --data "{\"username\":\"$username\",\"password\":\"$password\"}" -s -D login-headers.tmp -o /dev/null
token_header=$(cat login-headers.tmp | grep x-okapi-token)

echo "$token_header"

read -r -d '' statements << EOM
select '/acquisitions-units/memberships/' || id from diku_mod_orders_storage.acquisitions_unit_membership;
select '/acquisitions-units/units/' || id from diku_mod_orders_storage.acquisitions_unit;
select '/loan-policy-storage/loan-policies/' || id from diku_mod_circulation_storage.loan_policy;
select '/overdue-fines-policies/' || id from diku_mod_feesfines.overdue_fine_policy;
select '/lost-item-fees-policies/' || id from diku_mod_feesfines.lost_item_fee_policy;
select '/patron-notice-policy-storage/patron-notice-policies/' || id from diku_mod_circulation_storage.patron_notice_policy;
select '/request-policy-storage/request-policies/' || id from diku_mod_circulation_storage.request_policy;
select '/coursereserves/terms/' || id from diku_mod_courses.coursereserves_terms;
select '/coursereserves/coursetypes/' || id from diku_mod_courses.coursereserves_coursetypes;
select '/coursereserves/departments/' || id from diku_mod_courses.coursereserves_departments;
select '/loan-types/' || id from diku_mod_inventory_storage.loan_type;
select '/material-types/' || id from diku_mod_inventory_storage.material_type;
select '/holdings-types/' || id from diku_mod_inventory_storage.holdings_type where jsonb->>'name' in ('Electronic','Physical');
select '/statistical-codes/' || id from diku_mod_inventory_storage.statistical_code;
select '/statistical-code-types/' || id from diku_mod_inventory_storage.statistical_code_type;
select '/call-number-types/' || id from diku_mod_inventory_storage.call_number_type where jsonb->>'name' in ('UDC','MOYS','LC Modified');
select '/batch-groups/' || id from diku_mod_invoice_storage.batch_groups where jsonb->>'name' in ('Amherst (AC)');
select '/organizations-storage/categories/' || id from diku_mod_organizations_storage.categories;
select '/locations/' || id from diku_mod_inventory_storage.location;
select '/location-units/libraries/' || id from diku_mod_inventory_storage.loclibrary;
select '/location-units/campuses/' || id from diku_mod_inventory_storage.loccampus;
select '/location-units/institutions/' || id from diku_mod_inventory_storage.locinstitution;
select '/service-points/' || id from diku_mod_inventory_storage.service_point;
select '/groups/' || id from diku_mod_users.groups where jsonb->>'group' != 'admin';
select '/addresstypes/' || id from diku_mod_users.addresstype;
select '/owners/' || id from diku_mod_feesfines.owners;
select '/configurations/entries/' || id from diku_mod_configuration.config_data;
select '/finance-storage/expense-classes/' || id from diku_mod_finance_storage.expense_class;
select '/finance/budgets/' || id from diku_mod_finance_storage.budget;
select '/finance/funds/' || id from diku_mod_finance_storage.fund;
select '/finance/groups/' || id from diku_mod_finance_storage.groups;
select '/finance/ledgers/' || id from diku_mod_finance_storage.ledger;
select '/finance/fiscal-years/' || id from diku_mod_finance_storage.fiscal_year;
select '/finance/fund-types/' || id from diku_mod_finance_storage.fund_type;
select '/staff-slips-storage/staff-slips/' || id from diku_mod_circulation_storage.staff_slips;
select '/data-import-profiles/jobProfiles/' || id from diku_mod_data_import_converter_storage.job_profiles where id not in ('d0ebb7b0-2f0f-11eb-adc1-0242ac120002', '91f9b8d6-d80e-4727-9783-73fb53e3c786');
select '/data-import-profiles/actionProfiles/' || id from diku_mod_data_import_converter_storage.action_profiles where id not in ('d0ebba8a-2f0f-11eb-adc1-0242ac120002', 'cddff0e1-233c-47ba-8be5-553c632709d9', '6aa8e98b-0d9f-41dd-b26f-15658d07eb52', 'fa45f3ec-9b83-11eb-a8b3-0242ac130003', 'f8e58651-f651-485d-aead-d2fa8700e2d1');
select '/data-import-profiles/matchProfiles/' || id from diku_mod_data_import_converter_storage.match_profiles where id not in ('d27d71ce-8a1e-44c6-acea-96961b5592c6', '31dbb554-0826-48ec-a0a4-3c55293d4dee');
select '/data-import-profiles/mappingProfiles/' || id from diku_mod_data_import_converter_storage.mapping_profiles where id not in ('d0ebbc2e-2f0f-11eb-adc1-0242ac120002', '862000b9-84ea-4cae-a223-5fc0552f2b42', 'f90864ef-8030-480f-a43f-8cdd21233252', 'bf7b3b86-9b84-11eb-a8b3-0242ac130003', '991c0300-44a6-47e3-8ea2-b01bb56a38cc');
select '/copycat/profiles/' || id from diku_mod_copycat.profile;
EOM

# iterate select statements
echo "$statements" | while IFS= read -r query ; do
  # execute select query
  result=$(PGPASSWORD=$db_password $psql --no-psqlrc -h $db_host -p $db_port -U $db_user -d $db_name -t -c "${query/diku/$tenant}")
  # iterate query results
  echo "$result" | while IFS= read -r path ; do
    if [ -n "$path" ]; then
      url="$(echo $okapi${path/ /})"
      echo "$url"
      # issue delete request
      curl -X DELETE "$url" -H "x-okapi-tenant: $tenant" -H "$token_header"
      echo
    fi
  done
done
