package org.folio.rest.migration.model.request.purchaseorder;

import javax.validation.constraints.NotNull;

public class PurchaseOrderDefaults {

  @NotNull
  private String aqcAddressCode;

  @NotNull
  private String vendorRefQual;

  public PurchaseOrderDefaults() {

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
