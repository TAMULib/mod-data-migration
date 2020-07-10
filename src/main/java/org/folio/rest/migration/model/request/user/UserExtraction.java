package org.folio.rest.migration.model.request.user;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.AbstractExtraction;

public class UserExtraction extends AbstractExtraction {

  @NotNull
  private Database usernameDatabase;

  @NotNull
  private String usernameSql;

  @NotNull
  private String addressSql;

  @NotNull
  private String patronGroupSql;

  public UserExtraction() {
    super();
  }

  public Database getUsernameDatabase() {
    return usernameDatabase;
  }

  public void setUsernameDatabase(Database usernameDatabase) {
    this.usernameDatabase = usernameDatabase;
  }

  public String getUsernameSql() {
    return usernameSql;
  }

  public String getAddressSql() {
    return addressSql;
  }

  public String getPatronGroupSql() {
    return patronGroupSql;
  }

}
