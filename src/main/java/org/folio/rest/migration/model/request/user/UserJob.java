package org.folio.rest.migration.model.request.user;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class UserJob extends AbstractJob {

  @NotNull
  private String decodeSql;

  @NotNull
  private String userId;

  @NotNull
  private Boolean skipDuplicates;

  private Map<String, String> references;

  public UserJob() {
    super();
    references = new HashMap<String, String>();
  }

  public String getDecodeSql() {
    return decodeSql;
  }

  public void setDecodeSql(String decodeSql) {
    this.decodeSql = decodeSql;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Boolean getSkipDuplicates() {
    return skipDuplicates;
  }

  public void setSkipDuplicates(Boolean skipDuplicates) {
    this.skipDuplicates = skipDuplicates;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

}