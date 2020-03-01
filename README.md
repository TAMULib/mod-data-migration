# mod-data-migration

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Additional information

## MARC Bibliographic migration

POST to http://localhost:9003/migrate/bibs

```
{
  "extraction": {
    "count": "SELECT COUNT(*) AS total FROM ${SCHEMA}.bib_master",
    "page": "SELECT bib_id, suppress_in_opac FROM ${SCHEMA}.bib_master ORDER BY bib_id OFFSET ${OFFSET} ROWS FETCH NEXT ${LIMIT} ROWS ONLY",
    "additional": "SELECT bib_id, seqnum, record_segment FROM ${SCHEMA}.bib_data WHERE bib_id = ${BIB_ID}"
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
  ],
  "preActions": [
    "ALTER TABLE tern_mod_inventory_storage.instance DISABLE TRIGGER ALL",
    "DROP INDEX tern_mod_inventory_storage.instance_instancetypeid_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_modeofissuanceid_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_instancestatusid_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_identifiers_idx_ft",
    "DROP INDEX tern_mod_inventory_storage.instance_contributors_idx_ft",
    "DROP INDEX tern_mod_inventory_storage.instance_languages_idx_ft",
    "DROP INDEX tern_mod_inventory_storage.instance_title_idx_ft",
    "DROP INDEX tern_mod_inventory_storage.instance_keyword_idx_ft",
    "DROP INDEX tern_mod_inventory_storage.instance_subjects_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_classifications_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_identifiers_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_contributors_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_indextitle_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_title_idx_gin",
    "DROP INDEX tern_mod_inventory_storage.instance_publication_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_contributors_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_statisticalcodeids_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_title_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_indextitle_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_source_idx",
    "DROP INDEX tern_mod_inventory_storage.instance_hrid_idx_unique",
    "ALTER TABLE tern_mod_source_record_storage.records DISABLE TRIGGER ALL",
    "DROP INDEX tern_mod_source_record_storage.records_snapshot_id_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.records_externalidsholder_instanceid_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.records_parsed_record_id_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.records_raw_record_id_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.records_id_idx_unique",
    "DROP INDEX tern_mod_source_record_storage.records_id_idx",
    "ALTER TABLE tern_mod_source_record_storage.marc_records DISABLE TRIGGER ALL",
    "DROP INDEX tern_mod_source_record_storage.marc_records_id_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.marc_records_id_idx",
    "DROP INDEX tern_mod_source_record_storage.marc_records_id_idx_unique",
    "ALTER TABLE tern_mod_source_record_storage.raw_records DISABLE TRIGGER ALL",
    "DROP INDEX tern_mod_source_record_storage.raw_records_id_idx_btree",
    "DROP INDEX tern_mod_source_record_storage.raw_records_id_idx",
    "DROP INDEX tern_mod_source_record_storage.raw_records_id_idx_unique"
  ],
  "postActions": [
    "ALTER TABLE tern_mod_inventory_storage.instance ENABLE TRIGGER ALL",
    "CREATE INDEX instance_instancetypeid_idx ON tern_mod_inventory_storage.instance USING btree (instancetypeid)",
    "CREATE INDEX instance_modeofissuanceid_idx ON tern_mod_inventory_storage.instance USING btree (modeofissuanceid)",
    "CREATE INDEX instance_instancestatusid_idx ON tern_mod_inventory_storage.instance USING btree (instancestatusid)",
    "CREATE INDEX instance_identifiers_idx_ft ON tern_mod_inventory_storage.instance USING gin (to_tsvector('simple'::regconfig, f_unaccent((jsonb ->> 'identifiers'::text))))",
    "CREATE INDEX instance_contributors_idx_ft ON tern_mod_inventory_storage.instance USING gin (to_tsvector('simple'::regconfig, f_unaccent((jsonb ->> 'contributors'::text))))",
    "CREATE INDEX instance_languages_idx_ft ON tern_mod_inventory_storage.instance USING gin (to_tsvector('simple'::regconfig, f_unaccent((jsonb ->> 'languages'::text))))",
    "CREATE INDEX instance_title_idx_ft ON tern_mod_inventory_storage.instance USING gin (to_tsvector('simple'::regconfig, f_unaccent((jsonb ->> 'title'::text))))",
    "CREATE INDEX instance_keyword_idx_ft ON tern_mod_inventory_storage.instance USING gin (to_tsvector('simple'::regconfig, f_unaccent(concat_space_sql(VARIADIC ARRAY[(jsonb ->> 'title'::text), concat_array_object_values((jsonb -> 'contributors'::text), 'name'::text), concat_array_object_values((jsonb -> 'identifiers'::text), 'value'::text)]))))",
    "CREATE INDEX instance_subjects_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'subjects'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_classifications_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'classifications'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_identifiers_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'identifiers'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_contributors_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'contributors'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_indextitle_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'indexTitle'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_title_idx_gin ON tern_mod_inventory_storage.instance USING gin (lower(f_unaccent((jsonb ->> 'title'::text))) gin_trgm_ops)",
    "CREATE INDEX instance_publication_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower(f_unaccent((jsonb ->> 'publication'::text))), 600))",
    "CREATE INDEX instance_contributors_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower(f_unaccent((jsonb ->> 'contributors'::text))), 600))",
    "CREATE INDEX instance_statisticalcodeids_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower((jsonb ->> 'statisticalCodeIds'::text)), 600))",
    "CREATE INDEX instance_title_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower(f_unaccent((jsonb ->> 'title'::text))), 600))",
    "CREATE INDEX instance_indextitle_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower(f_unaccent((jsonb ->> 'indexTitle'::text))), 600))",
    "CREATE INDEX instance_source_idx ON tern_mod_inventory_storage.instance USING btree (\"left\"(lower((jsonb ->> 'source'::text)), 600))",
    "CREATE UNIQUE INDEX instance_hrid_idx_unique ON tern_mod_inventory_storage.instance USING btree (lower(f_unaccent((jsonb ->> 'hrid'::text))))",
    "ALTER TABLE tern_mod_source_record_storage.records ENABLE TRIGGER ALL",
    "CREATE INDEX records_snapshot_id_idx_btree ON tern_mod_source_record_storage.records USING btree (((jsonb ->> 'snapshotId'::text)))",
    "CREATE INDEX records_externalidsholder_instanceid_idx_btree ON tern_mod_source_record_storage.records USING btree ((((jsonb -> 'externalIdsHolder'::text) ->> 'instanceId'::text)))",
    "CREATE INDEX records_parsed_record_id_idx_btree ON tern_mod_source_record_storage.records USING btree (((jsonb ->> 'parsedRecordId'::text)))",
    "CREATE INDEX records_raw_record_id_idx_btree ON tern_mod_source_record_storage.records USING btree (((jsonb ->> 'rawRecordId'::text)))",
    "CREATE INDEX records_id_idx ON tern_mod_source_record_storage.records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "CREATE UNIQUE INDEX records_id_idx_unique ON tern_mod_source_record_storage.records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "ALTER TABLE tern_mod_source_record_storage.marc_records ENABLE TRIGGER ALL",
    "CREATE INDEX marc_records_id_idx_btree ON tern_mod_source_record_storage.marc_records USING btree (((jsonb ->> 'id'::text)))",
    "CREATE INDEX marc_records_id_idx ON tern_mod_source_record_storage.marc_records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "CREATE UNIQUE INDEX marc_records_id_idx_unique ON tern_mod_source_record_storage.marc_records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "ALTER TABLE tern_mod_source_record_storage.raw_records ENABLE TRIGGER ALL",
    "CREATE INDEX raw_records_id_idx_btree ON tern_mod_source_record_storage.raw_records USING btree (((jsonb ->> 'id'::text)))",
    "CREATE INDEX raw_records_id_idx ON tern_mod_source_record_storage.raw_records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "CREATE UNIQUE INDEX raw_records_id_idx_unique ON tern_mod_source_record_storage.raw_records USING btree (lower(f_unaccent((jsonb ->> 'id'::text))))",
    "REINDEX TABLE tern_mod_source_record_storage.raw_records",
    "REINDEX TABLE tern_mod_source_record_storage.marc_records",
    "REINDEX TABLE tern_mod_source_record_storage.records",
    "REINDEX TABLE tern_mod_inventory_storage.instance"
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
