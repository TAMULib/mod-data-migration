package org.folio.rest.migration.model.request;

public class ItemDefaults {

  private String permanentLoanTypeId;

  private String permanentLocationId;

  public ItemDefaults() { }

  public String getPermanentLoanTypeId() {
    return permanentLoanTypeId;
  }

  public void setPermanentLoanTypeId(String permanentLoanTypeId) {
    this.permanentLoanTypeId = permanentLoanTypeId;
  }

  public String getPermanentLocationId() {
    return permanentLocationId;
  }

  public void setPermanentLocationId(String permanentLocationId) {
    this.permanentLocationId = permanentLocationId;
  }

}