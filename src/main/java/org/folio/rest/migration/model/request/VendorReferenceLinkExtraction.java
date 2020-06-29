package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class VendorReferenceLinkExtraction extends AbstractExtraction {

  @NotNull
  private String vendorSql;

  public VendorReferenceLinkExtraction() {
    super();
  }

  public String getVendorSql() {
    return vendorSql;
  }

  public void setVendorSql(String vendorSql) {
    this.vendorSql = vendorSql;
  }

}