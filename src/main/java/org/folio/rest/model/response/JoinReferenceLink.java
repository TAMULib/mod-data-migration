package org.folio.rest.model.response;

public class JoinReferenceLink extends BaseReferenceLink {

  private final String reference;

  public JoinReferenceLink(String id, String typeId, String typeName, String folioReference, String externalReference,
      String reference) {
    super(id, typeId, typeName, folioReference, externalReference);
    this.reference = reference;
  }

  public String getReference() {
    return reference;
  }

}
