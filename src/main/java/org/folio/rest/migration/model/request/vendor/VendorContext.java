package org.folio.rest.migration.model.request.vendor;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class VendorContext extends AbstractContext {

  @NotNull
  private VendorExtraction extraction;

  private List<VendorJob> jobs;

  @NotNull
  private VendorMaps maps;

  @NotNull
  private VendorDefaults defaults;

  public VendorContext() {
    super();
    jobs = new ArrayList<>();
  }

  public VendorExtraction getExtraction() {
    return extraction;
  }

  public void setExtraction(VendorExtraction extraction) {
    this.extraction = extraction;
  }

  public List<VendorJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<VendorJob> jobs) {
    this.jobs = jobs;
  }

  public VendorMaps getMaps() {
    return maps;
  }

  public void setMaps(VendorMaps maps) {
    this.maps = maps;
  }

  public VendorDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(VendorDefaults defaults) {
    this.defaults = defaults;
  }

}
