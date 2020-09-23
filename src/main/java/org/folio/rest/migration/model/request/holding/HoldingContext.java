package org.folio.rest.migration.model.request.holding;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

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
    jobs = new ArrayList<>();
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

  public HoldingMaps getMaps() {
    return maps;
  }

  public void setMaps(HoldingMaps maps) {
    this.maps = maps;
  }

  public HoldingDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(HoldingDefaults defaults) {
    this.defaults = defaults;
  }

}
