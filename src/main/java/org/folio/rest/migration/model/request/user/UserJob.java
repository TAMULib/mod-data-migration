package org.folio.rest.migration.model.request.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  private String noteWhereClause;

  @NotNull
  private String noteTypeId;

  @NotNull
  private Boolean skipDuplicates;

  private List<String> userReferenceTypeIds;

  private List<String> barcodeReferenceTypeIds;

  private Map<String, String> alternativeExternalReferenceTypeIds;

  public UserJob() {
    super();
    this.userReferenceTypeIds = new ArrayList<>();
    this.barcodeReferenceTypeIds = new ArrayList<>();
    this.alternativeExternalReferenceTypeIds = new HashMap<>();
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

  public String getNoteWhereClause() {
    return noteWhereClause;
  }

  public void setNoteWhereClause(String noteWhereClause) {
    this.noteWhereClause = noteWhereClause;
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

  public List<String> getUserReferenceTypeIds() {
    return userReferenceTypeIds;
  }

  public void setUserReferenceTypeIds(List<String> userReferenceTypeIds) {
    this.userReferenceTypeIds = userReferenceTypeIds;
  }

  public List<String> getBarcodeReferenceTypeIds() {
    return barcodeReferenceTypeIds;
  }

  public void setBarcodeReferenceTypeIds(List<String> barcodeReferenceTypeIds) {
    this.barcodeReferenceTypeIds = barcodeReferenceTypeIds;
  }

  public Map<String, String> getAlternativeExternalReferenceTypeIds() {
    return alternativeExternalReferenceTypeIds;
  }

  public void setAlternativeExternalReferenceTypeIds(Map<String, String> alternativeExternalReferenceTypeIds) {
    this.alternativeExternalReferenceTypeIds = alternativeExternalReferenceTypeIds;
  }

}
