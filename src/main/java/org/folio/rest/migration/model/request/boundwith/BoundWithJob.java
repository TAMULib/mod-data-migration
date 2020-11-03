package org.folio.rest.migration.model.request.boundwith;

import java.util.Map;

import org.folio.rest.migration.model.request.AbstractJob;

public class BoundWithJob extends AbstractJob {

  private Map<String, String> references;

  public BoundWithJob() {

  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
