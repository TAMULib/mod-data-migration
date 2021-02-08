package org.folio.rest.migration.model.request.order;

import javax.validation.constraints.NotNull;

public class OrderDefaults {

  @NotNull
  private String aqcAddressCode;

  @NotNull
  private String vendorRefQual;

  public OrderDefaults() {

  }

  public String getAqcAddressCode() {
    return aqcAddressCode;
  }

  public void setAqcAddressCode(String aqcAddressCode) {
    this.aqcAddressCode = aqcAddressCode;
  }

  public String getVendorRefQual() {
    return vendorRefQual;
  }

  public void setVendorRefQual(String vendorRefQual) {
    this.vendorRefQual = vendorRefQual;
  }

}
