package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class VendorExtraction extends AbstractExtraction {

  @NotNull
  private String accountSql;

  @NotNull
  private String addressSql;

  @NotNull
  private String aliasSql;

  @NotNull
  private String noteSql;

  @NotNull
  private String phoneSql;

  public VendorExtraction() {
    super();
  }

  public String getAccountSql() {
    return accountSql;
  }

  public void setAccountSql(String accountSql) {
    this.accountSql = accountSql;
  }

  public String getAddressSql() {
    return addressSql;
  }

  public void setAddressSql(String addressSql) {
    this.addressSql = addressSql;
  }

  public String getAliasSql() {
    return aliasSql;
  }

  public void setAliasSql(String aliasSql) {
    this.aliasSql = aliasSql;
  }

  public String getNoteSql() {
    return noteSql;
  }

  public void setNoteSql(String noteSql) {
    this.noteSql = noteSql;
  }

  public String getPhoneSql() {
    return phoneSql;
  }

  public void setPhoneSql(String phoneSql) {
    this.phoneSql = phoneSql;
  }

}