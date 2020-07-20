package org.folio.rest.migration.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ReferenceDatum {

  private final String tenant;

  private final String token;

  private final String path;

  private final JsonNode data;

  public ReferenceDatum(String tenant, String token, String path, JsonNode data) {
    this.tenant = tenant;
    this.token = token;
    this.path = path;
    this.data = data;
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

  public static ReferenceDatum of(ReferenceData referenceData, JsonNode data) {
    return new ReferenceDatum(referenceData.getTenant(), referenceData.getToken(), referenceData.getPath(), data);
  }

}
