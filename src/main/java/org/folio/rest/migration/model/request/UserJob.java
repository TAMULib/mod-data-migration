package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class UserJob extends AbstractJob {

  @NotNull
  private String userId;

  private Map<String, String> references;

  public UserJob() {
    super();
    references = new HashMap<String, String>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}