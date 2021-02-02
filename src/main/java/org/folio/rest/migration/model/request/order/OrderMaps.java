package org.folio.rest.migration.model.request.order;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class OrderMaps {

  @NotNull
  private Map<String, String> acqAddresses;

  @NotNull
  private Map<String, String> poLineAcqMethods;

  @NotNull
  private Map<String, String> poLineReceiptStatus;

  @NotNull
  private Map<String, Map<String, String>> location;

  @NotNull
  private Map<String, Map<String, String>> expenseClasses;

  @NotNull
  private Map<String, Map<String, String>> fundCodes;

  @NotNull
  private Map<String, String> vendorRefQual;

  public OrderMaps() {
    super();
    acqAddresses = new HashMap<>();
    poLineAcqMethods = new HashMap<>();
    poLineReceiptStatus = new HashMap<>();
    location = new HashMap<>();
    expenseClasses = new HashMap<>();
    fundCodes = new HashMap<>();
    vendorRefQual = new HashMap<>();
  }

  public Map<String, String> getAcqAddresses() {
    return acqAddresses;
  }

  public void setAcqAddresses(Map<String, String> acqAddresses) {
    this.acqAddresses = acqAddresses;
  }

  public Map<String, String> getPoLineAcqMethods() {
    return poLineAcqMethods;
  }

  public void setPoLineAcqMethods(Map<String, String> poLineAcqMethods) {
    this.poLineAcqMethods = poLineAcqMethods;
  }

  public Map<String, String> getPoLineReceiptStatus() {
    return poLineReceiptStatus;
  }

  public void setPoLineReceiptStatus(Map<String, String> poLineReceiptStatus) {
    this.poLineReceiptStatus = poLineReceiptStatus;
  }

  public Map<String, Map<String, String>> getLocation() {
    return location;
  }

  public void setLocation(Map<String, Map<String, String>> location) {
    this.location = location;
  }

  public Map<String, Map<String, String>> getExpenseClasses() {
    return expenseClasses;
  }

  public void setExpenseClasses(Map<String, Map<String, String>> expenseClasses) {
    this.expenseClasses = expenseClasses;
  }

  public Map<String, Map<String, String>> getFundCodes() {
    return fundCodes;
  }

  public void setFundCodes(Map<String, Map<String, String>> fundCodes) {
    this.fundCodes = fundCodes;
  }

  public Map<String, String> getVendorRefQual() {
    return vendorRefQual;
  }

  public void setVendorRefQual(Map<String, String> vendorRefQual) {
    this.vendorRefQual = vendorRefQual;
  }

}
