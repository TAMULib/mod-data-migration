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
  private String piecesSql;

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

  public String getPiecesSql() {
    return piecesSql;
  }

  public void setPiecesSql(String piecesSql) {
    this.piecesSql = piecesSql;
  }

}
