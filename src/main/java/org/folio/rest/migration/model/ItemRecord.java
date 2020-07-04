package org.folio.rest.migration.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Status.Name;

public class ItemRecord {

  private final String itemId;
  private final String barcode;

  private final String enumeration;
  private final String chronology;

  private final int numberOfPieces;
  private final String materialTypeId;

  private Status status;

  private String permanentLoanTypeId;
  private String temporaryLoanTypeId;

  private String permanentLocationId;
  private String temporaryLocationId;
  
  private String id;
  private String holdingId;

  private String createdByUserId;
  private Date createdDate;

  public ItemRecord(String itemId, String barcode, String enumeration, String chronology, int numberOfPieces, String materialTypeId) {
    this.itemId = itemId;
    this.barcode = barcode;
    this.enumeration = enumeration;
    this.chronology = chronology;
    this.numberOfPieces = numberOfPieces;
    this.materialTypeId = materialTypeId;
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

  public void setStatus(Status status) {
    this.status = status;
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
    if (Objects.nonNull(temporaryLoanTypeId)) {
      item.setTemporaryLoanTypeId(temporaryLoanTypeId);
    }
    item.setMaterialTypeId(materialTypeId);
    item.setPermanentLocationId(permanentLocationId);
    if (Objects.nonNull(temporaryLocationId)) {
      item.setTemporaryLocationId(temporaryLocationId);
    }
    // item.setEffectiveLocationId(null);

    // TODO: get year caption
    // item.setYearCaption(null);

    // TODO: get volume
    // item.setVolume(null);

    // TODO: get item damaged status id
    // item.setItemDamagedStatusId(null);

    // TODO: get suppress from discovery
    // item.setDiscoverySuppress(null);

    // TODO: get statistical code ids
    // item.setStatisticalCodeIds(null);

    // TODO: get circulation notes
    // item.setCirculationNotes(null);

    // TODO: get notes
    // item.setNotes(null);

    // TODO: go over extract_item_voyager.pl

    item.setBarcode(barcode);
    item.setChronology(chronology);
    item.setNumberOfPieces(String.valueOf(numberOfPieces));
    item.setEnumeration(enumeration);

    // TODO: get appropriate status
    Status status = new Status();
    status.setName(Name.AVAILABLE);

    // TODO: get status date
    // status.setDate(null);

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
