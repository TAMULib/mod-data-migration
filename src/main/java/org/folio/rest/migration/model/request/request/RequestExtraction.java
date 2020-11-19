package org.folio.rest.migration.model.request.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class RequestExtraction extends AbstractExtraction {

  @NotNull
  private String locationSql;

  public RequestExtraction() {
    super();
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

}
