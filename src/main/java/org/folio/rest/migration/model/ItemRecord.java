package org.folio.rest.migration.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Status.Name;
import org.folio.rest.migration.model.request.MfhdItem;

public class ItemRecord {

  private final String itemId;
  private final String barcode;

  private final String enumeration;
  private final String chronology;

  private final int numberOfPieces;
  private final String materialTypeId;
  private final Status status;

  private String permanentLoanTypeId;
  private String temporaryLoanTypeId;

  private String permanentLocationId;
  private String temporaryLocationId;
  
  private String id;
  private String holdingId;

  private String createdByUserId;
  private Date createdDate;

  public ItemRecord(String itemId, String barcode, MfhdItem mfhdItem, int numberOfPieces, String materialTypeId, Name status) {
    this.itemId = itemId;
    this.barcode = barcode;
    this.enumeration = mfhdItem.getItemEnum();
    this.chronology = mfhdItem.getChron();
    this.numberOfPieces = numberOfPieces;
    this.materialTypeId = materialTypeId;
    this.status = new Status();
    this.status.setName(status);
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

  public void setPermanentLoanTypeId(String permanentLoanTypeId) {
    this.permanentLoanTypeId = permanentLoanTypeId;
  }

  public String getTemporaryLoanTypeId() {
    return temporaryLoanTypeId;
  }

  public void setTemporaryLoanTypeId(String temporaryLoanTypeId) {
    this.temporaryLoanTypeId = temporaryLoanTypeId;
  }

  public String getPermanentLocationId() {
    return permanentLocationId;
  }

  public void setPermanentLocationId(String permanentLocationId) {
    this.permanentLocationId = permanentLocationId;
  }

  public String getTemporaryLocationId() {
    return temporaryLocationId;
  }

  public void setTemporaryLocationId(String temporaryLocationId) {
    this.temporaryLocationId = temporaryLocationId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHoldingId() {
    return holdingId;
  }

  public void setHoldingId(String holdingId) {
    this.holdingId = holdingId;
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
    item.setId(id);
    item.setHoldingsRecordId(holdingId);
    item.setPermanentLoanTypeId(permanentLoanTypeId);
    item.setTemporaryLoanTypeId(temporaryLoanTypeId);
    item.setMaterialTypeId(materialTypeId);
    item.setPermanentLocationId(permanentLocationId);
    item.setTemporaryLocationId(temporaryLocationId);
    // item.setEffectiveLocationId(null);

    item.setBarcode(barcode);
    item.setChronology(chronology);
    item.setNumberOfPieces(String.valueOf(numberOfPieces));
    item.setEnumeration(enumeration);
    item.setStatus(status);

    item.setHrid(String.format("%s%011d", hridPrefix, hrid));

    Set<String> formerIds = new HashSet<>();
    formerIds.add(itemId);
    item.setFormerIds(formerIds);

    org.folio.rest.jaxrs.model.Metadata metadata = new org.folio.rest.jaxrs.model.Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    item.setMetadata(metadata);
    return item;
  }

}
