package org.folio.rest.migration.model.request.user;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class UserJob extends AbstractJob {

  @NotNull
  private String decodeSql;

  @NotNull
  private String user;

  @NotNull
  private String dbCode;

  @NotNull
  private String noteTypeId;

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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getDbCode() {
    return dbCode;
  }

  public void setDbCode(String dbCode) {
    this.dbCode = dbCode;
  }

  public String getNoteTypeId() {
    return noteTypeId;
  }

  public void setNoteTypeId(String noteTypeId) {
    this.noteTypeId = noteTypeId;
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
