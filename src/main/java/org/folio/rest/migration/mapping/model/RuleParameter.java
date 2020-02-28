package org.folio.rest.migration.mapping.model;

import java.util.ArrayList;
import java.util.List;

// TODO: make interface and separate into parameter types
public class RuleParameter {

  private String name;

  private String unspecifiedInstanceTypeCode;

  private String substring;

  private Integer from;

  private Integer to;

  private List<String> names;

  private String oclc_regex;

  public RuleParameter() {
    names = new ArrayList<String>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUnspecifiedInstanceTypeCode() {
    return unspecifiedInstanceTypeCode;
  }

  public void setUnspecifiedInstanceTypeCode(String unspecifiedInstanceTypeCode) {
    this.unspecifiedInstanceTypeCode = unspecifiedInstanceTypeCode;
  }

  public String getSubstring() {
    return substring;
  }

  public void setSubstring(String substring) {
    this.substring = substring;
  }

  public Integer getFrom() {
    return from;
  }

  public void setFrom(Integer from) {
    this.from = from;
  }

  public Integer getTo() {
    return to;
  }

  public void setTo(Integer to) {
    this.to = to;
  }

  public List<String> getNames() {
    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }

  public String getOclc_regex() {
    return oclc_regex;
  }

  public void setOclc_regex(String oclc_regex) {
    this.oclc_regex = oclc_regex;
  }

}
