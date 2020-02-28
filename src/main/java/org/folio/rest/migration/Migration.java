package org.folio.rest.migration;

import java.util.concurrent.CompletableFuture;

import org.folio.rest.migration.service.MigrationService;

@FunctionalInterface
public interface Migration {

  CompletableFuture<Boolean> run(MigrationService migrationService);

}
