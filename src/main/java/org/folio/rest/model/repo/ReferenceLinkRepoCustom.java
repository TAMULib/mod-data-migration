package org.folio.rest.model.repo;

import java.util.stream.Stream;

import org.folio.rest.model.ReferenceLink;
import org.folio.rest.model.response.CollectorReferenceLink;
import org.folio.rest.model.response.JoinReferenceLink;

public interface ReferenceLinkRepoCustom {

  public Stream<ReferenceLink> streamAllByTypeIdOrderByExternalReferenceAsc(String typeId, String orderClass)
      throws ClassNotFoundException;

  public Stream<CollectorReferenceLink> streamAllByTypeIdCollectingTypeIdOrderByExternalReferenceAsc(String typeId,
      String collectTypeId, String orderClass) throws ClassNotFoundException;

  public Stream<JoinReferenceLink> streamAllByTypeIdJoiningTypeIdOrderByExternalReferenceAsc(String typeId,
      String joinTypeId, String orderClass) throws ClassNotFoundException;

}