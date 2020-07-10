package org.folio.rest.migration.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

public class InventoryReferenceLinkContext extends AbstractContext {

  @NotNull
  private InventoryReferenceLinkExtraction extraction;

  private List<InventoryReferenceLinkJob> jobs;

  public InventoryReferenceLinkContext() {
    super();
  }

  public InventoryReferenceLinkExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(InventoryReferenceLinkExtraction extraction) {
    this.extraction = extraction;
  }

  public List<InventoryReferenceLinkJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<InventoryReferenceLinkJob> jobs) {
    this.jobs = jobs;
  }

}
