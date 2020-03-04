package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.folio.spring.domain.model.AbstractBaseEntity;

@Entity
// @formatter:off
@Table(
  uniqueConstraints = {
    @UniqueConstraint(columnNames = { "folioReference", "externalReference", "type_id" })
  },
  indexes = {
    @Index(columnList = "type_id,externalReference"),
    @Index(columnList = "type_id,id,externalreference")
  }
)
//@formatter:on
public class ReferenceLink extends AbstractBaseEntity {

  @NotNull
  @Column(nullable = false)
  private String folioReference;

  @NotNull
  @Column(nullable = false)
  private String externalReference;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "type_id")
  private ReferenceLinkType type;

  public ReferenceLink() {
    super();
  }

  public String getFolioReference() {
    return folioReference;
  }

  public void setFolioReference(String folioReference) {
    this.folioReference = folioReference;
  }

  public String getExternalReference() {
    return externalReference;
  }

  public void setExternalReference(String externalReference) {
    this.externalReference = externalReference;
  }

  public ReferenceLinkType getType() {
    return type;
  }

  public void setType(ReferenceLinkType type) {
    this.type = type;
  }

}
