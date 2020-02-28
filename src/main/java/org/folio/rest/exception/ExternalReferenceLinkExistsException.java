package org.folio.rest.exception;

import org.folio.rest.model.ReferenceLink;

public class ExternalReferenceLinkExistsException extends ReferenceLinkExistsException {

  private static final long serialVersionUID = -4470647079772964748L;

  public ExternalReferenceLinkExistsException(ReferenceLink referenceLink) {
    super("Reference Link already exists for the given External Reference and Type.", referenceLink);
  }

}
