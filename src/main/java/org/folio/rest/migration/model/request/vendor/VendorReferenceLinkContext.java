package org.folio.rest.migration.model.request.vendor;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class VendorReferenceLinkContext extends AbstractContext {

  @NotNull
  private VendorReferenceLinkExtraction extraction;

  private List<VendorReferenceLinkJob> jobs;

  public VendorReferenceLinkContext() {
    super();
  }

  public VendorReferenceLinkExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(VendorReferenceLinkExtraction extraction) {
    this.extraction = extraction;
  }

  public List<VendorReferenceLinkJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<VendorReferenceLinkJob> jobs) {
    this.jobs = jobs;
  }

}
