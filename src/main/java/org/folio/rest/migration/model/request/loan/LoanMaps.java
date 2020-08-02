package org.folio.rest.migration.model.request.loan;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class LoanMaps {

  @NotNull
  private Map<String, Map<Integer, String>> location;

  public LoanMaps() {
    location = new HashMap<>();
  }

  public Map<String, Map<Integer, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<Integer, String>> location) {
    this.location = location;
  }

}
