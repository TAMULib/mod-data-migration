package org.folio.rest.migration.model.request.feefine;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class FeeFineMaps {

  @NotNull
  private Map<String, String> feefineTypeLabels;

  @NotNull
  private Map<String, Map<String, FeeFineOwner>> feefineOwner;

  public FeeFineMaps() {

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
