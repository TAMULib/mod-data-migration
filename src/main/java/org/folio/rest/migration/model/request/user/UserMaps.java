package org.folio.rest.migration.model.request.user;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class UserMaps {

  @NotNull
  private Map<String, String> patronGroup;

  @NotNull
  private Map<String, String> preferredContactType;

  public UserMaps() {
    patronGroup = new HashMap<>();
    preferredContactType = new HashMap<>();
  }

  public Map<String, String> getPatronGroup() {
    return patronGroup;
  }

  public void setPatronGroup(Map<String, String> patronGroup) {
    this.patronGroup = patronGroup;
  }

  public Map<String, String> getPreferredContactType() {
    return preferredContactType;
  }

  public void setPreferredContactType(Map<String, String> preferredContactType) {
    this.preferredContactType = preferredContactType;
  }

}
