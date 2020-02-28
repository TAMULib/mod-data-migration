package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.folio.spring.domain.model.AbstractBaseEntity;

@Entity
public class ReferenceLinkType extends AbstractBaseEntity {

  @NotNull
  @Column(unique = true, nullable = false)
  private String name;

  public ReferenceLinkType() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}