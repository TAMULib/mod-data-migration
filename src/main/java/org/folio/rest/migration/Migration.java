package org.folio.rest.migration;

import java.util.concurrent.CompletableFuture;

import org.folio.rest.migration.exception.MigrationException;
import org.folio.rest.migration.service.MigrationService;

@FunctionalInterface
public interface Migration {

  CompletableFuture<String> run(MigrationService migrationService) throws MigrationException;

}
