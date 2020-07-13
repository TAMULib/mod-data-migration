package org.folio.rest.migration.model.request.vendor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class VendorMaps {

  @NotNull
  private Map<String, String> categories;

  @NotNull
  private Map<String, String> countryCodes;

  @NotNull
  private Map<String, List<String>> ignore;

  public VendorMaps() {
    categories = new HashMap<String, String>();
  }

  public Map<String, String> getCategories() {
    return categories;
  }

  public void setCategories(Map<String, String> categories) {
    this.categories = categories;
  }

  public Map<String, String> getCountryCodes() {
    return countryCodes;
  }

  public void setCountryCodes(Map<String, String> countryCodes) {
    this.countryCodes = countryCodes;
  }

  public Map<String, List<String>> getIgnore() {
    return ignore;
  }

  public void setIgnore(Map<String, List<String>> ignore) {
    this.ignore = ignore;
  }

}