package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Database;

public class UserExtraction extends AbstractExtraction {

  private Database usernameDatabase;

  @NotNull
  private String usernameSql;

  @NotNull
  private String addressesSql;

  @NotNull
  private String patronGroupSql;

  @NotNull
  private String amdbDecodeString;

  @NotNull
  private String msdbDecodeString;

  public UserExtraction() {
    super();
  }

  public String getUsernameSql() {
    return usernameSql;
  }

  public String getAddressesSql() {
    return addressesSql;
  }

  public String getPatronGroupSql() {
    return patronGroupSql;
  }

  public Database getUsernameDatabase() {
    return usernameDatabase;
  }

  public String getAmdbDecodeString() {
    return amdbDecodeString;
  }

  public String getMsdbDecodeString() {
    return msdbDecodeString;
  }

  public void setUsernameDatabase(Database usernameDatabase) {
    this.usernameDatabase = usernameDatabase;
  }

}
