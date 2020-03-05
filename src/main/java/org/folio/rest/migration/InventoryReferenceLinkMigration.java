package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.InventoryReferenceLinkJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import com.fasterxml.jackson.databind.JsonNode;

public class InventoryReferenceLinkMigration extends AbstractMigration<InventoryReferenceLinkContext> {

  private static final String BIB_ID = "BIB_ID";
  private static final String HOLDING_ITEMS = "HOLDING_ITEMS";

  private static final String SOURCE_RECORD_REFERENCE_ID = "sourceRecordTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";
  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String ITEM_REFERENCE_ID = "itemTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";
  private static final String ITEM_TO_HOLDING_REFERENCE_ID = "itemToHoldingTypeId";

  // (id,externalreference,folioreference,type_id)
  private static String REFERENCE_LINK_COPY_SQL = "COPY %s.referencelink (id,externalreference,folioreference,type_id) FROM STDIN";

  private InventoryReferenceLinkMigration(InventoryReferenceLinkContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {

    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    preActions(migrationService.referenceLinkSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<InventoryReferenceLinkContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(migrationService.referenceLinkSettings, context.getPostActions());
      }

    });

    Database voyagerSettings = context.getExtraction().getDatabase();

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (InventoryReferenceLinkJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      int partitions = job.getPartitions();
      int limit = count / partitions;
      if (limit * partitions < count) {
        limit++;
      }
      int offset = 0;
      for (int i = 0; i < partitions; i++) {
        Map<String, Object> partitionContext = new HashMap<String, Object>();
        partitionContext.put(SQL, context.getExtraction().getPageSql());
        partitionContext.put(SCHEMA, job.getSchema());
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        taskQueue.submit(new InventoryReferenceLinkPartitionTask(migrationService, partitionContext, job));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(true);
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

      String sourceRecordRLTypeId = job.getReferences().get(SOURCE_RECORD_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
      String holdingTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String holdingToBibTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);
      String itemTypeId = job.getReferences().get(ITEM_REFERENCE_ID);
      String itemToHoldingTypeId = job.getReferences().get(ITEM_TO_HOLDING_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, migrationService.referenceLinkSettings);

      try {

        PGCopyOutputStream referenceLinkOutput = new PGCopyOutputStream(threadConnections.getReferenceLinkConnection(),
            String.format(REFERENCE_LINK_COPY_SQL, tenant));
        PrintWriter referenceLinkWriter = new PrintWriter(referenceLinkOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement holdingIdsStatement = threadConnections.getHoldingIdsConnection().createStatement();
        Statement itemIdsStatement = threadConnections.getItemIdsConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {
          String bibId = pageResultSet.getString(BIB_ID);

          String sourceRecordRLId = UUID.randomUUID().toString();
          String sourceRecordFolioReference = UUID.randomUUID().toString();
          String instanceRLId = UUID.randomUUID().toString();
          String instanceFolioReference = UUID.randomUUID().toString();

          referenceLinkWriter.println(String.join("\t", sourceRecordRLId, bibId, sourceRecordFolioReference, sourceRecordRLTypeId));
          referenceLinkWriter.println(String.join("\t", instanceRLId, bibId, instanceFolioReference, instanceRLTypeId));

          String[] holdingItems = (String[]) pageResultSet.getArray(HOLDING_ITEMS).getArray();

          String currentHoldingId = null;

          for (int i = 0; i < holdingItems.length; i++) {
            String[] holdingItem = holdingItems[i].split("::");
            if (holdingItem.length > 0 && StringUtils.isNotEmpty(holdingItem[0])) {
              String holdingId = holdingItem[0];
              String holdingRLId = UUID.randomUUID().toString();
              if (!holdingId.equals(currentHoldingId)) {
                String holdingFolioReference = UUID.randomUUID().toString();
                referenceLinkWriter.println(String.join("\t", holdingRLId, holdingId, holdingFolioReference, holdingTypeId));
                referenceLinkWriter.println(String.join("\t", UUID.randomUUID().toString(), instanceRLId, holdingRLId, holdingToBibTypeId));
                currentHoldingId = holdingId;
              }
              if (holdingItem.length > 1 && StringUtils.isNotEmpty(holdingItem[1])) {
                String itemId = holdingItem[1];
                String itemRLId = UUID.randomUUID().toString();
                String itemFolioReference = UUID.randomUUID().toString();
                referenceLinkWriter.println(String.join("\t", itemRLId, itemId, itemFolioReference, itemTypeId));
                referenceLinkWriter.println(String.join("\t", UUID.randomUUID().toString(), holdingRLId, itemRLId, itemToHoldingTypeId));
              }
            }
          }
        }

        referenceLinkWriter.close();

        pageStatement.close();
        holdingIdsStatement.close();
        itemIdsStatement.close();

        pageResultSet.close();

      } catch (SQLException e) {
        e.printStackTrace();
      }

      threadConnections.closeAll();

      log.info("{} {} finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database referenceLinkSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setHoldingIdsConnection(getConnection(voyagerSettings));
    threadConnections.setItemIdsConnection(getConnection(voyagerSettings));
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
    private Connection holdingIdsConnection;
    private Connection itemIdsConnection;

    private BaseConnection referenceLinkConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getHoldingIdsConnection() {
      return holdingIdsConnection;
    }

    public void setHoldingIdsConnection(Connection holdingIdsConnection) {
      this.holdingIdsConnection = holdingIdsConnection;
    }

    public Connection getItemIdsConnection() {
      return itemIdsConnection;
    }

    public void setItemIdsConnection(Connection itemIdsConnection) {
      this.itemIdsConnection = itemIdsConnection;
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
        holdingIdsConnection.close();
        itemIdsConnection.close();
        referenceLinkConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
