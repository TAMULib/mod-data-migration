package org.folio.rest.migration.model.request.vendor;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class VendorMaps {

  @NotNull
  private Map<String, String> categories;

  @NotNull
  private Map<String, String> countryCodes;

  @NotNull
  private Map<String, String> vendorTypes;

  public VendorMaps() {
    categories = new HashMap<>();
    countryCodes = new HashMap<>();
    vendorTypes = new HashMap<>();
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

  public Map<String, String> getVendorTypes() {
    return vendorTypes;
  }

  public void setVendorTypes(Map<String, String> vendorTypes) {
    this.vendorTypes = vendorTypes;
  }

}
