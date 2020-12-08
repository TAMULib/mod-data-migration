package org.folio.rest.migration.model;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.feesfines.Accountdata;
import org.folio.rest.jaxrs.model.feesfines.actions.Feefineactiondata;
import org.folio.rest.jaxrs.model.feesfines.Metadata;
import org.folio.rest.jaxrs.model.feesfines.PaymentStatus;
import org.folio.rest.jaxrs.model.feesfines.Status;
import org.folio.rest.migration.model.request.feefine.FeeFineDefaults;
import org.folio.rest.migration.model.request.feefine.FeeFineMaps;
import org.folio.rest.migration.model.request.feefine.FeeFineOwner;
import org.folio.rest.model.ReferenceLink;

public class FeeFineRecord {

  private final String id;

  private final String patronId;
  private final String itemId;
  private final String itemBarcode;
  private final String finefeeId;
  private final String amount;
  private final String remaining;
  private final String finefeeType;
  private final String finefeeNote;
  private final String createDate;
  private final String mfhdId;
  private final String displayCallNo;
  private final String itemEnum;
  private final String chron;
  private final String effectiveLocation;
  private final String fineLocation;
  private final String title;
  private final String bibId;

  private String location;

  private Optional<String> materialTypeId;

  private String userId;

  private Optional<ReferenceLink> instanceRL;
  private Optional<ReferenceLink> holdingRL;
  private Optional<ReferenceLink> itemRL;

  private String createdByUserId;
  private Date createdDate;

  public FeeFineRecord(
    String patronId,
    String itemId,
    String itemBarcode,
    String finefeeId,
    String amount,
    String remaining,
    String finefeeType,
    String finefeeNote,
    String createDate,
    String mfhdId,
    String displayCallNo,
    String itemEnum,
    String chron,
    String effectiveLocation,
    String fineLocation,
    String title,
    String bibId
  ) {
    this.patronId = patronId;
    this.itemId = itemId;
    this.itemBarcode = itemBarcode;
    this.finefeeId = finefeeId;
    this.amount = amount;
    this.remaining = remaining;
    this.finefeeType = finefeeType;
    this.finefeeNote = finefeeNote;
    this.createDate = createDate;
    this.mfhdId = mfhdId;
    this.displayCallNo = displayCallNo;
    this.itemEnum = itemEnum;
    this.chron = chron;
    this.effectiveLocation = effectiveLocation;
    this.fineLocation = fineLocation;
    this.title = title;
    this.bibId = bibId;

    this.id = UUID.randomUUID().toString();
    this.instanceRL = Optional.empty();
    this.holdingRL = Optional.empty();
    this.itemRL = Optional.empty();
  }

  public String getId() {
    return id;
  }

  public String getPatronId() {
    return patronId;
  }

  public String getItemId() {
    return itemId;
  }

  public String getItemBarcode() {
    return itemBarcode;
  }

  public String getFinefeeId() {
    return finefeeId;
  }

  public String getAmount() {
    return amount;
  }

  public String getRemaining() {
    return remaining;
  }

  public String getFinefeeType() {
    return finefeeType;
  }

  public String getFinefeeNote() {
    return finefeeNote;
  }

  public String getCreateDate() {
    return createDate;
  }

  public String getMfhdId() {
    return mfhdId;
  }

  public String getDisplayCallNo() {
    return displayCallNo;
  }

  public String getItemEnum() {
    return itemEnum;
  }

  public String getChron() {
    return chron;
  }

  public String getEffectiveLocation() {
    return effectiveLocation;
  }

  public String getFineLocation() {
    return fineLocation;
  }

  public String getTitle() {
    return title;
  }

