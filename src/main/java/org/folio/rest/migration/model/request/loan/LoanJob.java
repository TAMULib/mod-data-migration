package org.folio.rest.migration.model.request.loan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class LoanJob extends AbstractJob {

  @NotNull
  private Map<String, String> references;

  private List<String> userExternalReferenceTypeIds;

  public LoanJob() {
    this.references = new HashMap<>();
    this.userExternalReferenceTypeIds = new ArrayList<>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public List<String> getUserExternalReferenceTypeIds() {
    return userExternalReferenceTypeIds;
  }

  public void setUserReferenceTypeIds(List<String> userExternalReferenceTypeIds) {
    this.userExternalReferenceTypeIds = userExternalReferenceTypeIds;
  }

}
