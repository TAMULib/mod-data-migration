package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class ItemMaps {

  @NotNull
  private Map<String, String> location;

  @NotNull
  private Map<String, String> loanType;

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

}