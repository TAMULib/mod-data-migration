package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class Extraction {

  @NotNull
  private String count;

  @NotNull
  private String page;

  @NotNull
  private String additional;

  public Extraction() {

  }

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public String getAdditional() {
    return additional;
  }

  public void setAdditional(String additional) {
    this.additional = additional;
  }

}