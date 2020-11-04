package org.folio.rest.migration.model;

public class ItemMfhdRecord {

  private final String caption;
  private final String chron;
  private final String itemEnum;
  private final String freetext;
  private final String year;
  private final String location;

  private final String callNumber;
  private final String callNumberType;

  public ItemMfhdRecord(String caption, String chron, String itemEnum, String freetext, String year, String location,
      String callNumber, String callNumberType) {
    this.caption = caption;
    this.chron = chron;
    this.itemEnum = itemEnum;
    this.freetext = freetext;
    this.year = year;
    this.location = location;
    this.callNumber = callNumber;
    this.callNumberType = callNumberType;
  }

  public String getCaption() {
    return caption;
  }

  public String getChron() {
    return chron;
  }

  public String getItemEnum() {
    return itemEnum;
  }

  public String getFreetext() {
    return freetext;
  }

  public String getYear() {
    return year;
  }

  public String getLocation() {
    return location;
  }

  public String getCallNumber() {
    return callNumber;
  }

  public String getCallNumberType() {
    return callNumberType;
  }

}
