package org.folio.rest.migration.model.request.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class UserMaps {

  @NotNull
  private Map<String, String> patronGroup;

  @NotNull
  private Map<String, List<String>> ignore;

  public UserMaps() {
    patronGroup = new HashMap<>();
    ignore = new HashMap<>();
  }

  public Map<String, String> getPatronGroup() {
    return patronGroup;
  }

  public void setPatronGroup(Map<String, String> patronGroup) {
    this.patronGroup = patronGroup;
  }

  public Map<String, List<String>> getIgnore() {
    return ignore;
  }

  public void setIgnore(Map<String, List<String>> ignore) {
    this.ignore = ignore;
  }

}
