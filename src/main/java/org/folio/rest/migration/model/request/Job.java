package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.generated.common.ProfileInfo;

public class Job {

  @NotNull
  private String schema;

  @NotNull
  private int partitions;

  @NotNull
  private String userId;

  @NotNull
  private ProfileInfo profileInfo;

  private boolean useReferenceLinks;

  private Map<String, String> references;

  public Job() {
    useReferenceLinks = false;
    references = new HashMap<String, String>();
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public int getPartitions() {
    return partitions;
  }

  public void setPartitions(int partitions) {
    this.partitions = partitions;
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