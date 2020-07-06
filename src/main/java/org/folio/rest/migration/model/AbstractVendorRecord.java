package org.folio.rest.migration.model;

import org.folio.rest.migration.model.request.VendorDefaults;
import org.folio.rest.migration.model.request.VendorMaps;

public class AbstractVendorRecord {

  protected VendorMaps maps;
  protected VendorDefaults defaults;

  public VendorMaps getMaps() {
    return maps;
  }

  public void setMaps(VendorMaps vendorMaps) {
    this.maps = vendorMaps;
  }

  public VendorDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(VendorDefaults vendorDefaults) {
    this.defaults = vendorDefaults;
  }

}