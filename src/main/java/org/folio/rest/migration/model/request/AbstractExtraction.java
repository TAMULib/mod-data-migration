package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Database;

public abstract class AbstractExtraction {

  @NotNull
  private Database database;

  @NotNull
  private String count;

  @NotNull
  private String page;

  public AbstractExtraction() {

  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

}
