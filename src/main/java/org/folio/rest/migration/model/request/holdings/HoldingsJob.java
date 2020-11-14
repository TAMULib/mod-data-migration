package org.folio.rest.migration.model.request.holdings;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class HoldingsJob extends AbstractJob {

  @NotNull
  private String user;

  private Map<String, String> references;

  public HoldingsJob() {
    super();
    references = new HashMap<String, String>();
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
