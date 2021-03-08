package org.folio.rest.migration.model.request.purchaseorder;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class PurchaseOrderExtraction extends AbstractExtraction {

  @NotNull
  private String locationSql;

  @NotNull
  private String lineItemNotesSql;

  @NotNull
  private String poLinesSql;

  @NotNull
  private String receivingHistorySql;

  public PurchaseOrderExtraction() {
    super();
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

  public String getLineItemNotesSql() {
    return lineItemNotesSql;
  }

  public void setLineItemNotesSql(String lineItemNotesSql) {
    this.lineItemNotesSql = lineItemNotesSql;
  }

  public String getPoLinesSql() {
    return poLinesSql;
  }

  public void setPoLinesSql(String poLinesSql) {
    this.poLinesSql = poLinesSql;
  }

  public String getReceivingHistorySql() {
    return receivingHistorySql;
  }

  public void setReceivingHistorySql(String receivingHistorySql) {
    this.receivingHistorySql = receivingHistorySql;
  }

}
