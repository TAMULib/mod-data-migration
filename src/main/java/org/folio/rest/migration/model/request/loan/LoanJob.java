package org.folio.rest.migration.model.request.loan;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class LoanJob extends AbstractJob {

  @NotNull
  private Map<String, String> references;

  private Map<String, String> alternativeExternalReferenceTypeIds;

  public LoanJob() {
    this.references = new HashMap<>();
    this.alternativeExternalReferenceTypeIds = new HashMap<>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public Map<String, String> getAlternativeExternalReferenceTypeIds() {
    return alternativeExternalReferenceTypeIds;
  }

  public void setAlternativeExternalReferenceTypeIds(Map<String, String> alternativeExternalReferenceTypeIds) {
    this.alternativeExternalReferenceTypeIds = alternativeExternalReferenceTypeIds;
  }

}
