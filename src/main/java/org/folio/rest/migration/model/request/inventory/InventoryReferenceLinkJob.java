package org.folio.rest.migration.model.request.inventory;

import java.util.HashMap;
import java.util.Map;

import org.folio.rest.migration.model.request.AbstractJob;

public class InventoryReferenceLinkJob extends AbstractJob {

  private Map<String, String> references;

  public InventoryReferenceLinkJob() {
    super();
    references = new HashMap<String, String>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
