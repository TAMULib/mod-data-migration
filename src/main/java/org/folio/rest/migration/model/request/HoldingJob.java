package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.jaxrs.model.common.ProfileInfo;

public class HoldingJob extends AbstractJob {

  @NotNull
  private String userId;

  private boolean useReferenceLinks;

  private Map<String, String> references;

  public HoldingJob() {
    super();
    useReferenceLinks = false;
    references = new HashMap<String, String>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public boolean isUseReferenceLinks() {
    return useReferenceLinks;
  }

  public void setUseReferenceLinks(boolean useReferenceLinks) {
    this.useReferenceLinks = useReferenceLinks;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}