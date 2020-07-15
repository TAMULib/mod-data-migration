package org.folio.rest.migration.model.request.holding;

public class HoldingDefaults {

  private String acqMethod;

  private String callNumberTypeId;

  private Boolean discoverySuppress;

  private String holdingsTypeId;

  private String permanentLocationId;

  private String receiptStatus;

  private String retentionPolicy;

  public HoldingDefaults() { }

  public String getAcqMethod() {
    return acqMethod;
  }

  public void setAcqMethod(String acqMethod) {
    this.acqMethod = acqMethod;
  }

  public Boolean getDiscoverySuppress() {
    return discoverySuppress;
  }

  public void setDiscoverySuppress(Boolean discoverySuppress) {
    this.discoverySuppress = discoverySuppress;
  }

  public String getCallNumberTypeId() {
    return callNumberTypeId;
  }

  public void setCallNumberTypeId(String callNumber) {
    this.callNumberTypeId = callNumber;
  }

  public String getHoldingsTypeId() {
    return holdingsTypeId;
  }

  public void setHoldingsTypeId(String holdingsType) {
    this.holdingsTypeId = holdingsType;
  }

  public String getPermanentLocationId() {
    return permanentLocationId;
  }

  public void setPermanentLocationId(String permanentLocationId) {
    this.permanentLocationId = permanentLocationId;
  }

  public String getReceiptStatus() {
    return receiptStatus;
  }

  public void setReceiptStatus(String receiptStatus) {
    this.receiptStatus = receiptStatus;
  }

  public String getRetentionPolicy() {
    return retentionPolicy;
  }

  public void setRetentionPolicy(String retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

}
