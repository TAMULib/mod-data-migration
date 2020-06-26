package org.folio.rest.migration.mapping.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RuleCondition {

  @NotNull
  private String type;

  @NotNull
  private RuleParameter parameter;

  private String value;

  @JsonAlias("LDR")
  private boolean ldr;

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

  public boolean getLdr() {
    return ldr;
  }

  public void setLdr(boolean ldr) {
    this.ldr = ldr;
  }

}
