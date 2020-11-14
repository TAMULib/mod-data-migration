package org.folio.rest.migration.model.request.holdings;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class HoldingsMaps {

  @NotNull
  private Map<String, Map<String, String>> location;

  @NotNull
  private Map<String, String> acqMethod;

  @NotNull
  private Map<String, String> callNumberType;

  @NotNull
  private Map<String, String> holdingsType;

  @NotNull
  private Map<String, String> holdingsNotesType;

  @NotNull
  private Map<String, String> receiptStatus;

  @NotNull
  private Map<String, String> retentionPolicy;

  @NotNull
  private Map<String, String> fieldRegexExclusion;

  public HoldingsMaps() {
    location = new HashMap<>();
    acqMethod = new HashMap<>();
    callNumberType = new HashMap<>();
    holdingsType = new HashMap<>();
    holdingsNotesType = new HashMap<>();
    receiptStatus = new HashMap<>();
    retentionPolicy = new HashMap<>();
    fieldRegexExclusion = new HashMap<>();
  }

  public Map<String, Map<String, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<String, String>> location) {
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

  public Map<String, String> getHoldingsNotesType() {
    return holdingsNotesType;
  }

  public void setHoldingsNotesType(Map<String, String> holdingsNotesType) {
    this.holdingsNotesType = holdingsNotesType;
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

  public Map<String, String> getFieldRegexExclusion() {
    return fieldRegexExclusion;
  }

  public void setFieldRegexExclusion(Map<String, String> fieldRegexExclusion) {
    this.fieldRegexExclusion = fieldRegexExclusion;
  }

}
