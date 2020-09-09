package org.folio.rest.migration.model.request.feefine;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class FeeFineOwner {

  @NotNull
  private String ownerId;

  @NotNull
  private String feeFineOwner;

  @NotNull
  private Map<String, String> fineFeeType;

  public FeeFineOwner() {

  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getFeeFineOwner() {
    return feeFineOwner;
  }

  public void setFeeFineOwner(String feeFineOwner) {
    this.feeFineOwner = feeFineOwner;
  }

  public Map<String, String> getFineFeeType() {
    return fineFeeType;
  }

  public void setFineFeeType(Map<String, String> fineFeeType) {
    this.fineFeeType = fineFeeType;
  }

}
