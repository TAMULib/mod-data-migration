package org.folio.rest.migration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.coursereserve.CourseReserveContext;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.service.ReferenceDataService;

public class CourseReserveMigration extends AbstractMigration<CourseReserveContext> {

  private final ReferenceDataService referenceDataService;

  private CourseReserveMigration(CourseReserveContext context, ReferenceDataService referenceDataService, String tenant) {
    super(context, tenant);
    this.referenceDataService = referenceDataService;
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<CourseReserveContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    taskQueue.submit(new CourseReserveTask(referenceDataService));

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static CourseReserveMigration with(CourseReserveContext context, ReferenceDataService referenceDataService, String tenant) {
    return new CourseReserveMigration(context, referenceDataService, tenant);
  }

  public class CourseReserveTask implements PartitionTask<CourseReserveContext> {

    private final ReferenceDataService referenceDataService;

    public CourseReserveTask(ReferenceDataService referenceDataService) {
      this.referenceDataService = referenceDataService;
    }

    public CourseReserveTask execute(CourseReserveContext context) {
      try {
        referenceDataService.loadReferenceDataAsync( "classpath:/referenceData/coursereserves/*.json", tenant).get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }

      return this;
    }

  }

}
