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

  private List<String> preActions;

  private List<String> postActions;

  public Context() {
    jobs = new ArrayList<Job>();
    preActions = new ArrayList<String>();
    postActions = new ArrayList<String>();
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

  public List<String> getPreActions() {
    return preActions;
  }

  public void setPreActions(List<String> preActions) {
    this.preActions = preActions;
  }

  public List<String> getPostActions() {
    return postActions;
  }

  public void setPostActions(List<String> postActions) {
    this.postActions = postActions;
  }

}
