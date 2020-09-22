package org.folio.rest.migration.model;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.folio.rest.jaxrs.model.dataimport.common.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.dataimport.dto.AdditionalInfo;
import org.folio.rest.jaxrs.model.dataimport.dto.ParsedRecord;
import org.folio.rest.jaxrs.model.dataimport.dto.ParsedRecordDto.RecordType;
import org.folio.rest.jaxrs.model.dataimport.dto.RawRecord;
import org.folio.rest.jaxrs.model.dataimport.mod_source_record_storage.RecordModel;
import org.folio.rest.jaxrs.model.inventory.Instance;
import org.folio.rest.jaxrs.model.inventory.Metadata;
import org.folio.rest.migration.mapping.InstanceMapper;

import io.vertx.core.json.JsonObject;

public class BibRecord {

  private final String bibId;
  private final String statusId;
  private final Boolean suppressDiscovery;
  private final Set<String> statisticalCodes;

  private String marc;
  private String sourceRecordId;
  private String instanceId;

  private JsonObject parsedRecord;

  private JsonObject originalParsedRecord;

  private String createdByUserId;
  private Date createdDate;

  public BibRecord(String bibId, String statusId, Boolean suppressDiscovery, Set<String> statisticalCodes) {
    this.bibId = bibId;
    this.statusId = statusId;
    this.suppressDiscovery = suppressDiscovery;
    this.statisticalCodes = statisticalCodes;
  }

  public String getBibId() {
    return bibId;
  }

  public Boolean getSuppressDiscovery() {
    return suppressDiscovery;
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

  public JsonObject getOriginalParsedRecord() {
    return originalParsedRecord;
  }

  public void setOriginalParsedRecord(JsonObject originalParsedRecord) {
    this.originalParsedRecord = originalParsedRecord;
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

  public RecordModel toRecordModel(String jobExecutionId, int order) {
    final RecordModel recordModel = new RecordModel();
    recordModel.setId(sourceRecordId);
    recordModel.setSnapshotId(jobExecutionId);
    recordModel.setMatchedId(sourceRecordId);
    recordModel.setRecordType(RecordType.MARC);
    recordModel.setRawRecordId(sourceRecordId);
    recordModel.setParsedRecordId(sourceRecordId);
    recordModel.setOrder(order);
    ExternalIdsHolder externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setInstanceId(instanceId);
    recordModel.setExternalIdsHolder(externalIdsHolder);
    AdditionalInfo additionalInfo = new AdditionalInfo();
    additionalInfo.setSuppressDiscovery(suppressDiscovery);
    recordModel.setAdditionalInfo(additionalInfo);
    org.folio.rest.jaxrs.model.dataimport.dto.Metadata metadata = new org.folio.rest.jaxrs.model.dataimport.dto.Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    recordModel.setMetadata(metadata);
    return recordModel;
  }

  public RawRecord toRawRecord() {
    final RawRecord rawRecord = new RawRecord();
    rawRecord.setId(sourceRecordId);
    rawRecord.setContent(marc);
    return rawRecord;
  }

  public ParsedRecord toParsedRecord() {
    final ParsedRecord parsedRecord = new ParsedRecord();
    parsedRecord.setId(sourceRecordId);
    parsedRecord.setContent(this.parsedRecord.getMap());
    return parsedRecord;
  }

  public Instance toInstance(InstanceMapper instanceMapper, String hridString) {
    final Instance instance = instanceMapper.getInstance(originalParsedRecord);
    if (Objects.nonNull(instance)) {
      instance.setId(instanceId);
      instance.setDiscoverySuppress(suppressDiscovery);
      instance.setStatisticalCodeIds(statisticalCodes);
      instance.setStatusId(statusId);

      instance.setHrid(hridString);

      Metadata metadata = new Metadata();
      metadata.setCreatedByUserId(createdByUserId);
      metadata.setCreatedDate(createdDate);
      instance.setMetadata(metadata);
    }
    return instance;
  }

}
