package org.folio.rest.migration.model.request.feefine;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class FeeFineExtraction extends AbstractExtraction {

  @NotNull
  private String materialTypeSql;

  @NotNull
  private String locationSql;

  public FeeFineExtraction() {
    super();
  }

  public String getMaterialTypeSql() {
    return materialTypeSql;
  }

  public void setMaterialTypeSql(String materialTypeSql) {
    this.materialTypeSql = materialTypeSql;
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

}
