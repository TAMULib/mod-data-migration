package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public abstract class AbstractDefaults {

  @NotNull
  private Map<String, String> defaults;

  public AbstractDefaults() {
    defaults = new HashMap<String, String>();
  }

  public Map<String, String> getDefaults() {
    return defaults;
  }

  public void setDefaults(Map<String, String> defaults) {
    this.defaults = defaults;
  }
}