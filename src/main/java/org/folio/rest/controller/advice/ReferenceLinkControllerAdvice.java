package org.folio.rest.controller.advice;

import org.folio.rest.exception.ReferenceLinkExistsException;
import org.folio.rest.model.ReferenceLink;
import org.folio.spring.model.response.Errors;
import org.folio.spring.utility.ErrorUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class ReferenceLinkControllerAdvice {

  private static final Logger logger = LoggerFactory.getLogger(ReferenceLinkControllerAdvice.class);

  @ExceptionHandler(value = ReferenceLinkExistsException.class)
  public ResponseEntity<ReferenceLink> handleInvalidValuePathException(ReferenceLinkExistsException exception) {
    return ResponseEntity.accepted().body(exception.getReferenceLink());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ClassNotFoundException.class)
  public Errors handleClassNotFoundException(ClassNotFoundException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.BAD_REQUEST);
  }

}
