package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.user.UserReferenceLinkContext;
import org.folio.rest.migration.model.request.user.UserReferenceLinkJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class UserReferenceLinkMigration extends AbstractMigration<UserReferenceLinkContext> {

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PATRON_BARCODE = "PATRON_BARCODE";
  private static final String EXTERNAL_SYSTEM_ID = "EXTERNAL_SYSTEM_ID";

  private static final String USER_REFERENCE_ID = "userTypeId";
  private static final String USER_BARCODE_REFERENCE_ID = "userBarcodeTypeId";
  private static final String USER_EXTERNAL_REFERENCE_ID = "userExternalTypeId";
  private static final String USER_TO_EXTERNAL_REFERENCE_ID = "userToExternalTypeId";

  private static final String DECODE = "DECODE";

  private static final Map<String, String> EXTERNAL_ID_TO_FOLIO_REFERENCE = new ConcurrentHashMap<>();

  // (id,external_reference,folioreference,type_id)
  private static String REFERENCE_LINK_COPY_SQL = "COPY %s.reference_links (id,external_reference,folio_reference,type_id) FROM STDIN WITH NULL AS 'null'";

  private UserReferenceLinkMigration(UserReferenceLinkContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    preActions(migrationService.referenceLinkSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<UserReferenceLinkContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(migrationService.referenceLinkSettings, context.getPostActions());
        EXTERNAL_ID_TO_FOLIO_REFERENCE.clear();
        migrationService.complete();
      }

    });

    Database voyagerSettings = context.getExtraction().getDatabase();

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (UserReferenceLinkJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      int partitions = job.getPartitions();
      int limit = (int) Math.ceil((double) count / (double) partitions);
      int offset = 0;
      for (int i = 0; i < partitions; i++) {
        Map<String, Object> partitionContext = new HashMap<String, Object>();
        partitionContext.put(SQL, context.getExtraction().getPageSql());
        partitionContext.put(SCHEMA, job.getSchema());
        partitionContext.put(DECODE, job.getDecodeSql());
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new UserReferenceLinkPartitionTask(migrationService, partitionContext, job));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static UserReferenceLinkMigration with(UserReferenceLinkContext context, String tenant) {
    return new UserReferenceLinkMigration(context, tenant);
  }

  public class UserReferenceLinkPartitionTask implements PartitionTask<UserReferenceLinkContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final UserReferenceLinkJob job;

    public UserReferenceLinkPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext, UserReferenceLinkJob job) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.job = job;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public String getSchema() {
      return job.getSchema();
    }

    @Override
    public PartitionTask<UserReferenceLinkContext> execute(UserReferenceLinkContext context) {
      long startTime = System.nanoTime();

      String schema = this.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      String userRLTypeId = job.getReferences().get(USER_REFERENCE_ID);
      String userBarcodeRLTypeId = job.getReferences().get(USER_BARCODE_REFERENCE_ID);
      String userExternalRLTypeId = job.getReferences().get(USER_EXTERNAL_REFERENCE_ID);
      String userToExternalRLTypeId = job.getReferences().get(USER_TO_EXTERNAL_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, migrationService.referenceLinkSettings);

      String tenantSchema = migrationService.schemaService.getSchema(tenant);

      try (
        PrintWriter referenceLinkWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getReferenceLinkConnection(), String.format(REFERENCE_LINK_COPY_SQL, tenantSchema)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {
          String patronId = pageResultSet.getString(PATRON_ID);
          String patronBarcode = pageResultSet.getString(PATRON_BARCODE);
          String externalSystemId = pageResultSet.getString(EXTERNAL_SYSTEM_ID);

          if (Objects.isNull(patronBarcode)) {
            patronBarcode = StringUtils.EMPTY;
          }

          String existingUserFolioReference = EXTERNAL_ID_TO_FOLIO_REFERENCE.get(externalSystemId);

          String userRLId = UUID.randomUUID().toString();
          String userBarcodeRLId = UUID.randomUUID().toString();
          String userExternalRLId = UUID.randomUUID().toString();
          String userToExternalRLId = UUID.randomUUID().toString();
          String userFolioReference;

          if (Objects.nonNull(existingUserFolioReference)) {
            userFolioReference = existingUserFolioReference;
          } else {
            userFolioReference = craftUUID("user", schema, patronId);
            EXTERNAL_ID_TO_FOLIO_REFERENCE.put(externalSystemId, userFolioReference);
          }

          referenceLinkWriter.println(String.join("\t", userRLId, patronId, userFolioReference, userRLTypeId));
          referenceLinkWriter.println(String.join("\t", userBarcodeRLId, patronBarcode, userFolioReference, userBarcodeRLTypeId));
          referenceLinkWriter.println(String.join("\t", userExternalRLId, externalSystemId, userFolioReference, userExternalRLTypeId));
          referenceLinkWriter.println(String.join("\t", userToExternalRLId, userRLId, userExternalRLId, userToExternalRLTypeId));
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((UserReferenceLinkPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database referenceLinkSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setReferenceLinkConnection(getConnection(referenceLinkSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private BaseConnection referenceLinkConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public BaseConnection getReferenceLinkConnection() {
      return referenceLinkConnection;
    }

    public void setReferenceLinkConnection(BaseConnection referenceLinkConnection) {
      this.referenceLinkConnection = referenceLinkConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        referenceLinkConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
