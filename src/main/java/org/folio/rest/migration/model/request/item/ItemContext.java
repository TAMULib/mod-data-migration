package org.folio.rest.migration.model.request.item;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractContext;

public class ItemContext extends AbstractContext {

  @NotNull
  private ItemExtraction extraction;

  private List<ItemJob> jobs;

  @NotNull
  private ItemMaps maps;

  @NotNull
  private ItemDefaults defaults;

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

  public ItemMaps getMaps() {
    return maps;
  }

  public void setMaps(ItemMaps maps) {
    this.maps = maps;
  }

  public ItemDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(ItemDefaults defaults) {
    this.defaults = defaults;
  }

}
