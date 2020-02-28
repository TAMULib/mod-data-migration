package org.folio.rest.exception;

import org.folio.rest.model.ReferenceLink;

public class FolioReferenceLinkExistsException extends ReferenceLinkExistsException {

  private static final long serialVersionUID = -1213554355092812227L;

  public FolioReferenceLinkExistsException(ReferenceLink referenceLink) {
    super("Reference Link already exists for the given Folio Reference and Type.", referenceLink);
  }

}
