package org.folio.rest.migration.model.request;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class ItemJob extends AbstractJob {

  @NotNull
  private String userId;

  @NotNull
  private String materialTypeId;

  private Map<String, String> references;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getMaterialTypeId() {
    return materialTypeId;
  }

  public void setMaterialTypeId(String materialTypeId) {
    this.materialTypeId = materialTypeId;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
