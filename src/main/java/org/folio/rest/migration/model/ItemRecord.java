package org.folio.rest.migration.model;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.folio.rest.jaxrs.model.CirculationNote;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Materialtypes;
import org.folio.rest.jaxrs.model.Note__1;
import org.folio.rest.jaxrs.model.Statisticalcode;
import org.folio.rest.jaxrs.model.Statisticalcodes;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Status.Name;

public class ItemRecord {

  private final String itemId;

  private final int numberOfPieces;
  private final String spineLabel;
  
  private final String itemNoteTypeId;
  private final String itemDamagedStatusId;

  private String id;
  private String holdingId;

  private String barcode;

  private String materialTypeId;

  private ItemMfhdRecord mfhdItem;

  private List<ItemStatusRecord> statuses;

  private List<Note__1> itemNotes;
  private List<CirculationNote> circulationNotes;

  private String permanentLoanTypeId;
  private String temporaryLoanTypeId;

  private String permanentLocationId;
  private String temporaryLocationId;

  private String createdByUserId;
  private Date createdDate;

  public ItemRecord(String itemId, int numberOfPieces, String spineLabel, String itemNoteTypeId, String itemDamagedStatusId) {
    this.itemId = itemId;
    this.numberOfPieces = numberOfPieces;
    this.spineLabel = spineLabel;
    this.itemNoteTypeId = itemNoteTypeId;
    this.itemDamagedStatusId = itemDamagedStatusId;
  }

  public String getItemId() {
    return itemId;
  }

  public int getNumberOfPieces() {
    return numberOfPieces;
  }

  public String getSpineLabel() {
    return spineLabel;
  }

  public String getItemNoteTypeId() {
    return itemNoteTypeId;
  }

  public String getItemDamagedStatusId() {
    return itemDamagedStatusId;
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

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(String materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

  public ItemMfhdRecord getMfhdItem() {
    return mfhdItem;
  }

  public void setMfhdItem(ItemMfhdRecord mfhdItem) {
    this.mfhdItem = mfhdItem;
  }

  public List<ItemStatusRecord> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<ItemStatusRecord> statuses) {
    this.statuses = statuses;
  }

  public List<Note__1> getItemNotes() {
    return itemNotes;
  }

  public void setItemNotes(List<Note__1> itemNotes) {
    this.itemNotes = itemNotes;
  }

  public List<CirculationNote> getCirculationNotes() {
    return circulationNotes;
  }

  public void setCirculationNotes(List<CirculationNote> circulationNotes) {
    this.circulationNotes = circulationNotes;
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

  public Item toItem(String hridString, Statisticalcodes statisticalcodes, Materialtypes materilatypes) {
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

    Set<String> yearCaption = new HashSet<>();
    if (Objects.nonNull(mfhdItem.getCaption())) {
      yearCaption.add(mfhdItem.getCaption());
    }
    if (Objects.nonNull(spineLabel)) {
      yearCaption.add(spineLabel);
    }
    item.setYearCaption(yearCaption);

    item.setVolume(mfhdItem.getYear());

    item.setCirculationNotes(circulationNotes);

    List<Note__1> notes = new ArrayList<>();
    Note__1 note = new Note__1();
    note.setNote(mfhdItem.getFreetext());
    note.setStaffOnly(true);
    note.setItemNoteTypeId(itemNoteTypeId);
    notes.add(note);
    notes.addAll(itemNotes);
    item.setNotes(notes);

    item.setBarcode(barcode);
    item.setChronology(mfhdItem.getChron());
    item.setNumberOfPieces(String.valueOf(numberOfPieces));
    item.setEnumeration(mfhdItem.getItemEnum());

    // default to not suppress discovery
    item.setDiscoverySuppress(false);

    Status status = new Status();
    // setting to available by default
    status.setName(Name.AVAILABLE);
    // assuming current date by default
    status.setDate(new Date());

    Set<String> statisticalCodeIds = new HashSet<>();

    AtomicBoolean statusesFirstPass = new AtomicBoolean(true);
    statuses.stream().forEach(s -> {

      if (statusesFirstPass.compareAndSet(true, false)) {
        if (isNotEmpty(s.getCirctrans())) {
          status.setName(Name.AVAILABLE);
        } else if (isNotEmpty(s.getItemStatus())) {
          Name name = Name.fromValue(s.getItemStatus());
          status.setName(name);
        }
        if (isNotEmpty(s.getItemStatusDate())) {
          Date date = Date.from(Instant.parse(s.getItemStatusDate()));
          status.setDate(date);
        }
      }

      if (isNotEmpty(s.getItemStatus())) {
        if (s.getItemStatus().toLowerCase().contains("damaged")) {
          item.setItemDamagedStatusId(itemDamagedStatusId);
        } else {
          Optional<Statisticalcode> potentialStatisticalcode = statisticalcodes.getStatisticalCodes().stream()
            .filter(sc -> sc.getCode().equals(s.getItemStatus()))
            .findFirst();
          if (potentialStatisticalcode.isPresent()) {
            statisticalCodeIds.add(potentialStatisticalcode.get().getId());
          }
        }

        if (s.getItemStatus().contains("Missing") || s.getItemStatus().contains("Lost") || s.getItemStatus().contains("Withdrawn")) {
          item.setDiscoverySuppress(true);
        }
      }
    });

    item.setStatus(status);
    item.setStatisticalCodeIds(statisticalCodeIds);

    item.setHrid(hridString);

    Set<String> formerIds = new HashSet<>();
    formerIds.add(itemId);
    item.setFormerIds(formerIds);

    org.folio.rest.jaxrs.model.Metadata metadata = new org.folio.rest.jaxrs.model.Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    item.setMetadata(metadata);

    item.setMaterialTypeId(materialTypeId);

    return item;
  }

}
