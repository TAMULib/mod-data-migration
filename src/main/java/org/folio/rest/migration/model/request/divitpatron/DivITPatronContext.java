package org.folio.rest.migration.model.request.divitpatron;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.AbstractContext;

public class DivITPatronContext extends AbstractContext {

  @NotNull
  private Database database;

  private List<DivITPatronJob> jobs;

  public DivITPatronContext() {
    super();
    jobs = new ArrayList<>();
  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public List<DivITPatronJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<DivITPatronJob> jobs) {
    this.jobs = jobs;
  }

}
