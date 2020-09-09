package org.folio.rest.migration.model.request.feefine;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class FeeFineOwner {

  @NotNull
  private String ownerId;

  @NotNull
  private String feeFineOwner;

  @NotNull
  private Map<Integer, String> fineFeeType;

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

  public Map<Integer, String> getFineFeeType() {
    return fineFeeType;
  }

  public void setFineFeeType(Map<Integer, String> fineFeeType) {
    this.fineFeeType = fineFeeType;
  }

}
