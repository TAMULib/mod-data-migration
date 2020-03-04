package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public abstract class AbstractJob {

  @NotNull
  private String schema;

  @NotNull
  private int partitions;

  public AbstractJob() {

  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public int getPartitions() {
    return partitions;
  }

  public void setPartitions(int partitions) {
    this.partitions = partitions;
  }

}
