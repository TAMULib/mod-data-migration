package org.folio.rest.migration.model.request.request;

import java.util.HashMap;
import java.util.Map;

import org.folio.rest.migration.model.request.AbstractJob;

public class RequestJob extends AbstractJob {

  private Map<String, String> references;

  public RequestJob() {
    this.references = new HashMap<>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
