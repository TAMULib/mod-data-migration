package org.folio.rest.migration.model.request.loan;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class LoanMaps {

  @NotNull
  private Map<String, Map<String, String>> location;

  @NotNull
  private Map<String, String> locationCode;

  public LoanMaps() {
    location = new HashMap<>();
    locationCode = new HashMap<>();
  }

  public Map<String, Map<String, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<String, String>> location) {
    this.location = location;
  }

  public Map<String, String> getLocationCode() {
    return locationCode;
  }

  public void setLocationCode(Map<String, String> locationCode) {
    this.locationCode = locationCode;
  }

}
