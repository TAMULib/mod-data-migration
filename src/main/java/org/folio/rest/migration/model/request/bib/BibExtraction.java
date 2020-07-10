package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class BibExtraction extends AbstractExtraction {

  @NotNull
  private String marcSql;

  public BibExtraction() {
    super();
  }

  public String getMarcSql() {
    return marcSql;
  }

  public void setMarcSql(String marcSql) {
    this.marcSql = marcSql;
  }

}