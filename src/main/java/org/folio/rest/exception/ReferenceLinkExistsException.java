package org.folio.rest.exception;

import org.folio.rest.model.ReferenceLink;

public class ReferenceLinkExistsException extends RuntimeException {

  private static final long serialVersionUID = 8239125524416844006L;

  private final ReferenceLink referenceLink;

  public ReferenceLinkExistsException(ReferenceLink referenceLink) {
    super("Reference Link Already Exists");
    this.referenceLink = referenceLink;
  }

  public ReferenceLinkExistsException(String message, ReferenceLink referenceLink) {
    super(message);
    this.referenceLink = referenceLink;
  }

  public ReferenceLink getReferenceLink() {
    return referenceLink;
  }

}
