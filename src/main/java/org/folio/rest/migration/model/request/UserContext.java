package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class UserContext extends AbstractContext {

  @NotNull
  private UserExtraction extraction;

  private List<UserJob> jobs;

  public UserContext() {
    super();
  }

  public UserExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(UserExtraction extraction) {
    this.extraction = extraction;
  }

  public List<UserJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<UserJob> jobs) {
    this.jobs = jobs;
  }

}
