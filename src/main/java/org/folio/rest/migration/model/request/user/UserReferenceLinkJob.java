package org.folio.rest.migration.model.request.user;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class UserReferenceLinkJob extends AbstractJob {

  @NotNull
  private String decodeSql;

  private Map<String, String> references;

  public UserReferenceLinkJob() {
    super();
    references = new HashMap<String, String>();
  }

  public String getDecodeSql() {
    return decodeSql;
  }

  public void setDecodeSql(String decodeSql) {
    this.decodeSql = decodeSql;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}
