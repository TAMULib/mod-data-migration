package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

public class VendorReferenceLinkJob extends AbstractJob {

  private Map<String, String> references;

  public VendorReferenceLinkJob() {
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