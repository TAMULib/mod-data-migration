package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class BibExtraction extends AbstractExtraction {

  @NotNull
  private String marcSql;

  @NotNull
  private String bibHistorySql;

  public BibExtraction() {
    super();
  }

  public String getMarcSql() {
    return marcSql;
  }

  public void setMarcSql(String marcSql) {
    this.marcSql = marcSql;
  }

  public String getBibHistorySql() {
    return marcSql;
  }

  public void setBibHistorySql(String marcSql) {
    this.marcSql = marcSql;
  }

}