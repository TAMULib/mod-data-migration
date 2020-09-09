package org.folio.rest.migration.model;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.Accountdata;
import org.folio.rest.jaxrs.model.Feefineactiondata;
import org.folio.rest.jaxrs.model.Metadata;
import org.folio.rest.jaxrs.model.PaymentStatus;
import org.folio.rest.jaxrs.model.Status__1;
import org.folio.rest.migration.model.request.feefine.FeeFineDefaults;
import org.folio.rest.migration.model.request.feefine.FeeFineMaps;
import org.folio.rest.model.ReferenceLink;

public class FeeFineRecord {

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

  private final String id;

  private Optional<String> materialType;

  private Optional<ReferenceLink> userRL;

  private Optional<ReferenceLink> instanceRL;
  private Optional<ReferenceLink> holdingRL;
  private Optional<ReferenceLink> itemRL;

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
  }

  public Accountdata toAccount(FeeFineMaps maps, FeeFineDefaults defaults) {
    Accountdata account = new Accountdata();
    account.setId(getId());

    account.setAmount(Double.parseDouble(getAmount()));
    account.setRemaining(Double.parseDouble(getRemaining()));

    String feeFineType = maps.getFeefineTypeLabels().get(getFinefeeType());
    account.setFeeFineType(feeFineType);

    Date createDate = new Date(); // TODO: format to string getCreateDate
    account.setDateCreated(createDate);

    account.setUserId(userRL.get().getFolioReference());

    PaymentStatus paymentStatus = new PaymentStatus();
    paymentStatus.setName("Outstanding");
    account.setPaymentStatus(paymentStatus);

    // TODO: status

    Metadata metadata = new Metadata();
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

    if (StringUtils.isNoneEmpty(getFinefeeNote())) {
      feefineaction.setComments(getFinefeeNote());
    }

    return feefineaction;
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

  public Optional<String> getMaterialType() {
    return materialType;
  }

  public void setMaterialType(Optional<String> materialType) {
    this.materialType = materialType;
  }

  public Optional<ReferenceLink> getUserRL() {
    return userRL;
  }

  public void setUserRL(Optional<ReferenceLink> userRL) {
    this.userRL = userRL;
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

}
