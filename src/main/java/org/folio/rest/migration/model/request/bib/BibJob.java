package org.folio.rest.migration.model.request.bib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class BibJob extends AbstractJob {

  @NotNull
  private String user;

  @NotNull
  private String instanceStatusId;

  @NotNull
  private String controlNumberIdentifier;

  private Map<String, String> references;

  private Map<String, List<String>> exclusions;

  public BibJob() {
    super();
    references = new HashMap<>();
    exclusions = new HashMap<>();
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getInstanceStatusId() {
    return instanceStatusId;
  }

  public void setInstanceStatusId(String instanceStatusId) {
    this.instanceStatusId = instanceStatusId;
  }

  public String getControlNumberIdentifier() {
    return controlNumberIdentifier;
  }

  public void setControlNumberIdentifier(String controlNumberIdentifier) {
    this.controlNumberIdentifier = controlNumberIdentifier;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public Map<String, List<String>> getExclusions() {
    return exclusions;
  }

  public void setExclusions(Map<String, List<String>> exclusions) {
    this.exclusions = exclusions;
  }

}
