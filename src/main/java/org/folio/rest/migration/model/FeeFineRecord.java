package org.folio.rest.migration.model;

import java.util.Optional;

import org.folio.rest.jaxrs.model.Feefineactiondata;
import org.folio.rest.jaxrs.model.Feefinedata;
import org.folio.rest.migration.model.request.feefine.FeeFineDefaults;
import org.folio.rest.migration.model.request.feefine.FeeFineMaps;

public class FeeFineRecord {

  private final String patronId;
  private final String itemId;
  private final String itemBarcode;
  private final String finefeeId;
  private final String amount;
  private final String remaining;
  private final String finefeeType;
  private final String finefeeNote;
  private final String createSate;
  private final String mfhdId;
  private final String displayCallNo;
  private final String itemEnum;
  private final String chron;
  private final String effectiveLocation;
  private final String fineLocation;
  private final String title;
  private final String bibId;

  private final Optional<String> materialType;

  public FeeFineRecord(
    String patronId,
    String itemId,
    String itemBarcode,
    String finefeeId,
    String amount,
    String remaining,
    String finefeeType,
    String finefeeNote,
    String createSate,
    String mfhdId,
    String displayCallNo,
    String itemEnum,
    String chron,
    String effectiveLocation,
    String fineLocation,
    String title,
    String bibId,
    Optional<String> materialType
  ) {
    this.patronId = patronId;
    this.itemId = itemId;
    this.itemBarcode = itemBarcode;
    this.finefeeId = finefeeId;
    this.amount = amount;
    this.remaining = remaining;
    this.finefeeType = finefeeType;
    this.finefeeNote = finefeeNote;
    this.createSate = createSate;
    this.mfhdId = mfhdId;
    this.displayCallNo = displayCallNo;
    this.itemEnum = itemEnum;
    this.chron = chron;
    this.effectiveLocation = effectiveLocation;
    this.fineLocation = fineLocation;
    this.title = title;
    this.bibId = bibId;
    this.materialType = materialType;
  }

  public Feefinedata toFeefine(FeeFineMaps maps, FeeFineDefaults defaults) {
    Feefinedata feefine = new Feefinedata();

    return feefine;
  }

  public Feefineactiondata toFeefineaction(FeeFineMaps maps, FeeFineDefaults defaults) {
    Feefineactiondata feefineaction = new Feefineactiondata();

    return feefineaction;
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

  public String getCreateSate() {
    return createSate;
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

}
