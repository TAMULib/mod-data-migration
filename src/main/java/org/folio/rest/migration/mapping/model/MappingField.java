package org.folio.rest.migration.mapping.model;

import java.util.ArrayList;
import java.util.List;

public class MappingField {

  private boolean entityPerRepeatedSubfield = false;

  private List<MappingEntity> entity;

  public MappingField() {
    entity = new ArrayList<MappingEntity>();
  }

  public MappingField(List<MappingEntity> entity) {
    this.entity = entity;
  }

  public boolean isEntityPerRepeatedSubfield() {
    return entityPerRepeatedSubfield;
  }

  public void setEntityPerRepeatedSubfield(boolean entityPerRepeatedSubfield) {
    this.entityPerRepeatedSubfield = entityPerRepeatedSubfield;
  }

  public List<MappingEntity> getEntity() {
    return entity;
  }

  public void setEntity(List<MappingEntity> entity) {
    this.entity = entity;
  }

}
