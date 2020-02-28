package org.folio.rest.migration.mapping.model;

import javax.validation.constraints.NotNull;

public class RuleCondition {

  @NotNull
  private String type;

  @NotNull
  private RuleParameter parameter;

  private String value;

  private String LDR;

  public RuleCondition() {

  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public RuleParameter getParameter() {
    return parameter;
  }

  public void setParameter(RuleParameter parameter) {
    this.parameter = parameter;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLDR() {
    return LDR;
  }

  public void setLDR(String lDR) {
    LDR = lDR;
  }

}
