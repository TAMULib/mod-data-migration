package org.folio.rest.migration.mapping.model;

import java.util.List;

public class SubfieldDelimiter {

  private String value;

  private List<Character> subfields;

  public SubfieldDelimiter() {

  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Character> getSubfields() {
    return subfields;
  }

  public void setSubfields(List<Character> subfields) {
    this.subfields = subfields;
  }

}
