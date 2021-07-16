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

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.vendor.VendorReferenceLinkContext;
import org.folio.rest.migration.model.request.vendor.VendorReferenceLinkJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class VendorReferenceLinkMigration extends AbstractMigration<VendorReferenceLinkContext> {

  private static final String VENDOR_ID = "VENDOR_ID";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";

  // (id,external_reference,folioreference,type_id)
  private static String REFERENCE_LINK_COPY_SQL = "COPY %s.reference_links (id,external_reference,folio_reference,type_id) FROM STDIN WITH NULL AS 'null'";

  private VendorReferenceLinkMigration(VendorReferenceLinkContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    preActions(migrationService.referenceLinkSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<VendorReferenceLinkContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(migrationService.referenceLinkSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Database voyagerSettings = context.getExtraction().getDatabase();

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (VendorReferenceLinkJob job : context.getJobs()) {

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
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new VendorReferenceLinkPartitionTask(migrationService, partitionContext, job));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static VendorReferenceLinkMigration with(VendorReferenceLinkContext context, String tenant) {
    return new VendorReferenceLinkMigration(context, tenant);
  }

  public class VendorReferenceLinkPartitionTask implements PartitionTask<VendorReferenceLinkContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final VendorReferenceLinkJob job;

    public VendorReferenceLinkPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext, VendorReferenceLinkJob job) {
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
    public PartitionTask<VendorReferenceLinkContext> execute(VendorReferenceLinkContext context) {
      long startTime = System.nanoTime();

      String schema = this.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, migrationService.referenceLinkSettings);

      String tenantSchema = migrationService.schemaService.getSchema(tenant);

      try (
        PrintWriter referenceLinkWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getReferenceLinkConnection(), String.format(REFERENCE_LINK_COPY_SQL, tenantSchema)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {
          final String vendorId = pageResultSet.getString(VENDOR_ID);
          final String vendorRLId = UUID.randomUUID().toString();
          final String vendorFolioReference = craftUUID("vendor", schema, vendorId);
          referenceLinkWriter.println(String.join("\t", vendorRLId, vendorId, vendorFolioReference, vendorRLTypeId));
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
      return Objects.nonNull(obj) && ((VendorReferenceLinkPartitionTask) obj).getIndex() == this.getIndex();
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
