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

  @NotNull
  private String patronNoteSql;

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

  public void setUsernameSql(String usernameSql) {
    this.usernameSql = usernameSql;
  }

  public String getAddressSql() {
    return addressSql;
  }

  public void setAddressSql(String addressSql) {
    this.addressSql = addressSql;
  }

  public String getPatronGroupSql() {
    return patronGroupSql;
  }

  public void setPatronGroupSql(String patronGroupSql) {
    this.patronGroupSql = patronGroupSql;
  }

  public String getPatronNoteSql() {
    return patronNoteSql;
  }

  public void setPatronNoteSql(String patronNoteSql) {
    this.patronNoteSql = patronNoteSql;
  }

}
