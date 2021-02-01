package org.folio.rest.migration.model.request.order;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class OrderJob extends AbstractJob {

  @NotNull
  private String columns;

  @NotNull
  private String tables;

  @NotNull
  private String conditions;

  public OrderJob() {
    super();
  }

  public String getColumns() {
    return columns;
  }

  public void setColumns(String columns) {
    this.columns = columns;
  }

  public String getTables() {
    return tables;
  }

  public void setTables(String tables) {
    this.tables = tables;
  }

  public String getConditions() {
    return conditions;
  }

  public void setConditions(String conditions) {
    this.conditions = conditions;
  }

}
