package org.folio.rest.migration.model;

import java.util.Date;

import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Status.Name;
import org.folio.rest.migration.model.request.MfhdItem;

public class ItemRecord {

  private final String itemId;
  private final String barcode;

  private final String enumeration;
  private final String chronology;

  private final String permanentLocationId;
  private final String[] formerIds;
  private final int numberOfPieces;
  private final String materialTypeId;
  private final Status status;
  private final String permanentLoanTypeId;
  private final String temporaryLoanTypeId;
  private final String temporaryLocationId;

  private String holdingId;
  private String instanceId;

  private String createdByUserId;
  private Date createdDate;

  public ItemRecord(String itemId, String barcode, MfhdItem mfhdItem, String permanentLocationId, String formerId, int numberOfPieces, String materialTypeId, Name status, String permanentLoanTypeId, String temporaryLoanTypeId, String temporaryLocationId) {
    this.itemId = itemId;
    this.barcode = barcode;
    this.enumeration = mfhdItem.getItemEnum();
    this.chronology = mfhdItem.getChron();
    this.permanentLocationId = permanentLocationId;
    this.formerIds = new String[] { formerId };
    this.numberOfPieces = numberOfPieces;
    this.materialTypeId = materialTypeId;
    this.status = new Status();
    this.status.setName(status);
    this.permanentLoanTypeId = permanentLoanTypeId;
    this.temporaryLoanTypeId = temporaryLoanTypeId;
    this.temporaryLocationId = temporaryLocationId;
  }

  public String getItemId() {
    return itemId;
  }

  public String getBarcode() {
    return barcode;
  }

  public String getEnumeration() {
    return enumeration;
  }

  public String getChronology() {
    return chronology;
  }

  public String getPermanentLocationId() {
    return permanentLocationId;
  }

  public String[] getFormerIds() {
    return formerIds;
  }

  public int getNumberOfPieces() {
    return numberOfPieces;
  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public Status getStatus() {
    return status;
  }

  public String getPermanentLoanTypeId() {
    return permanentLoanTypeId;
  }

  public String getTemporaryLoanTypeId() {
    return temporaryLoanTypeId;
  }

  public String getTemporaryLocationId() {
    return temporaryLocationId;
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

  public Item toItem(String hridPrefix, int hrid) {
    final Item item = new Item();
    item.setId(itemId);
    org.folio.rest.jaxrs.model.Metadata metadata = new org.folio.rest.jaxrs.model.Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    item.setMetadata(metadata);
    return item;
  }

}
