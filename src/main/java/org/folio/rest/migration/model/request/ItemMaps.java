package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class ItemMaps {

  @NotNull
  private Map<String, String> location;

  @NotNull
  private Map<String, String> loanType;

  @NotNull
  private Map<String, String> itemStatus;

  @NotNull
  private Map<String, String> statusName;

  public ItemMaps() {
    location = new HashMap<String, String>();
    loanType = new HashMap<String, String>();
  }

  public Map<String, String> getLocation() {
    return location;
  }

  public void setLocation(Map<String, String> location) {
    this.location = location;
  }

  public Map<String, String> getLoanType() {
    return loanType;
  }

  public void setLoanType(Map<String, String> loanType) {
    this.loanType = loanType;
  }

  public Map<String, String> getItemStatus() {
    return itemStatus;
  }

  public void setItemStatus(Map<String, String> itemStatus) {
    this.itemStatus = itemStatus;
  }

  public Map<String, String> getStatusName() {
    return statusName;
  }

  public void setStatusName(Map<String, String> statusName) {
    this.statusName = statusName;
  }

}
