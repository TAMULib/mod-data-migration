package org.folio.rest.migration.model.request.loan;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class LoanJob extends AbstractJob {

  @NotNull
  private Map<String, String> references;

  public LoanJob() {
    references = new HashMap<>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
