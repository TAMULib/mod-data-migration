package org.folio.rest.migration.model;

public class ItemStatusRecord {

  private final String itemStatus;
  private final String itemStatusDate;
  private final String circtrans;
  private final Integer itemStatusDesc;

  public ItemStatusRecord(String itemStatus, String itemStatusDate, String circtrans, Integer itemStatusDesc) {
    this.itemStatus = itemStatus;
    this.itemStatusDate = itemStatusDate;
    this.circtrans = circtrans;
    this.itemStatusDesc = itemStatusDesc;
  }

  public String getItemStatus() {
    return itemStatus;
  }

  public String getItemStatusDate() {
    return itemStatusDate;
  }

  public String getCirctrans() {
    return circtrans;
  }

  public Integer getItemStatusDesc() {
    return itemStatusDesc;
  }

}
