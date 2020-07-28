package org.folio.rest.migration.model.request.loan;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class LoanExtraction extends AbstractExtraction {

  @NotNull
  private String locationSql;

  public LoanExtraction() {
    super();
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

}
