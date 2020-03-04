package org.folio.rest.migration;

import org.folio.rest.migration.model.request.AbstractContext;

public interface PartitionTask<C extends AbstractContext> {

  public PartitionTask<C> execute(C context);

}
