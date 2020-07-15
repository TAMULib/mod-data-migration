package org.folio.rest.migration.model.request.bib;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractExtraction;

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
