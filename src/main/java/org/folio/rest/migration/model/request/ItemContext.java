package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class ItemContext extends AbstractContext {

  @NotNull
  private ItemExtraction extraction;

  private List<ItemJob> jobs;

  public ItemContext() {
    super();
  }

  public ItemExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(ItemExtraction extraction) {
    this.extraction = extraction;
  }

  public List<ItemJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<ItemJob> jobs) {
    this.jobs = jobs;
  }

}
