package org.folio.rest.migration.model.request.order;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

public class OrderExtraction extends AbstractExtraction {

  @NotNull
  private String lineItemNotesSql;

  public OrderExtraction() {
    super();
  }

  public String getLineItemNotesSql() {
    return lineItemNotesSql;
  }

  public void setLineItemNotesSql(String lineItemNotesSql) {
    this.lineItemNotesSql = lineItemNotesSql;
  }

}
