package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.inventory.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.inventory.InventoryReferenceLinkJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class InventoryReferenceLinkMigration extends AbstractMigration<InventoryReferenceLinkContext> {

  private static final String BIB_ID = "BIB_ID";
  private static final String MFHD_ID = "MFHD_ID";
  private static final String ITEM_ID = "ITEM_ID";

  private static final String SOURCE_RECORD_REFERENCE_ID = "sourceRecordTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";
  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String ITEM_REFERENCE_ID = "itemTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";
  private static final String ITEM_TO_HOLDING_REFERENCE_ID = "itemToHoldingTypeId";

  // (id,external_reference,folio_reference,type_id)
  private static String REFERENCE_LINK_COPY_SQL = "COPY %s.reference_links (id,external_reference,folio_reference,type_id) FROM STDIN WITH NULL AS 'null'";

  private static Map<String, Set<String>> HOLDING_EXTERNAL_REFERENCES = new HashMap<>();
  private static Map<String, Set<String>> ITEM_EXTERNAL_REFERENCES = new HashMap<>();

  private InventoryReferenceLinkMigration(InventoryReferenceLinkContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    preActions(migrationService.referenceLinkSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<InventoryReferenceLinkContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(migrationService.referenceLinkSettings, context.getPostActions());
        HOLDING_EXTERNAL_REFERENCES.clear();
        ITEM_EXTERNAL_REFERENCES.clear();
        migrationService.complete();
      }

    });

    Database voyagerSettings = context.getExtraction().getDatabase();

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (InventoryReferenceLinkJob job : context.getJobs()) {

      HOLDING_EXTERNAL_REFERENCES.put(job.getSchema(), new HashSet<>());
      ITEM_EXTERNAL_REFERENCES.put(job.getSchema(), new HashSet<>());

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
        taskQueue.submit(new InventoryReferenceLinkPartitionTask(migrationService, partitionContext, job));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static InventoryReferenceLinkMigration with(InventoryReferenceLinkContext context, String tenant) {
    return new InventoryReferenceLinkMigration(context, tenant);
  }

  public class InventoryReferenceLinkPartitionTask implements PartitionTask<InventoryReferenceLinkContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final InventoryReferenceLinkJob job;

    public InventoryReferenceLinkPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext, InventoryReferenceLinkJob job) {
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
    public PartitionTask<InventoryReferenceLinkContext> execute(InventoryReferenceLinkContext context) {
      long startTime = System.nanoTime();

      String schema = this.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Map<String, Object> holdingContext = new HashMap<>();
      holdingContext.put(SQL, context.getExtraction().getHoldingSql());
      holdingContext.put(SCHEMA, schema);

      Map<String, Object> itemContext = new HashMap<>();
      itemContext.put(SQL, context.getExtraction().getItemSql());
      itemContext.put(SCHEMA, schema);

      String sourceRecordRLTypeId = job.getReferences().get(SOURCE_RECORD_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
      String holdingTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String holdingToBibTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);
      String itemTypeId = job.getReferences().get(ITEM_REFERENCE_ID);
      String itemToHoldingTypeId = job.getReferences().get(ITEM_TO_HOLDING_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, migrationService.referenceLinkSettings);

      String tenantSchema = migrationService.schemaService.getSchema(tenant);

      try (
        PrintWriter referenceLinkWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getReferenceLinkConnection(), String.format(REFERENCE_LINK_COPY_SQL, tenantSchema)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement holdingStatement = threadConnections.getHoldingConnection().createStatement();
        Statement itemStatement = threadConnections.getItemConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String bibId = pageResultSet.getString(BIB_ID);

          holdingContext.put(BIB_ID, bibId);

          String sourceRecordRLId = UUID.randomUUID().toString();
          String sourceRecordFolioReference = UUID.randomUUID().toString();
          String instanceRLId = UUID.randomUUID().toString();
          String instanceFolioReference = UUID.randomUUID().toString();

          referenceLinkWriter.println(String.join("\t", sourceRecordRLId, bibId, sourceRecordFolioReference, sourceRecordRLTypeId));
          referenceLinkWriter.println(String.join("\t", instanceRLId, bibId, instanceFolioReference, instanceRLTypeId));

          try (ResultSet holdingIdsResultSet = getResultSet(holdingStatement, holdingContext)) {

            while (holdingIdsResultSet.next()) {
              String holdingId = holdingIdsResultSet.getString(MFHD_ID);
              String holdingRLId = UUID.randomUUID().toString();
              String holdingFolioReference = UUID.randomUUID().toString();

              if (!holdingAlreadyProcessed(schema, holdingId)) {
                referenceLinkWriter.println(String.join("\t", holdingRLId, holdingId, holdingFolioReference, holdingTypeId));
              }
              referenceLinkWriter.println(String.join("\t", UUID.randomUUID().toString(), holdingRLId, instanceRLId, holdingToBibTypeId));

              itemContext.put(MFHD_ID, holdingId);

              try (ResultSet itemIdsResultSet = getResultSet(itemStatement, itemContext)) {

                while (itemIdsResultSet.next()) {
                  String itemId = itemIdsResultSet.getString(ITEM_ID);
                  String itemRLId = UUID.randomUUID().toString();
                  String itemFolioReference = UUID.randomUUID().toString();

                  if (!itemAlreadyProcessed(schema, itemId)) {
                    referenceLinkWriter.println(String.join("\t", itemRLId, itemId, itemFolioReference, itemTypeId));
                  }
                  referenceLinkWriter.println(String.join("\t", UUID.randomUUID().toString(), itemRLId, holdingRLId, itemToHoldingTypeId));
                }
              }
            }
          }
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
      return Objects.nonNull(obj) && ((InventoryReferenceLinkPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database referenceLinkSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setHoldingConnection(getConnection(voyagerSettings));
    threadConnections.setItemConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setReferenceLinkConnection(getConnection(referenceLinkSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private synchronized Boolean holdingAlreadyProcessed(String schema, String holdingId) {
    Set<String> holdingIds = HOLDING_EXTERNAL_REFERENCES.get(schema);
    boolean alreadyProcessed = holdingIds.contains(holdingId);
    if (!alreadyProcessed) {
      holdingIds.add(holdingId);
    }
    return alreadyProcessed;
  }

  private synchronized Boolean itemAlreadyProcessed(String schema, String itemId) {
    Set<String> itemIds = ITEM_EXTERNAL_REFERENCES.get(schema);
    boolean alreadyProcessed = itemIds.contains(itemId);
    if (!alreadyProcessed) {
      itemIds.add(itemId);
    }
    return alreadyProcessed;
  }

  private class ThreadConnections {

    private Connection pageConnection;
    private Connection holdingConnection;
    private Connection itemConnection;

    private BaseConnection referenceLinkConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getHoldingConnection() {
      return holdingConnection;
    }

    public void setHoldingConnection(Connection holdingConnection) {
      this.holdingConnection = holdingConnection;
    }

    public Connection getItemConnection() {
      return itemConnection;
    }

    public void setItemConnection(Connection itemConnection) {
      this.itemConnection = itemConnection;
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
        holdingConnection.close();
        itemConnection.close();
        referenceLinkConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
