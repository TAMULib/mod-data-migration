package org.folio.rest.migration.config.model;

// NOTE: if module databases are separated, this will need to be modified to support
public class Modules {

  public Database database;

  public Modules() {

  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

}
