package org.folio.rest.migration.model.request.vendor;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class VendorJob extends AbstractJob {

  @NotNull
  private String userId;

  private Map<String, String> references;

  @NotNull
  private String locations;

  @NotNull
  private String statuses;

  @NotNull
  private String types;

  public VendorJob() {
    super();
    references = new HashMap<String, String>();
    locations = "";
    statuses = "";
    types = "";
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public String getLocations() {
    return locations;
  }

  public void setLocations(String locations) {
    this.locations = locations;
  }

  public String getStatuses() {
    return statuses;
  }

  public void setStatuses(String statuses) {
    this.statuses = statuses;
  }

  public String getTypes() {
    return types;
  }

  public void setTypes(String types) {
    this.types = types;
  }

}