package org.folio.rest.migration.mapping;

import java.util.List;

import org.folio.rest.migration.mapping.model.MappingField;
import org.folio.rest.migration.mapping.model.RuleParameter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class serves as context to store parameters for rule execution
 */
public class RuleExecutionContext {

  private final MappingParameters mappingParameters;

  private final ObjectNode instanceObject;

  private final Leader leader;

  private List<MappingField> mappingFields;

  private VariableField variableField;

  private RuleParameter ruleParameter;

  // TODO: refactor as it is also control field data
  private String subfieldValue;

  public RuleExecutionContext(MappingParameters mappingParameters, ObjectNode instanceObject, Leader leader) {
    this.mappingParameters = mappingParameters;
    this.instanceObject = instanceObject;
    this.leader = leader;
  }

  public RuleExecutionContext withMappingFields(List<MappingField> mappingFields) {
    this.mappingFields = mappingFields;
    return this;
  }

  public RuleExecutionContext withControlField(ControlField variableField) {
    this.variableField = variableField;
    return this;
  }

  public RuleExecutionContext withDataField(DataField variableField) {
    this.variableField = variableField;
    return this;
  }

  public RuleExecutionContext withSubfieldValue(String subfieldValue) {
    this.subfieldValue = subfieldValue;
    return this;
  }

  public List<MappingField> getMappingFields() {
    return mappingFields;
  }

  public ControlField getControlField() {
    if (variableField instanceof ControlField) {
      return (ControlField) variableField;
    }
    return null;
  }

  public DataField getDataField() {
    if (variableField instanceof DataField) {
      return (DataField) variableField;
    }
    return null;
  }

  public RuleParameter getRuleParameter() {
    return ruleParameter;
  }

  public void setRuleParameter(RuleParameter ruleParameter) {
    this.ruleParameter = ruleParameter;
  }

  public String getSubfieldValue() {
    return subfieldValue;
  }

  public void setSubfieldValue(String subfieldValue) {
    this.subfieldValue = subfieldValue;
  }

  public MappingParameters getMappingParameters() {
    return mappingParameters;
  }

  public ObjectNode getInstanceObject() {
    return instanceObject;
  }

  public Leader getLeader() {
    return leader;
  }

  public static RuleExecutionContext with(MappingParameters mappingParameters, ObjectNode instanceObject, Leader leader) {
    return new RuleExecutionContext(mappingParameters, instanceObject, leader);
  }

}