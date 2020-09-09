package org.folio.rest.migration.model.request.feefine;

import java.util.Map;

import org.folio.rest.migration.model.request.AbstractJob;

public class FeeFineJob extends AbstractJob {

  private Map<String, String> references;

  public FeeFineJob() {

  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
