package org.folio.rest.migration.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.folio.Instance;
import org.folio.Metadata;
import org.folio.rest.jaxrs.model.common.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.dto.AdditionalInfo;
import org.folio.rest.jaxrs.model.dto.ParsedRecord;
import org.folio.rest.jaxrs.model.dto.ParsedRecordDto.RecordType;
import org.folio.rest.jaxrs.model.dto.RawRecord;
import org.folio.rest.jaxrs.model.mod_source_record_storage.RecordModel;
import org.folio.rest.migration.mapping.InstanceMapper;

import io.vertx.core.json.JsonObject;

public class BibRecord {

  private final String bibId;
  private final Boolean suppressDiscovery;

  private final String rawRecordId;
  private final String parsedRecordId;

  private String marc;
  private String sourceRecordId;
  private String instanceId;

  private JsonObject parsedRecord;

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

  public JsonObject getParsedRecord() {
    return parsedRecord;
  }

  public void setParsedRecord(JsonObject parsedRecord) {
    this.parsedRecord = parsedRecord;
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
    recordModel.setSnapshotId(jobExecutionId);
    recordModel.setMatchedId(sourceRecordId);
    recordModel.setRecordType(RecordType.MARC);
    recordModel.setRawRecordId(rawRecordId);
    recordModel.setParsedRecordId(parsedRecordId);
    ExternalIdsHolder externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setInstanceId(instanceId);
    recordModel.setExternalIdsHolder(externalIdsHolder);
    AdditionalInfo additionalInfo = new AdditionalInfo();
    additionalInfo.setSuppressDiscovery(suppressDiscovery);
    recordModel.setAdditionalInfo(additionalInfo);
    org.folio.rest.jaxrs.model.dto.Metadata metadata = new org.folio.rest.jaxrs.model.dto.Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    recordModel.setMetadata(metadata);
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
    parsedRecord.setContent(this.parsedRecord.getMap());
    return parsedRecord;
  }

  public Instance toInstance(InstanceMapper instanceMapper, String hridPrefix, int hrid, List<String> statisticalCodes) throws JsonProcessingException {
    final Instance instance = instanceMapper.getInstance(parsedRecord);
    instance.setId(instanceId);
    instance.setHrid(String.format("%s%011d", hridPrefix, hrid));
    instance.setDiscoverySuppress(suppressDiscovery);
    if (statisticalCodes.size() > 0) {
      instance.setStatisticalCodeIds(statisticalCodes);
    }
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    instance.setMetadata(metadata);
    return instance;
  }

}