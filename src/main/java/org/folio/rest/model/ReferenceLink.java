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
  name = "reference_links",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = { "type_id", "external_reference" })
  },
  indexes = {
    @Index(columnList = "type_id,external_reference"),
    @Index(columnList = "type_id,id,external_reference"),
    @Index(columnList = "type_id,folio_reference")
  }
)
//@formatter:on
public class ReferenceLink extends AbstractBaseEntity {

  @NotNull
  @Column(name = "external_reference", nullable = false)
  private String externalReference;

  @NotNull
  @Column(name = "folio_reference", nullable = false)
  private String folioReference;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "type_id", nullable = false)
  private ReferenceLinkType type;

  public ReferenceLink() {
    super();
  }

  public String getExternalReference() {
    return externalReference;
  }

  public void setExternalReference(String externalReference) {
    this.externalReference = externalReference;
  }

  public String getFolioReference() {
    return folioReference;
  }

  public void setFolioReference(String folioReference) {
    this.folioReference = folioReference;
  }

  public ReferenceLinkType getType() {
    return type;
  }

  public void setType(ReferenceLinkType type) {
    this.type = type;
  }

}
