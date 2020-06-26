package org.folio.rest.migration.mapping.model;

import java.util.ArrayList;
import java.util.List;

public class MappingRule {

  private List<RuleCondition> conditions;

  private String description;

  private String value;

  public MappingRule() {
    conditions = new ArrayList<RuleCondition>();
  }

  public List<RuleCondition> getConditions() {
    return conditions;
  }

  public void setConditions(List<RuleCondition> conditions) {
    this.conditions = conditions;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}