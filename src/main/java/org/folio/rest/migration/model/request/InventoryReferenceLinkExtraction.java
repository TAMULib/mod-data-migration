package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class InventoryReferenceLinkExtraction extends AbstractExtraction {

  @NotNull
  private String holdingIdsSql;

  @NotNull
  private String itemIdsSql;

  public InventoryReferenceLinkExtraction() {
    super();
  }

  public String getHoldingIdsSql() {
    return holdingIdsSql;
  }

  public void setHoldingIdsSql(String holdingIdsSql) {
    this.holdingIdsSql = holdingIdsSql;
  }

  public String getItemIdsSql() {
    return itemIdsSql;
  }

  public void setItemIdsSql(String itemIdsSql) {
    this.itemIdsSql = itemIdsSql;
  }

}