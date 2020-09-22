package org.folio.rest.migration.model.request.feefine;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class FeeFineContext extends AbstractContext {

  @NotNull
  private FeeFineExtraction extraction;

  private List<FeeFineJob> jobs;

  @NotNull
  private FeeFineMaps maps;

  @NotNull
  private FeeFineDefaults defaults;

  @NotNull
  private List<String> userIdRLTypeIds;

  public FeeFineContext() {
    super();
    jobs = new ArrayList<>();
    userIdRLTypeIds = new ArrayList<>();
  }

  public FeeFineExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(FeeFineExtraction extraction) {
    this.extraction = extraction;
  }

  public List<FeeFineJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<FeeFineJob> jobs) {
    this.jobs = jobs;
  }

  public FeeFineMaps getMaps() {
    return maps;
  }

  public void setMaps(FeeFineMaps maps) {
    this.maps = maps;
  }

  public FeeFineDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(FeeFineDefaults defaults) {
    this.defaults = defaults;
  }

  public List<String> getUserIdRLTypeIds() {
    return userIdRLTypeIds;
  }

  public void setUserIdRLTypeIds(List<String> userIdRLTypeIds) {
    this.userIdRLTypeIds = userIdRLTypeIds;
  }

}
