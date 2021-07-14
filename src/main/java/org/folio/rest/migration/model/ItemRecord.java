package org.folio.rest.migration.model;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.folio.Materialtypes;
import org.folio.StatisticalCode;
import org.folio.Statisticalcodes;
import org.folio.rest.jaxrs.model.inventory.CirculationNote;
import org.folio.rest.jaxrs.model.inventory.EffectiveCallNumberComponents;
import org.folio.rest.jaxrs.model.inventory.Item;
import org.folio.rest.jaxrs.model.inventory.Metadata;
import org.folio.rest.jaxrs.model.inventory.Note__1;
import org.folio.rest.jaxrs.model.inventory.Status;
import org.folio.rest.jaxrs.model.inventory.Status.Name;
import org.folio.rest.migration.model.request.item.ItemDefaults;
import org.folio.rest.migration.model.request.item.ItemMaps;
import org.marc4j.callnum.CallNumber;
import org.marc4j.callnum.LCCallNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRecord {

  private final static Logger log = LoggerFactory.getLogger(ItemRecord.class);

  private final String itemId;

  private final int numberOfPieces;
  private final String spineLabel;
  
  private final String itemNoteTypeId;
  private final String itemDamagedStatusId;

  private final String custodianStatisticalCodeId;

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
  private String effectiveLocationId;

  private String createdByUserId;
  private Date createdDate;

  private String callNumber;

  private String callNumberPrefix;

  private String callNumberSuffix;

  public ItemRecord(String itemId, int numberOfPieces, String spineLabel, String itemNoteTypeId, String itemDamagedStatusId, String custodianStatisticalCodeId) {
    this.itemId = itemId;
    this.numberOfPieces = numberOfPieces;
    this.spineLabel = spineLabel;
    this.itemNoteTypeId = itemNoteTypeId;
    this.itemDamagedStatusId = itemDamagedStatusId;
    this.custodianStatisticalCodeId = custodianStatisticalCodeId;
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

  public String getCustodianStatisticalCodeId() {
    return custodianStatisticalCodeId;
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

  public String getEffectiveLocationId() {
    return effectiveLocationId;
  }

  public void setEffectiveLocationId(String effectiveLocationId) {
    this.effectiveLocationId = effectiveLocationId;
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

  public String getCallNumber() {
    return callNumber;
  }

  public void setCallNumber(String callNumber) {
    this.callNumber = callNumber;
  }

  public String getCallNumberPrefix() {
    return callNumberPrefix;
  }

  public void setCallNumberPrefix(String callNumberPrefix) {
    this.callNumberPrefix = callNumberPrefix;
  }

  public String getCallNumberSuffix() {
    return callNumberSuffix;
  }

  public void setCallNumberSuffix(String callNumberSuffix) {
    this.callNumberSuffix = callNumberSuffix;
  }

  public Item toItem(String hridString, Statisticalcodes statisticalcodes, Materialtypes materilatypes, ItemMaps maps, ItemDefaults defaults) {
    final Item item = new Item();
    item.setId(id);
    item.setHoldingsRecordId(holdingId);
    item.setMaterialTypeId(materialTypeId);

    item.setPermanentLoanTypeId(permanentLoanTypeId);
    if (Objects.nonNull(temporaryLoanTypeId)) {
      item.setTemporaryLoanTypeId(temporaryLoanTypeId);
    }

    if (Objects.nonNull(permanentLocationId)) {
      item.setPermanentLocationId(permanentLocationId);
    }
    if (Objects.nonNull(temporaryLocationId)) {
      item.setTemporaryLocationId(temporaryLocationId);
    }
    if (Objects.nonNull(effectiveLocationId)) {
      item.setEffectiveLocationId(effectiveLocationId);
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

    if (isNotEmpty(mfhdItem.getFreetext())) {
      Note__1 note = new Note__1();
      note.setNote(mfhdItem.getFreetext());
      note.setStaffOnly(true);
      note.setItemNoteTypeId(itemNoteTypeId);
      itemNotes.add(note);
    }

    item.setNotes(itemNotes);
    item.setCirculationNotes(circulationNotes);

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

    statisticalCodeIds.add(custodianStatisticalCodeId);

    Map<String, String> statusNameMap = maps.getStatusName();

    for (int i = 0; i < statuses.size(); i++) {
      ItemStatusRecord s = statuses.get(i);

      String folioItemStatus = "Available";

      if (StringUtils.isEmpty(s.getCirctrans())
          && StringUtils.isNotEmpty(s.getItemStatus())
          && statusNameMap.containsKey(s.getItemStatus())) {
        folioItemStatus = statusNameMap.get(s.getItemStatus());
      }

      if (s.getItemStatusDesc().equals("Damaged")) {
        item.setItemDamagedStatusId(itemDamagedStatusId);
      } else {
        Optional<StatisticalCode> potentialStatisticalcode = statisticalcodes.getStatisticalCodes().stream()
          .filter(sc -> sc.getName().equals(s.getItemStatusDesc()))
          .findFirst();
        if (potentialStatisticalcode.isPresent()) {
          statisticalCodeIds.add(potentialStatisticalcode.get().getId());
        }
      }

      if (s.getItemStatusDesc().equals("Missing") ||
          s.getItemStatusDesc().equals("Lost--Library Applied") ||
          s.getItemStatusDesc().equals("Lost--System Applied") ||
          s.getItemStatusDesc().equals("Withdrawn")) {
        item.setDiscoverySuppress(true);
      }

      if (i == 0) {
        status.setName(Name.fromValue(folioItemStatus));
        if (isNotEmpty(s.getItemStatusDate())) {
          status.setDate(Date.from(Instant.parse(s.getItemStatusDate())));
        } else {
          log.debug(String.format("Item with barcode %s status does not have a date", barcode));
        }
      }

    }

    item.setStatus(status);
    item.setStatisticalCodeIds(statisticalCodeIds);

    item.setHrid(hridString);

    Set<String> formerIds = new HashSet<>();
    formerIds.add(itemId);
    item.setFormerIds(formerIds);


    EffectiveCallNumberComponents effectiveCallNumberComponents = new EffectiveCallNumberComponents();

    String callNumberType = mfhdItem.getCallNumberType();
    if (StringUtils.isNotEmpty(callNumberType) && maps.getCallNumberType().containsKey(callNumberType)) {
      effectiveCallNumberComponents.setTypeId(maps.getCallNumberType().get(callNumberType));
    } else {
      effectiveCallNumberComponents.setTypeId(defaults.getCallNumberTypeId());
    }

    effectiveCallNumberComponents.setCallNumber(callNumber);
    effectiveCallNumberComponents.setPrefix(callNumberPrefix);
    effectiveCallNumberComponents.setSuffix(callNumberSuffix);

    item.setEffectiveCallNumberComponents(effectiveCallNumberComponents);

    setEffectiveShelvingOrder(item);

    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    metadata.setUpdatedByUserId(createdByUserId);
    metadata.setUpdatedDate(createdDate);
    item.setMetadata(metadata);

    item.setMaterialTypeId(materialTypeId);

    return item;
  }

  private void setEffectiveShelvingOrder(final Item item) {
    if (StringUtils.isNotBlank(item.getEffectiveCallNumberComponents().getCallNumber())) {
      Optional<String> shelfKey
        = getShelfKeyFromCallNumber(
        Stream.of(
          item.getEffectiveCallNumberComponents().getCallNumber(),
          item.getVolume(),
          item.getEnumeration(),
          item.getChronology(),
          item.getCopyNumber()
        ).filter(StringUtils::isNotBlank)
          .map(StringUtils::trim)
          .collect(Collectors.joining(" "))
      );
      String suffixValue =
        Objects.toString(Optional.ofNullable(item.getEffectiveCallNumberComponents())
          .orElse(new EffectiveCallNumberComponents()).getSuffix(), "")
          .trim();
      String nonNullableSuffixValue = suffixValue.isEmpty() ? "" : " " + suffixValue;
      item.setEffectiveShelvingOrder(
        shelfKey.stream()
          .map(shelfKeyValue -> shelfKeyValue + nonNullableSuffixValue)
          .findFirst()
          .orElse(nonNullableSuffixValue));
    }
  }

  private Optional<String> getShelfKeyFromCallNumber(String callNumberParam) {
    if (callNumberParam == null) return Optional.empty();
    CallNumber callNumber = new LCCallNumber();
    callNumber.parse(callNumberParam);
    return callNumber.isValid() ? Optional.of(callNumber.getShelfKey()) : Optional.empty();
  }

}
