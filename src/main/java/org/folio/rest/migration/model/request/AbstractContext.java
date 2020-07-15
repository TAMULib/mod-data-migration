package org.folio.rest.migration.model.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

public abstract class AbstractContext {

  @NotNull
  private int parallelism;

  private List<String> preActions;

  private List<String> postActions;

  public AbstractContext() {
    preActions = new ArrayList<>();
    postActions = new ArrayList<>();
  }

  public int getParallelism() {
    return parallelism;
  }

  public void setParallelism(int parallelism) {
    this.parallelism = parallelism;
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
