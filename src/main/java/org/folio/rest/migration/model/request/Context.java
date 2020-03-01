package org.folio.rest.migration.model.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

public class Context {

  @NotNull
  private Extraction extraction;

  @NotNull
  private int parallelism;

  private List<Job> jobs;

  public Context() {
    jobs = new ArrayList<Job>();
  }

  public Extraction getExtraction() {
    return extraction;
  }

  public void setExtraction(Extraction extraction) {
    this.extraction = extraction;
  }

  public int getParallelism() {
    return parallelism;
  }

  public void setParallelism(int parallelism) {
    this.parallelism = parallelism;
  }

  public List<Job> getJobs() {
    return jobs;
  }

  public void setJobs(List<Job> jobs) {
    this.jobs = jobs;
  }

}
