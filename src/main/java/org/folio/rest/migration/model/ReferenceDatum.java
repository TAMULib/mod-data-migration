package org.folio.rest.migration.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ReferenceDatum {

  private final String tenant;

  private final String token;

  private final String path;

  private final JsonNode data;

  private final ReferenceData.Action action;

  public ReferenceDatum(String tenant, String token, String path, JsonNode data, ReferenceData.Action action) {
    this.tenant = tenant;
    this.token = token;
    this.path = path;
    this.data = data;
    this.action = action;
  }

  public String getTenant() {
    return tenant;
  }

  public String getToken() {
    return token;
  }

  public String getPath() {
    return path;
  }

  public JsonNode getData() {
    return data;
  }

  public ReferenceData.Action getAction() {
    return action;
  }

  public static ReferenceDatum of(ReferenceData referenceData, JsonNode data) {
    return of(referenceData.getTenant(), referenceData.getToken(), referenceData.getPath(), data, referenceData.getAction());
  }

  public static ReferenceDatum of(String tenant, String token, String path, JsonNode data, ReferenceData.Action action) {
    return new ReferenceDatum(tenant, token, path, data, action);
  }

}
