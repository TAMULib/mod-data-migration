package org.folio.rest.model.response;

import java.util.Arrays;
import java.util.List;

public class CollectorReferenceLink extends BaseReferenceLink {

  private final List<String> references;

  public CollectorReferenceLink(String id, String typeId, String typeName, String folioReference,
      String externalReference, String[] references) {
    super(id, typeId, typeName, folioReference, externalReference);
    this.references = Arrays.asList(references);
  }

  public List<String> getReferences() {
    return references;
  }

}
