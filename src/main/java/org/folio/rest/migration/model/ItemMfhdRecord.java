package org.folio.rest.migration.model;

public class ItemMfhdRecord {

  private final String caption;
  private final String chron;
  private final String itemEnum;
  private final String freetext;
  private final String year;
  private final String location;

  public ItemMfhdRecord(String caption, String chron, String itemEnum, String freetext, String year, String location) {
    this.caption = caption;
    this.chron = chron;
    this.itemEnum = itemEnum;
    this.freetext = freetext;
    this.year = year;
    this.location = location;
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

}
