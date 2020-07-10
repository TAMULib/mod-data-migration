package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class HoldingExtraction extends AbstractExtraction {

  @NotNull
  private String locationSql;

  @NotNull
  private String marcSql;

  public HoldingExtraction() {
    super();
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

  public String getMarcSql() {
    return marcSql;
  }

  public void setMarcSql(String marcSql) {
    this.marcSql = marcSql;
  }

}