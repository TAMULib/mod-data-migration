package org.folio.rest.migration.model.request.user;

public class UserDefaults {

  private Boolean primaryAddress;

  private String preferredContactType;

  private String temporaryEmail;

  private String expirationDate;

  public UserDefaults() {
  }

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

  public String getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
  }

}
