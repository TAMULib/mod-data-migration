package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.jaxrs.model.mod_data_import_converter_storage.JobProfile;

public class BibJob extends AbstractJob {

  @NotNull
  private String userId;

  @NotNull
  private String instanceStatusId;

  @NotNull
  private JobProfile profile;

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

  public String getInstanceStatusId() {
    return instanceStatusId;
  }

  public void setInstanceStatusId(String instanceStatusId) {
    this.instanceStatusId = instanceStatusId;
  }

  public JobProfile getProfile() {
    return profile;
  }

  public void setProfile(JobProfile profile) {
    this.profile = profile;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}