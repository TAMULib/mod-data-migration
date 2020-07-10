package org.folio.rest.migration.model.request;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class ItemJob extends AbstractJob {

  @NotNull
  private String userId;

  @NotNull
  private String defaultMaterialTypeId;

  @NotNull
  private String itemNoteTypeId;

  @NotNull
  private String itemDamagedStatusId;

  private Map<String, String> references;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDefaultMaterialTypeId() {
    return defaultMaterialTypeId;
  }

  public void setDefaultMaterialTypeId(String defaultMaterialTypeId) {
    this.defaultMaterialTypeId = defaultMaterialTypeId;
  }

  public String getItemNoteTypeId() {
    return itemNoteTypeId;
  }

  public void setItemNoteTypeId(String itemNoteTypeId) {
    this.itemNoteTypeId = itemNoteTypeId;
  }

  public String getItemDamagedStatusId() {
    return itemDamagedStatusId;
  }

  public void setItemDamagedStatusId(String itemDamagedStatusId) {
    this.itemDamagedStatusId = itemDamagedStatusId;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
