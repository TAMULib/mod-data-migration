package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.generated.common.ProfileInfo;

public class BibJob extends AbstractJob {

  @NotNull
  private String userId;

  @NotNull
  private ProfileInfo profileInfo;

  private boolean useReferenceLinks;

  private Map<String, String> references;

  public BibJob() {
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

  public ProfileInfo getProfileInfo() {
    return profileInfo;
  }

  public void setProfileInfo(ProfileInfo profileInfo) {
    this.profileInfo = profileInfo;
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