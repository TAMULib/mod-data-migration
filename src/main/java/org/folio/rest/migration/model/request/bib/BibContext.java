package org.folio.rest.migration.model.request.bib;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class BibContext extends AbstractContext {

  @NotNull
  private BibExtraction extraction;

  private List<BibJob> jobs;

  public BibContext() {
    super();
  }

  public BibExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(BibExtraction extraction) {
    this.extraction = extraction;
  }

  public List<BibJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<BibJob> jobs) {
    this.jobs = jobs;
  }

}
