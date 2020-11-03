package org.folio.rest.migration.model.request.boundwith;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class BoundWithJob extends AbstractJob {

  private Map<String, String> references;

  @NotNull
  private String statusId;

  @NotNull
  private String instanceTypeId;

  @NotNull
  private String modeOfIssuanceId;

  @NotNull
  private String instanceRelationshipTypeId;

  @NotNull
  private String holdingsTypeId;

  public BoundWithJob() {
    super();
    references = new HashMap<String, String>();
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public String getStatusId() {
    return statusId;
  }

  public void setStatusId(String statusId) {
    this.statusId = statusId;
  }

  public String getInstanceTypeId() {
    return instanceTypeId;
  }

  public void setInstanceTypeId(String instanceTypeId) {
    this.instanceTypeId = instanceTypeId;
  }

  public String getModeOfIssuanceId() {
    return modeOfIssuanceId;
  }

  public void setModeOfIssuanceId(String modeOfIssuanceId) {
    this.modeOfIssuanceId = modeOfIssuanceId;
  }

  public String getInstanceRelationshipTypeId() {
    return instanceRelationshipTypeId;
  }

  public void setInstanceRelationshipTypeId(String instanceRelationshipTypeId) {
    this.instanceRelationshipTypeId = instanceRelationshipTypeId;
  }

  public String getHoldingsTypeId() {
    return holdingsTypeId;
  }

  public void setHoldingsTypeId(String holdingsTypeId) {
    this.holdingsTypeId = holdingsTypeId;
  }

}
