package org.folio.rest.migration.config.model;

import javax.validation.constraints.NotNull;

public class Database {

  @NotNull
  private String url;

  @NotNull
  private String username;

  @NotNull
  private String password;

  @NotNull
  private String driverClassName;

  public Database() {

  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

}
