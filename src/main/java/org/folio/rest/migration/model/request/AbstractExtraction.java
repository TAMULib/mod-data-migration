package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Database;

public abstract class AbstractExtraction {

  @NotNull
  private Database database;

  @NotNull
  private String countSql;

  @NotNull
  private String pageSql;

  public AbstractExtraction() {

  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public String getCountSql() {
    return countSql;
  }

  public void setCountSql(String countSql) {
    this.countSql = countSql;
  }

  public String getPageSql() {
    return pageSql;
  }

  public void setPageSql(String pageSql) {
    this.pageSql = pageSql;
  }

}
