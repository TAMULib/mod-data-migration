package org.folio.rest.migration.model.request.item;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class ItemJob extends AbstractJob {

  @NotNull
  private String user;

  @NotNull
  private String itemNoteTypeId;

  @NotNull
  private String itemDamagedStatusId;

  private Map<String, String> references;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
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
