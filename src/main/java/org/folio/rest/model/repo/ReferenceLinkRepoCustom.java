package org.folio.rest.model.repo;

import java.util.stream.Stream;

import org.folio.rest.model.ReferenceLink;

public interface ReferenceLinkRepoCustom {

  public Stream<ReferenceLink> streamAllByTypeIdOrderByExternalReferenceAsc(String typeId, String orderClass)
      throws ClassNotFoundException;

}
