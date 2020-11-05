package org.folio.rest.migration.model.request.divitpatron;

import javax.validation.constraints.NotNull;

public class DivITPatronJob {

  @NotNull
  private String sql;

  public DivITPatronJob() {
    super();
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

}
