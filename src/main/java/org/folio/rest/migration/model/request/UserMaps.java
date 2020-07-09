package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class UserMaps {

  @NotNull
  private Map<String, Map<String, String>> patronGroup;

  public UserMaps() {
    patronGroup = new HashMap<String, Map<String, String>>();
  }

  public Map<String, Map<String, String>> getPatronGroup() {
    return patronGroup;
  }

  public void setPatronGroup(Map<String, Map<String, String>> patronGroup) {
    this.patronGroup = patronGroup;
  }

}