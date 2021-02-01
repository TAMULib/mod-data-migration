package org.folio.rest.migration.model.request.order;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class OrderExtraction extends AbstractExtraction {

  @NotNull
  private String lineItemNotesSql;

  @NotNull
  private String poLinesSql;

  @NotNull
  private String locationSql;

  public OrderExtraction() {
    super();
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

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

}
