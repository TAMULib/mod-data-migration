package org.folio.rest.migration.model.request.purchaseorder;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class PurchaseOrderContext extends AbstractContext {

  @NotNull
  private PurchaseOrderExtraction extraction;

  private List<PurchaseOrderJob> jobs;

  @NotNull
  private PurchaseOrderMaps maps;

  @NotNull
  private PurchaseOrderDefaults defaults;

  public PurchaseOrderContext() {
    super();
    jobs = new ArrayList<>();
  }

  public PurchaseOrderExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(PurchaseOrderExtraction extraction) {
    this.extraction = extraction;
  }

  public List<PurchaseOrderJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<PurchaseOrderJob> jobs) {
    this.jobs = jobs;
  }

  public PurchaseOrderMaps getMaps() {
    return maps;
  }

  public void setMaps(PurchaseOrderMaps maps) {
    this.maps = maps;
  }

  public PurchaseOrderDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(PurchaseOrderDefaults defaults) {
    this.defaults = defaults;
  }

}