  public String getBibId() {
    return bibId;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Optional<String> getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(Optional<String> materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Optional<ReferenceLink> getInstanceRL() {
    return instanceRL;
  }

  public void setInstanceRL(Optional<ReferenceLink> instanceRL) {
    this.instanceRL = instanceRL;
  }

  public Optional<ReferenceLink> getHoldingRL() {
    return holdingRL;
  }

  public void setHoldingRL(Optional<ReferenceLink> holdingRL) {
    this.holdingRL = holdingRL;
  }

  public Optional<ReferenceLink> getItemRL() {
    return itemRL;
  }

  public void setItemRL(Optional<ReferenceLink> itemRL) {
    this.itemRL = itemRL;
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

  public Accountdata toAccount(FeeFineMaps maps, FeeFineDefaults defaults, String schema) {
    Accountdata account = new Accountdata();
    account.setId(id);

    account.setAmount(Double.parseDouble(amount));
    account.setRemaining(Double.parseDouble(remaining));

    String feeFineType = maps.getFeefineTypeLabels().get(finefeeType);
    account.setFeeFineType(feeFineType);

    account.setDateCreated(Date.from(Instant.parse(createDate)));

    account.setUserId(userId);

    PaymentStatus paymentStatus = new PaymentStatus();
    paymentStatus.setName("Outstanding");
    account.setPaymentStatus(paymentStatus);

    Status status = new Status();
    status.setName("Open");
    account.setStatus(status);

    account.setMaterialTypeId(defaults.getMaterialTypeId());

    if (itemRL.isPresent()) {

      if (materialTypeId.isPresent()) {
        account.setMaterialTypeId(materialTypeId.get());
      }

      if (instanceRL.isPresent()) {
        account.setInstanceId(instanceRL.get().getFolioReference());
      }

      if (holdingRL.isPresent()) {
        account.setHoldingsRecordId(holdingRL.get().getFolioReference());
      }

      account.setItemId(itemRL.get().getFolioReference());

      account.setLocation(location);

      if (Objects.nonNull(itemBarcode)) {
        account.setBarcode(itemBarcode);
      }

      account.setTitle(title);

      String callNumber = displayCallNo;
      if (Objects.nonNull(itemEnum)) {
        callNumber += StringUtils.SPACE + itemEnum;
      }
      if (Objects.nonNull(chron)) {
        callNumber += StringUtils.SPACE + chron;
      }
      account.setCallNumber(callNumber);
    } else {
      account.setItemId(defaults.getItemId());
    }

    Map<String, FeeFineOwner> feeFineOwnerMap = maps.getFeefineOwner().get(schema);
    for (Entry<String, FeeFineOwner> feeDineOwnerEntry : feeFineOwnerMap.entrySet()) {
      String regex = feeDineOwnerEntry.getKey();
      if (fineLocation.matches(regex)) {
        FeeFineOwner feeFineOwner = feeDineOwnerEntry.getValue();
        account.setOwnerId(feeFineOwner.getOwnerId());
        account.setFeeFineOwner(feeFineOwner.getFeeFineOwner());
        account.setFeeFineId(feeFineOwner.getFineFeeType().get(getFinefeeType()));
      }
    }

    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    metadata.setUpdatedByUserId(createdByUserId);
    metadata.setUpdatedDate(createdDate);
    account.setMetadata(metadata);
    return account;
  }

  public Feefineactiondata toFeefineaction(Accountdata account, FeeFineMaps maps, FeeFineDefaults defaults) {
    Feefineactiondata feefineaction = new Feefineactiondata();
    feefineaction.setAccountId(account.getId());
    feefineaction.setId(UUID.randomUUID().toString());
    feefineaction.setNotify(false);
    feefineaction.setTransactionInformation("-");

    feefineaction.setAmountAction(account.getAmount());
    feefineaction.setBalance(account.getRemaining());
    feefineaction.setTypeAction(account.getFeeFineType());
    feefineaction.setDateAction(account.getDateCreated());
    feefineaction.setUserId(account.getUserId());

    if (StringUtils.isNotEmpty(finefeeNote)) {
      feefineaction.setComments(finefeeNote);
    }

    feefineaction.setCreatedAt(account.getFeeFineOwner());

    return feefineaction;
  }

}
