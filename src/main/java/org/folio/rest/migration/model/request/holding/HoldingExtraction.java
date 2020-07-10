package org.folio.rest.migration.model.request.holding;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

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