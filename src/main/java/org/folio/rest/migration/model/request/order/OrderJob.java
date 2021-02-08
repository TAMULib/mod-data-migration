package org.folio.rest.migration.model.request.order;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class OrderJob extends AbstractJob {

  @NotNull
  private Map<String, String> pageAdditionalContext;

  @NotNull
  private String poNumberPrefix;

  @NotNull
  private Boolean includeAddresses;

  @NotNull
  private Map<String, String> references;

  @NotNull
  private Map<String, String> poLinesAdditionalContext;

  @NotNull
  private String productIdType;

  private String defaultLocationId;

  public OrderJob() {
    super();
    pageAdditionalContext = new HashMap<>();
    references = new HashMap<>();
    poLinesAdditionalContext = new HashMap<>();
  }

  public Map<String, String> getPageAdditionalContext() {
    return pageAdditionalContext;
  }

  public void setPageAdditionalContext(Map<String, String> pageAdditionalContext) {
    this.pageAdditionalContext = pageAdditionalContext;
  }

  public String getPoNumberPrefix() {
    return poNumberPrefix;
  }

  public void setPoNumberPrefix(String poNumberPrefix) {
    this.poNumberPrefix = poNumberPrefix;
  }

  public Boolean getIncludeAddresses() {
    return includeAddresses;
  }

  public void setIncludeAddresses(Boolean includeAddresses) {
    this.includeAddresses = includeAddresses;
  }

  public Map<String, String> getReferences() {
    return references;
  }

  public void setReferences(Map<String, String> references) {
    this.references = references;
  }

  public Map<String, String> getPoLinesAdditionalContext() {
    return poLinesAdditionalContext;
  }

  public void setPoLinesAdditionalContext(Map<String, String> poLinesAdditionalContext) {
    this.poLinesAdditionalContext = poLinesAdditionalContext;
  }

  public String getProductIdType() {
    return productIdType;
  }

  public void setProductIdType(String productIdType) {
    this.productIdType = productIdType;
  }

  public String getDefaultLocationId() {
    return defaultLocationId;
  }

  public void setDefaultLocationId(String defaultLocationId) {
    this.defaultLocationId = defaultLocationId;
  }

}
