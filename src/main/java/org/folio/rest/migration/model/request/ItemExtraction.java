package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class ItemExtraction extends AbstractExtraction {

  @NotNull
  private String mfhdSql;

  @NotNull
  private String barcodeSql;

  @NotNull
  private String itemTypeSql;

  @NotNull
  private String locationSql;

  @NotNull
  private String itemStatusSql;

  @NotNull
  private String noteSql;

  @NotNull
  private String materialTypeSql;

  public ItemExtraction() {
    super();
  }

  public String getMfhdSql() {
    return mfhdSql;
  }

  public void setMfhdSql(String mfhdSql) {
    this.mfhdSql = mfhdSql;
  }

  public String getBarcodeSql() {
    return barcodeSql;
  }

  public void setBarcodeSql(String barcodeSql) {
    this.barcodeSql = barcodeSql;
  }

  public String getItemTypeSql() {
    return itemTypeSql;
  }

  public void setItemTypeSql(String itemTypeSql) {
    this.itemTypeSql = itemTypeSql;
  }

  public String getLocationSql() {
    return locationSql;
  }

  public void setLocationSql(String locationSql) {
    this.locationSql = locationSql;
  }

  public String getItemStatusSql() {
    return itemStatusSql;
  }

  public void setItemStatusSql(String itemStatusSql) {
    this.itemStatusSql = itemStatusSql;
  }

  public String getNoteSql() {
    return noteSql;
  }

  public void setNoteSql(String noteSql) {
    this.noteSql = noteSql;
  }

  public String getMaterialTypeSql() {
    return materialTypeSql;
  }

  public void setMaterialTypeSql(String materialTypeSql) {
    this.materialTypeSql = materialTypeSql;
  }

}
