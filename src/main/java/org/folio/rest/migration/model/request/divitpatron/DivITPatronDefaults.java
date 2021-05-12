package org.folio.rest.migration.model.request.divitpatron;

public class DivITPatronDefaults {

  private String preferredContactType;

  private String temporaryEmail;

  public DivITPatronDefaults() {

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
