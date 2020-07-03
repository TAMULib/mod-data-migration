package org.folio.rest.migration.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.folio.rest.jaxrs.model.Holdingsrecord;
import org.folio.rest.migration.mapping.HoldingMapper;

import io.vertx.core.json.JsonObject;

public class HoldingRecord {

  private final String mfhdId;
  private final String locationId;

  private final Boolean discoverySuppress;

  private final String callNumber;
  private final String callNumberType;
  private final String holdingsType;
  private final String receiptStatus;
  private final String acquisitionMethod;
  private final String retentionPolicy;

  private JsonObject parsedRecord;

  private String holdingId;
  private String instanceId;

  private String createdByUserId;
  private Date createdDate;

  public HoldingRecord(String mfhdId, String locationId, Boolean discoverySuppress, String callNumber, String callNumberType, String holdingsType, String receiptStatus, String acquisitionMethod, String retentionPolicy) {
    this.mfhdId = mfhdId;
    this.locationId = locationId;
    this.discoverySuppress = discoverySuppress;
    this.callNumber = callNumber;
    this.callNumberType = callNumberType;
    this.holdingsType = holdingsType;
    this.receiptStatus = receiptStatus;
    this.acquisitionMethod = acquisitionMethod;
    this.retentionPolicy = retentionPolicy;
  }

  public String getMfhdId() {
    return mfhdId;
  }

  public String getLocationId() {
    return locationId;
  }

  public Boolean getDiscoverySuppress() {
    return discoverySuppress;
  }

  public String getCallNumber() {
    return callNumber;
  }

  public String getCallNumberType() {
    return callNumberType;
  }

  public String getHoldingsType() {
    return holdingsType;
  }

  public String getReceiptStatus() {
    return receiptStatus;
  }

  public String getAcquisitionMethod() {
    return acquisitionMethod;
  }

  public String getRetentionPolicy() {
    return retentionPolicy;
  }

  public JsonObject getParsedRecord() {
    return parsedRecord;
  }

  public void setParsedRecord(JsonObject parsedRecord) {
    this.parsedRecord = parsedRecord;
  }

  public String getHoldingId() {
    return holdingId;
  }

  public void setHoldingId(String holdingId) {
    this.holdingId = holdingId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
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

  public Holdingsrecord toHolding(HoldingMapper holdingMapper, String hridPrefix, int hrid) throws JsonProcessingException {
    final Holdingsrecord holding = holdingMapper.getHolding(parsedRecord);
    if (Objects.nonNull(holding)) {
      holding.setId(holdingId);
      holding.setInstanceId(instanceId);
      holding.setPermanentLocationId(locationId);
      // holding.setTemporaryLocationId(null);
      holding.setHoldingsTypeId(holdingsType);
      holding.setCallNumberTypeId(callNumberType);
      // holding.setIllPolicyId(null);

      holding.setDiscoverySuppress(discoverySuppress);
      holding.setCallNumber(callNumber);
      holding.setReceiptStatus(receiptStatus);
      holding.setAcquisitionMethod(acquisitionMethod);
      holding.setRetentionPolicy(retentionPolicy);

      holding.setHrid(String.format("%s%011d", hridPrefix, hrid));

      Set<String> formerIds = new HashSet<>();
      formerIds.add(mfhdId);
      holding.setFormerIds(formerIds);

      org.folio.rest.jaxrs.model.Metadata metadata = new org.folio.rest.jaxrs.model.Metadata();
      metadata.setCreatedByUserId(createdByUserId);
      metadata.setCreatedDate(createdDate);
      holding.setMetadata(metadata);
    }
    return holding;
  }

}