package org.folio.rest.migration.model.request.order;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.model.request.AbstractJob;

public class OrderJob extends AbstractJob {

  @NotNull
  private String columns;

  @NotNull
  private String tables;

  @NotNull
  private String conditions;

  @NotNull
  private String poNumberPrefix;

  @NotNull
  private Boolean includeAddresses;

  private Map<String, String> references;

  public OrderJob() {
    super();
    references = new HashMap<>();
  }

  public String getColumns() {
    return columns;
  }

  public void setColumns(String columns) {
    this.columns = columns;
  }

  public String getTables() {
    return tables;
  }

  public void setTables(String tables) {
    this.tables = tables;
  }

  public String getConditions() {
    return conditions;
  }

  public void setConditions(String conditions) {
    this.conditions = conditions;
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

}
