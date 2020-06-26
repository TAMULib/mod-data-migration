package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class HoldingContext extends AbstractContext {

  @NotNull
  private HoldingExtraction extraction;

  private List<HoldingJob> jobs;

  @NotNull
  private HoldingMaps maps;

  @NotNull
  private HoldingDefaults defaults;

  public HoldingContext() {
    super();
  }

  public HoldingExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(HoldingExtraction extraction) {
    this.extraction = extraction;
  }

  public List<HoldingJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<HoldingJob> jobs) {
    this.jobs = jobs;
  }

  public HoldingMaps getHoldingMaps() {
    return maps;
  }

  public void setHoldingMaps(HoldingMaps maps) {
    this.maps = maps;
  }

  public HoldingDefaults getHoldingDefaults() {
    return defaults;
  }

  public void setHoldingDefaults(HoldingDefaults defaults) {
    this.defaults = defaults;
  }

}
