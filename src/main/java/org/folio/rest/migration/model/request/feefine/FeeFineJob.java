package org.folio.rest.migration.model.request.feefine;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class FeeFineJob extends AbstractJob {

  @NotNull
  private String user;

  @NotNull
  private Map<String, String> references;

  public FeeFineJob() {
    references = new HashMap<>();
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
