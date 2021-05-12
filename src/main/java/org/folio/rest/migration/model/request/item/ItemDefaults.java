package org.folio.rest.migration.model.request.item;

public class ItemDefaults {

  private String permanentLoanTypeId;

  private String materialTypeId;

  private String callNumberTypeId;

  public ItemDefaults() {

  }

  public String getPermanentLoanTypeId() {
    return permanentLoanTypeId;
  }

  public void setPermanentLoanTypeId(String permanentLoanTypeId) {
    this.permanentLoanTypeId = permanentLoanTypeId;
  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(String materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

  public String getCallNumberTypeId() {
    return callNumberTypeId;
  }

  public void setCallNumberTypeId(String callNumber) {
    this.callNumberTypeId = callNumber;
  }

}
