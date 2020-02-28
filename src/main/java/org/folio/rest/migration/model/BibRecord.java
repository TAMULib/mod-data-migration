package org.folio.rest.migration.model;

import java.util.Date;
import java.util.UUID;

import org.folio.rest.migration.mapping.InstanceMapper;
import org.folio.rest.migration.model.generated.common.ExternalIdsHolder;
import org.folio.rest.migration.model.generated.inventory_storage.Instance;
import org.folio.rest.migration.model.generated.inventory_storage.Metadata;
import org.folio.rest.migration.model.generated.source_record_storage.AdditionalInfo;
import org.folio.rest.migration.model.generated.source_record_storage.ParsedRecord;
import org.folio.rest.migration.model.generated.source_record_storage.RawRecord;
import org.folio.rest.migration.model.generated.source_record_storage.RecordModel;
import org.folio.rest.migration.model.generated.source_record_storage.RecordModel.RecordType;
import org.marc4j.marc.Record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class BibRecord {

  private final String bibId;
  private final Boolean suppressDiscovery;

  private final String rawRecordId;
  private final String parsedRecordId;

  private String marc;
  private String sourceRecordId;
  private String instanceId;

  private Record record;
  private JsonNode marcJson;

  private String createdByUserId;
  private Date createdDate;

  public BibRecord(String bibId, Boolean suppressDiscovery) {
    this.bibId = bibId;
    this.suppressDiscovery = suppressDiscovery;
    this.rawRecordId = UUID.randomUUID().toString();
    this.parsedRecordId = UUID.randomUUID().toString();
  }

  public String getBibId() {
    return bibId;
  }

  public Boolean getSuppressDiscovery() {
    return suppressDiscovery;
  }

  public String getRawRecordId() {
    return rawRecordId;
  }

  public String getParsedRecordId() {
    return parsedRecordId;
  }

  public String getMarc() {
    return marc;
  }

  public void setMarc(String marc) {
    this.marc = marc;
  }

  public String getSourceRecordId() {
    return sourceRecordId;
  }

  public void setSourceRecordId(String sourceRecordId) {
    this.sourceRecordId = sourceRecordId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Record getRecord() {
    return record;
  }

  public void setRecord(Record record) {
    this.record = record;
  }

  public JsonNode getMarcJson() {
    return marcJson;
  }

  public void setMarcJson(JsonNode marcJson) {
    this.marcJson = marcJson;
  }

  public String getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(String createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public RecordModel toRecordModel(String jobExecutionId) {
    final RecordModel recordModel = new RecordModel();
    recordModel.setId(sourceRecordId);
    recordModel.setMatchedId(sourceRecordId);
    recordModel.setParsedRecordId(parsedRecordId);
    recordModel.setRawRecordId(rawRecordId);
    recordModel.setRecordType(RecordType.MARC);
    recordModel.setSnapshotId(jobExecutionId);
    AdditionalInfo additionalInfo = new AdditionalInfo();
    additionalInfo.setSuppressDiscovery(suppressDiscovery);
    recordModel.setAdditionalInfo(additionalInfo);
    ExternalIdsHolder externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setInstanceId(instanceId);
    recordModel.setExternalIdsHolder(externalIdsHolder);
    recordModel.setMetadata(getMetadata());
    return recordModel;
  }

  public RawRecord toRawRecord() {
    final RawRecord rawRecord = new RawRecord();
    rawRecord.setId(rawRecordId);
    rawRecord.setContent(marc);
    return rawRecord;
  }

  public ParsedRecord toParsedRecord() {
    final ParsedRecord parsedRecord = new ParsedRecord();
    parsedRecord.setId(parsedRecordId);
    parsedRecord.setContent(marcJson);
    return parsedRecord;
  }

  public Instance toInstance(InstanceMapper instanceMapper, String hridPrefix, int hrid) throws JsonProcessingException {
    final Instance instance = instanceMapper.getInstance(record);
    instance.setId(instanceId);
    instance.setHrid(String.format("%s%011d", hridPrefix, hrid));
    instance.setMetadata(getMetadata());
    return instance;
  }

  private Metadata getMetadata() {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    return metadata;
  }

}