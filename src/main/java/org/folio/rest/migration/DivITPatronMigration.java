package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronContext;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronJob;
import org.folio.rest.migration.service.MigrationService;

public class DivITPatronMigration extends AbstractMigration<DivITPatronContext> {

  private DivITPatronMigration(DivITPatronContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<DivITPatronContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    int index = 0;

    for (DivITPatronJob job : context.getJobs()) {
      Map<String, Object> partitionContext = new HashMap<>();
      partitionContext.put(SQL, job.getSql());
      partitionContext.put(INDEX, index);
      partitionContext.put(TOKEN, token);
      partitionContext.put(JOB, job);
      log.info("submitting index {}", index++);
      taskQueue.submit(new DivITPatronPartitionTask(migrationService, partitionContext));
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static DivITPatronMigration with(DivITPatronContext context, String tenant) {
    return new DivITPatronMigration(context, tenant);
  }

  public class DivITPatronPartitionTask implements PartitionTask<DivITPatronContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public DivITPatronPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public DivITPatronPartitionTask execute(DivITPatronContext context) {
      long startTime = System.nanoTime();

      Database settings = context.getDatabase();

      ThreadConnections threadConnections = getThreadConnections(settings);

      DivITPatronJob job = (DivITPatronJob) partitionContext.get(JOB);

      String token = (String) partitionContext.get(TENANT);

      try (
        Statement statement = threadConnections.getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, partitionContext);
      ) {

        while (resultSet.next()) {
          System.out.println(resultSet);
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((DivITPatronPartitionTask) obj).getIndex() == this.getIndex();
    }
  }

  private ThreadConnections getThreadConnections(Database settings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setConnection(getConnection(settings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection connection;

    public ThreadConnections() {

    }

    public Connection getConnection() {
      return connection;
    }

    public void setConnection(Connection connection) {
      this.connection = connection;
    }

    public void closeAll() {
      try {
        connection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
