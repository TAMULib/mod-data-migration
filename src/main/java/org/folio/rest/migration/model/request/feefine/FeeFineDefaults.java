package org.folio.rest.migration.model.request.feefine;

import javax.validation.constraints.NotNull;

public class FeeFineDefaults {

  @NotNull
  private String materialTypeId;

  @NotNull
  private String itemId;

  public FeeFineDefaults() {

  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(String materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

}
