package org.folio.rest.migration;

import java.util.concurrent.CompletableFuture;

import org.folio.rest.migration.model.request.InventoryReferenceLinkContext;
import org.folio.rest.migration.service.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryReferenceLinkMigration extends AbstractMigration<InventoryReferenceLinkContext> {

  private static final Logger log = LoggerFactory.getLogger(InventoryReferenceLinkMigration.class);

  private final InventoryReferenceLinkContext context;

  private final String tenant;

  private InventoryReferenceLinkMigration(InventoryReferenceLinkContext context, String tenant) {
    this.context = context;
    this.tenant = tenant;
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    return null;
  }

  public static InventoryReferenceLinkMigration with(InventoryReferenceLinkContext context, String tenant) {
    return new InventoryReferenceLinkMigration(context, tenant);
  }

}
