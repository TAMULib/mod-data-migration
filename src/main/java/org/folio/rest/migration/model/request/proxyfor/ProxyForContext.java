package org.folio.rest.migration.model.request.proxyfor;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class ProxyForContext extends AbstractContext {

  @NotNull
  private ProxyForExtraction extraction;

  private List<ProxyForJob> jobs;

  public ProxyForContext() {
    super();
    jobs = new ArrayList<>();
  }

  public ProxyForExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(ProxyForExtraction extraction) {
    this.extraction = extraction;
  }

  public List<ProxyForJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<ProxyForJob> jobs) {
    this.jobs = jobs;
  }

}
