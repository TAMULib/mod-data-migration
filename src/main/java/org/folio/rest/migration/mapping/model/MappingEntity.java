package org.folio.rest.migration.mapping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

public class MappingEntity {

  @NotNull
  private String target;

  private String description;

  private boolean applyRulesOnConcatenatedData = false;

  private boolean ignoreSubsequentFields = false;

  private List<SubfieldDelimiter> subFieldDelimiter;

  private List<Character> subfield;

  private List<String> requiredSubfield;

  private Optional<SubfieldSplit> subFieldSplit;

  private List<MappingRule> rules;

  public MappingEntity() {
    subFieldDelimiter = new ArrayList<SubfieldDelimiter>();
    subfield = new ArrayList<Character>();
    requiredSubfield = new ArrayList<String>();
    rules = new ArrayList<MappingRule>();
    subFieldSplit = Optional.empty();
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isApplyRulesOnConcatenatedData() {
    return applyRulesOnConcatenatedData;
  }

  public void setApplyRulesOnConcatenatedData(boolean applyRulesOnConcatenatedData) {
    this.applyRulesOnConcatenatedData = applyRulesOnConcatenatedData;
  }

  public boolean isIgnoreSubsequentFields() {
    return ignoreSubsequentFields;
  }

  public void setIgnoreSubsequentFields(boolean ignoreSubsequentFields) {
    this.ignoreSubsequentFields = ignoreSubsequentFields;
  }

  public List<SubfieldDelimiter> getSubFieldDelimiter() {
    return subFieldDelimiter;
  }

  public void setSubFieldDelimiter(List<SubfieldDelimiter> subFieldDelimiter) {
    this.subFieldDelimiter = subFieldDelimiter;
  }

  public List<Character> getSubfield() {
    return subfield;
  }

  public void setSubfield(List<Character> subfield) {
    this.subfield = subfield;
  }

  public List<String> getRequiredSubfield() {
    return requiredSubfield;
  }

  public void setRequiredSubfield(List<String> requiredSubfield) {
    this.requiredSubfield = requiredSubfield;
  }

  public Optional<SubfieldSplit> getSubFieldSplit() {
    return subFieldSplit;
  }

  public void setSubFieldSplit(Optional<SubfieldSplit> subFieldSplit) {
    this.subFieldSplit = subFieldSplit;
  }

  public List<MappingRule> getRules() {
    return rules;
  }

  public void setRules(List<MappingRule> rules) {
    this.rules = rules;
  }

}
