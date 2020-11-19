package org.folio.rest.migration.model.request.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class RequestContext extends AbstractContext {

  @NotNull
  private RequestExtraction extraction;

  private List<RequestJob> jobs;

  @NotNull
  private RequestMaps maps;

  public RequestContext() {
    super();
    jobs = new ArrayList<>();
  }

  public RequestExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(RequestExtraction extraction) {
    this.extraction = extraction;
  }

  public List<RequestJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<RequestJob> jobs) {
    this.jobs = jobs;
  }

  public RequestMaps getMaps() {
    return maps;
  }

  public void setMaps(RequestMaps maps) {
    this.maps = maps;
  }

}
