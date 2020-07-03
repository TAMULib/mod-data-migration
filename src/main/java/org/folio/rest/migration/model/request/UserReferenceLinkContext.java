package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class UserReferenceLinkContext extends AbstractContext {

  @NotNull
  private UserReferenceLinkExtraction extraction;

  private List<UserReferenceLinkJob> jobs;

  public UserReferenceLinkContext() {
    super();
  }

  public UserReferenceLinkExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(UserReferenceLinkExtraction extraction) {
    this.extraction = extraction;
  }

  public List<UserReferenceLinkJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<UserReferenceLinkJob> jobs) {
    this.jobs = jobs;
  }

}
