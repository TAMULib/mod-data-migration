package org.folio.rest.migration.model.request.boundwith;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class BoundWithContext extends AbstractContext {

  @NotNull
  private BoundWithExtraction extraction;

  private List<BoundWithJob> jobs;

  public BoundWithContext() {
    super();
    jobs = new ArrayList<>();
  }

  public BoundWithExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(BoundWithExtraction extraction) {
    this.extraction = extraction;
  }

  public List<BoundWithJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<BoundWithJob> jobs) {
    this.jobs = jobs;
  }

}
