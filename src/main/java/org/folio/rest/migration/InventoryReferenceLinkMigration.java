package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.InventoryReferenceLinkJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.folio.rest.model.ReferenceLinkType;

import com.fasterxml.jackson.databind.JsonNode;

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

  private InventoryReferenceLinkMigration(InventoryReferenceLinkContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {

    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    Database voyagerSettings = context.getExtraction().getDatabase();

    preActions(migrationService.referenceLinkSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<InventoryReferenceLinkContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(migrationService.referenceLinkSettings, context.getPostActions());
      }

    });

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
      for (int i = 0; i <= partitions; i++) {
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

      Map<String, Object> holdingIdsContext = new HashMap<>();
      holdingIdsContext.put(SQL, context.getExtraction().getHoldingIdsSql());
      holdingIdsContext.put(SCHEMA, schema);

      Map<String, Object> itemIdsContext = new HashMap<>();
      itemIdsContext.put(SQL, context.getExtraction().getItemIdsSql());
      itemIdsContext.put(SCHEMA, schema);

      String sourceRecordRLTypeId = job.getReferences().get(SOURCE_RECORD_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
      String holdingTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String holdingToBibTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);
      String itemTypeId = job.getReferences().get(ITEM_REFERENCE_ID);
      String itemToHoldingTypeId = job.getReferences().get(ITEM_TO_HOLDING_REFERENCE_ID);

      ReferenceLinkType sourceRecordRLType = migrationService.referenceLinkTypeRepo.getOne(sourceRecordRLTypeId);
      ReferenceLinkType instanceRLType = migrationService.referenceLinkTypeRepo.getOne(instanceRLTypeId);
      ReferenceLinkType holdingType = migrationService.referenceLinkTypeRepo.getOne(holdingTypeId);
      ReferenceLinkType holdingToBibType = migrationService.referenceLinkTypeRepo.getOne(holdingToBibTypeId);
      ReferenceLinkType itemType = migrationService.referenceLinkTypeRepo.getOne(itemTypeId);
      ReferenceLinkType itemToHoldingType = migrationService.referenceLinkTypeRepo.getOne(itemToHoldingTypeId);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      try {

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement holdingIdsStatement = threadConnections.getHoldingIdsConnection().createStatement();
        Statement itemIdsStatement = threadConnections.getItemIdsConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {
          List<ReferenceLink> referenceLinks = new ArrayList<>();

          String bibId = pageResultSet.getString(BIB_ID);

          holdingIdsContext.put(BIB_ID, bibId);

          String sourceRecordRLId = UUID.randomUUID().toString();
          String sourceRecordFolioReference = UUID.randomUUID().toString();
          String instanceRLId = UUID.randomUUID().toString();
          String instanceFolioReference = UUID.randomUUID().toString();

          referenceLinks.add(ReferenceLink.with(sourceRecordRLId, bibId, sourceRecordFolioReference, sourceRecordRLType));
          referenceLinks.add(ReferenceLink.with(instanceRLId, bibId, instanceFolioReference, instanceRLType));

          try (ResultSet holdingIdsResultSet = getResultSet(holdingIdsStatement, holdingIdsContext)) {

            while (holdingIdsResultSet.next()) {

              String holdingId = holdingIdsResultSet.getString(MFHD_ID);

              itemIdsContext.put(MFHD_ID, bibId);

              String holdingRLId = UUID.randomUUID().toString();
              String holdingFolioReference = UUID.randomUUID().toString();

              referenceLinks.add(ReferenceLink.with(holdingRLId, holdingId, holdingFolioReference, holdingType));
              referenceLinks.add(ReferenceLink.with(UUID.randomUUID().toString(), instanceRLId, holdingRLId, holdingToBibType));

              try (ResultSet itemIdsResultSet = getResultSet(itemIdsStatement, itemIdsContext)) {

                while (itemIdsResultSet.next()) {

                  String itemId = itemIdsResultSet.getString(ITEM_ID);

                  String itemRLId = UUID.randomUUID().toString();
                  String itemFolioReference = UUID.randomUUID().toString();

                  referenceLinks.add(ReferenceLink.with(itemRLId, itemId, itemFolioReference, itemType));
                  referenceLinks.add(ReferenceLink.with(UUID.randomUUID().toString(), holdingRLId, itemRLId, itemToHoldingType));
                }
              }
            }
          }
          migrationService.referenceLinkRepo.saveAll(referenceLinks);
        }

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

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setHoldingIdsConnection(getConnection(voyagerSettings));
    threadConnections.setItemIdsConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {
    private Connection pageConnection;
    private Connection holdingIdsConnection;
    private Connection itemIdsConnection;

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

    public void closeAll() {
      try {
        pageConnection.close();
        holdingIdsConnection.close();
        itemIdsConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
