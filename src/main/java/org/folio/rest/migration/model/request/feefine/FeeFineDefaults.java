package org.folio.rest.migration.model.request.feefine;

import javax.validation.constraints.NotNull;

public class FeeFineDefaults {

  @NotNull
  private String materialTypeId;

  public FeeFineDefaults() {

  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(String materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

}
