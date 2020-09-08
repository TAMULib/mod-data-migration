package org.folio.rest.migration.model.request.feefine;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class FeeFineExtraction extends AbstractExtraction {

  @NotNull
  private String materialTypeSql;

  public FeeFineExtraction() {
    super();
  }

  public String getMaterialTypeSql() {
    return materialTypeSql;
  }

  public void setMaterialTypeSql(String materialTypeSql) {
    this.materialTypeSql = materialTypeSql;
  }

}
