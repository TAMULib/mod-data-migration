package org.folio.rest.migration.model.request.item;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class ItemMaps {

  @NotNull
  private Map<String, Map<String, String>> location;

  @NotNull
  private Map<String, String> loanType;

  @NotNull
  private Map<String, Integer> itemStatus;

  @NotNull
  private Map<String, String> statusName;

  public ItemMaps() {
    location = new HashMap<>();
    loanType = new HashMap<>();
    itemStatus = new HashMap<>();
    statusName = new HashMap<>();
  }

  public Map<String, Map<String, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<String, String>> location) {
    this.location = location;
  }

  public Map<String, String> getLoanType() {
    return loanType;
  }

  public void setLoanType(Map<String, String> loanType) {
    this.loanType = loanType;
  }

  public Map<String, Integer> getItemStatus() {
    return itemStatus;
  }

  public void setItemStatus(Map<String, Integer> itemStatus) {
    this.itemStatus = itemStatus;
  }

  public Map<String, String> getStatusName() {
    return statusName;
  }

  public void setStatusName(Map<String, String> statusName) {
    this.statusName = statusName;
  }

}
