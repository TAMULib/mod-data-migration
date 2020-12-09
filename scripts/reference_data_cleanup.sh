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
select '/groups/' || id from diku_mod_users.groups;
select '/addresstypes/' || id from diku_mod_users.addresstype;
select '/owners/' || id from diku_mod_feesfines.owners;
select '/finance/fund-types/' || id from diku_mod_finance_storage.fund_type where jsonb->>'name' in ('Audio','College/University funds','Exchange','Faculty','Gifts','Grants','Physical','Restricted','State funds','Technical','Unrestricted');
select '/configurations/entries/' || id from diku_mod_configuration.config_data where jsonb->>'configName' in ('poLines-limit');
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
