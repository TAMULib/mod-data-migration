package org.folio.rest.migration.model.request.bib;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class BibMaps {

  @NotNull
  private Map<String, String> statisticalCode;

  public BibMaps() {
    statisticalCode = new HashMap<>();
  }

  public Map<String, String> getStatisticalCode() {
    return statisticalCode;
  }

  public void setStatisticalCode(Map<String, String> statisticalCode) {
    this.statisticalCode = statisticalCode;
  }

}
