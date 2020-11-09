package org.folio.rest.migration.model.request.divitpatron;

import javax.validation.constraints.NotNull;

public class DivITPatronJob {

  @NotNull
  private String name;

  @NotNull
  private String sql;

  public DivITPatronJob() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

}
