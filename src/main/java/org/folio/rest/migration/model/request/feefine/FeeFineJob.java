package org.folio.rest.migration.model.request.feefine;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class FeeFineJob extends AbstractJob {

  @NotNull
  private String userId;

  private Map<String, String> references;

  public FeeFineJob() {

  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

}
