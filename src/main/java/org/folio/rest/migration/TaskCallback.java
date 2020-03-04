package org.folio.rest.migration;

@FunctionalInterface
public interface TaskCallback {

  public void complete();

}