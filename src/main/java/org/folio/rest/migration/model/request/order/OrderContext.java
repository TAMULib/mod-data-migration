package org.folio.rest.migration.model.request.order;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class OrderContext extends AbstractContext {

  @NotNull
  private OrderExtraction extraction;

  private List<OrderJob> jobs;

  @NotNull
  private OrderMaps maps;

  public OrderContext() {
    super();
    jobs = new ArrayList<>();
  }

  public OrderExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(OrderExtraction extraction) {
    this.extraction = extraction;
  }

  public List<OrderJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<OrderJob> jobs) {
    this.jobs = jobs;
  }

  public OrderMaps getMaps() {
    return maps;
  }

  public void setMaps(OrderMaps maps) {
    this.maps = maps;
  }

}
