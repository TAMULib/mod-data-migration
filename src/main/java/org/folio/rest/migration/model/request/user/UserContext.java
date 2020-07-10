package org.folio.rest.migration.model.request.user;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class UserContext extends AbstractContext {

  @NotNull
  private UserExtraction extraction;

  private List<UserJob> jobs;

  @NotNull
  private UserMaps maps;

  @NotNull
  private UserDefaults defaults;

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

  public UserMaps getMaps() {
    return maps;
  }

  public void setMaps(UserMaps maps) {
    this.maps = maps;
  }

  public UserDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(UserDefaults defaults) {
    this.defaults = defaults;
  }

}
