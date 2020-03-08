package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class InventoryReferenceLinkExtraction extends AbstractExtraction {

  @NotNull
  private String holdingSql;

  @NotNull
  private String itemSql;

  public InventoryReferenceLinkExtraction() {
    super();
  }

  public String getHoldingSql() {
    return holdingSql;
  }

  public void setHoldingSql(String holdingSql) {
    this.holdingSql = holdingSql;
  }

  public String getItemSql() {
    return itemSql;
  }

  public void setItemSql(String itemSql) {
    this.itemSql = itemSql;
  }

}