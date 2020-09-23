package org.folio.rest.migration.model.request.loan;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class LoanContext extends AbstractContext {

  @NotNull
  private LoanExtraction extraction;

  private List<LoanJob> jobs;

  @NotNull
  private LoanMaps maps;

  public LoanContext() {
    super();
    jobs = new ArrayList<>();
  }

  public LoanExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(LoanExtraction extraction) {
    this.extraction = extraction;
  }

  public List<LoanJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<LoanJob> jobs) {
    this.jobs = jobs;
  }

  public LoanMaps getMaps() {
    return maps;
  }

  public void setMaps(LoanMaps maps) {
    this.maps = maps;
  }

}
