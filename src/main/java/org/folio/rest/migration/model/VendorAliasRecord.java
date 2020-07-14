package org.folio.rest.migration.model;

import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Alias;

public class VendorAliasRecord {

  private final String altVendorName;

  public VendorAliasRecord(String altVendorName) {
    this.altVendorName = altVendorName;
  }

  public String getAltVendorName() {
    return altVendorName;
  }

  public Alias toAlias() {
    final Alias alias = new Alias();

    alias.setValue(altVendorName);

    return alias;
  }

}
