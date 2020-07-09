package org.folio.rest.migration.model.request;

public class UserDefaults {

  private Boolean primaryAddress;

  private String preferredContactType;

  private String temporaryEmail;

  public UserDefaults() { }

  public Boolean getPrimaryAddress() {
    return primaryAddress;
  }

  public void setPrimaryAddress(Boolean primaryAddress) {
    this.primaryAddress = primaryAddress;
  }

  public String getPreferredContactType() {
    return preferredContactType;
  }

  public void setPreferredContactType(String preferredContactType) {
    this.preferredContactType = preferredContactType;
  }

  public String getTemporaryEmail() {
    return temporaryEmail;
  }

  public void setTemporaryEmail(String temporaryEmail) {
    this.temporaryEmail = temporaryEmail;
  }

}