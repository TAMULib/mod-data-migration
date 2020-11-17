package org.folio.rest.migration.model.request.holdings;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class HoldingsContext extends AbstractContext {

  @NotNull
  private HoldingsExtraction extraction;

  private List<HoldingsJob> jobs;

  @NotNull
  private HoldingsMaps maps;

  @NotNull
  private HoldingsDefaults defaults;

  public HoldingsContext() {
    super();
    jobs = new ArrayList<>();
  }

  public HoldingsExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(HoldingsExtraction extraction) {
    this.extraction = extraction;
  }

  public List<HoldingsJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<HoldingsJob> jobs) {
    this.jobs = jobs;
  }

  public HoldingsMaps getMaps() {
    return maps;
  }

  public void setMaps(HoldingsMaps maps) {
    this.maps = maps;
  }

  public HoldingsDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(HoldingsDefaults defaults) {
    this.defaults = defaults;
  }

}
