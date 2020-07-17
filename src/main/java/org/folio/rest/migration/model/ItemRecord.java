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
  private final String barcode;

  private final String caption;
  private final String enumeration;
  private final String chronology;
  private final String freetext;
  private final String year;

  private final int numberOfPieces;
  private final String spineLabel;
  private final String materialTypeId;
  private final String itemNoteTypeId;
  private final String itemDamagedStatusId;

  private final List<Note__1> itemNotes;
  private final List<CirculationNote> circulationNotes;

  private final List<ItemStatusRecord> statuses;

  private String permanentLoanTypeId;
  private String temporaryLoanTypeId;

  private String permanentLocationId;
  private String temporaryLocationId;

  private String id;
  private String holdingId;

  private String createdByUserId;
  private Date createdDate;

  public ItemRecord(String itemId, String barcode, String caption, String enumeration, String chronology, String freetext, String year, int numberOfPieces, String spineLabel, String materialTypeId, String itemNoteTypeId, String itemDamagedStatusId, List<ItemStatusRecord> statuses, List<Note__1> itemNotes, List<CirculationNote> circulationNotes) {
    this.itemId = itemId;
    this.barcode = barcode;
    this.caption = caption;
    this.enumeration = enumeration;
    this.chronology = chronology;
    this.freetext = freetext;
    this.year = year;
    this.numberOfPieces = numberOfPieces;
    this.spineLabel = spineLabel;
    this.materialTypeId = materialTypeId;
    this.itemNoteTypeId = itemNoteTypeId;
    this.itemDamagedStatusId = itemDamagedStatusId;
    this.statuses = statuses;
    this.itemNotes = itemNotes;
    this.circulationNotes = circulationNotes;
  }

  public String getItemDamagedStatusId() {
    return itemDamagedStatusId;
  }

  public String getSpineLabel() {
    return spineLabel;
  }

  public String getYear() {
    return year;
  }

  public String getFreetext() {
    return freetext;
  }

  public String getCaption() {
    return caption;
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

  public List<ItemStatusRecord> getStatuses() {
    return statuses;
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
    yearCaption.add(caption);
    yearCaption.add(spineLabel);
    item.setYearCaption(yearCaption);

    item.setVolume(year);

    item.setCirculationNotes(circulationNotes);

    List<Note__1> notes = new ArrayList<>();
    Note__1 note = new Note__1();
    note.setNote(freetext);
    note.setStaffOnly(true);
    note.setItemNoteTypeId(itemNoteTypeId);
    notes.add(note);
    notes.addAll(itemNotes);
    item.setNotes(notes);

    item.setBarcode(barcode);
    item.setChronology(chronology);
    item.setNumberOfPieces(String.valueOf(numberOfPieces));
    item.setEnumeration(enumeration);

    AtomicBoolean suppress = new AtomicBoolean(false);

    List<Status> processedStatuses = new ArrayList<>();
    Set<String> statisticalCodeIds = new HashSet<>();

    statuses.stream().forEach(s -> {
      Status status = new Status();
      if (isNotEmpty(s.getCirctrans())) {
        status.setName(Name.AVAILABLE);
      } else if (isNotEmpty(s.getItemStatus())) {
        Name name = Name.fromValue(s.getItemStatus());
        status.setName(name);
      } else {
        status.setName(Name.AVAILABLE);
      }

      if (isNotEmpty(s.getItemStatusDate())) {
        Date date = Date.from(Instant.parse(s.getItemStatusDate()));
        status.setDate(date);
      }
      processedStatuses.add(status);

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
          suppress.set(true);
        }
      }
    });

    item.setDiscoverySuppress(suppress.get());
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
