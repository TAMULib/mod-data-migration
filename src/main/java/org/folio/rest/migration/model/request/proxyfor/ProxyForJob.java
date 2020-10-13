package org.folio.rest.migration.model.request.proxyfor;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class ProxyForJob extends AbstractJob {

  @NotNull
  private String user;

  private Map<String, String> references;

  public ProxyForJob() {

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
