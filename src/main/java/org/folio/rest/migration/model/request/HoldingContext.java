package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class HoldingContext extends AbstractContext {

  @NotNull
  private HoldingExtraction extraction;

  private List<HoldingJob> jobs;

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

}
