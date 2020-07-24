package org.folio.rest.migration.model.request.loan;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class LoanContext extends AbstractContext {

  @NotNull
  private LoanExtraction extraction;

  private List<LoanJob> jobs;

  public LoanContext() {
    super();
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

}
