package org.folio.rest.migration.exception;

public class MigrationException extends Exception {

  private static final long serialVersionUID = 1063826189434005540L;

  public MigrationException(String message) {
    super(message);
  }

  public MigrationException(String message, Throwable cause) {
    super(message, cause);
  }

}
