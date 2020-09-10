package org.folio.rest.migration.model.request.feefine;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class FeeFineMaps {

  @NotNull
  private Map<String, Map<String, String>> location;

  @NotNull
  private Map<String, String> feefineTypeLabels;

  @NotNull
  private Map<String, Map<String, FeeFineOwner>> feefineOwner;

  public FeeFineMaps() {
    this.location = new HashMap<>();
    this.feefineTypeLabels = new HashMap<>();
    this.feefineOwner = new HashMap<>();
  }

  public Map<String, Map<String, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<String, String>> location) {
    this.location = location;
  }

  public Map<String, String> getFeefineTypeLabels() {
    return feefineTypeLabels;
  }

  public void setFeefineTypeLabels(Map<String, String> feefineTypeLabels) {
    this.feefineTypeLabels = feefineTypeLabels;
  }

  public Map<String, Map<String, FeeFineOwner>> getFeefineOwner() {
    return feefineOwner;
  }

  public void setFeefineOwner(Map<String, Map<String, FeeFineOwner>> feefineOwner) {
    this.feefineOwner = feefineOwner;
  }

}
