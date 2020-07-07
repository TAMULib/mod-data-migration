package org.folio.rest.migration.model.request;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class HoldingMaps {

  @NotNull
  private Map<String, String> location;

  @NotNull
  private Map<String, String> acqMethod;

  @NotNull
  private Map<String, String> callNumberType;

  @NotNull
  private Map<String, String> holdingsType;

  @NotNull
  private Map<String, String> receiptStatus;

  @NotNull
  private Map<String, String> retentionPolicy;

  public HoldingMaps() {
    location = new HashMap<String, String>();
    acqMethod = new HashMap<String, String>();
    callNumberType = new HashMap<String, String>();
    holdingsType = new HashMap<String, String>();
    receiptStatus = new HashMap<String, String>();
    retentionPolicy = new HashMap<String, String>();
  }

  public Map<String, String> getLocation() {
    return location;
  }

  public void setLocation(Map<String, String> location) {
    this.location = location;
  }

  public Map<String, String> getAcqMethod() {
    return acqMethod;
  }

  public void setAcqMethod(Map<String, String> acqMethod) {
    this.acqMethod = acqMethod;
  }

  public Map<String, String> getCallNumberType() {
    return callNumberType;
  }

  public void setCallNumberType(Map<String, String> callNumberType) {
    this.callNumberType = callNumberType;
  }

  public Map<String, String> getHoldingsType() {
    return holdingsType;
  }

  public void setHoldingsType(Map<String, String> holdingsType) {
    this.holdingsType = holdingsType;
  }

  public Map<String, String> getReceiptStatus() {
    return receiptStatus;
  }

  public void setReceiptStatus(Map<String, String> receiptStatus) {
    this.receiptStatus = receiptStatus;
  }

  public Map<String, String> getRetentionPolicy() {
    return retentionPolicy;
  }

  public void setRetentionPolicy(Map<String, String> retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

}