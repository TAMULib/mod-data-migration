package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Instance;
import org.folio.rest.jaxrs.model.inventory.Instancerelationship;
import org.folio.rest.jaxrs.model.inventory.Item;
import org.folio.rest.jaxrs.model.inventory.Items;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.boundwith.BoundWithContext;
import org.folio.rest.migration.model.request.boundwith.BoundWithJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;

public class BoundWithMigration extends AbstractMigration<BoundWithContext> {

  private static final String MFHD_ID = "MFHD_ID";
  private static final String BOUND_WITH = "BOUND_WITH";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";

  private BoundWithMigration(BoundWithContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<BoundWithContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (BoundWithJob job : context.getJobs()) {

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
        partitionContext.put(JOB, job);
        partitionContext.put(TOKEN, token);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new BoundWithPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static BoundWithMigration with(BoundWithContext context, String tenant) {
    return new BoundWithMigration(context, tenant);
  }

  public class BoundWithPartitionTask implements PartitionTask<BoundWithContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public BoundWithPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public BoundWithPartitionTask execute(BoundWithContext context) {
      long startTime = System.nanoTime();

      BoundWithJob job = (BoundWithJob) partitionContext.get(JOB);

      String token = (String) partitionContext.get(TOKEN);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          String mfhdId = pageResultSet.getString(MFHD_ID);
          String boundWith = pageResultSet.getString(BOUND_WITH);

          List<String> bibIds = Arrays.asList(boundWith.split(","));

          Optional<ReferenceLink> holdingsRl = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);
          if (!holdingsRl.isPresent()) {
            log.error("{} no holdings record id found for bib id {}", schema, mfhdId);
            continue;
          }

          List<ReferenceLink> instanceRLs = migrationService.referenceLinkRepo.findByTypeIdAndExternalReferenceIn(instanceRLTypeId, bibIds);
          if (instanceRLs.size() != bibIds.size()) {
            log.error("{} not all instance ids found for bib ids {}", schema, bibIds);
            continue;
          }

          String existingHoldingsRecordId = holdingsRl.get().getFolioReference();

          Holdingsrecord existingHoldingsRecord = migrationService.okapiService.fetchHoldingsRecordById(tenant, token, existingHoldingsRecordId);

          Instance parentInstance = new Instance();
          parentInstance.setId(UUID.randomUUID().toString());
          parentInstance.setSource("FOLIO");
          parentInstance.setTitle(String.format("%s_bound_with_%s", schema, mfhdId));
          parentInstance.setDiscoverySuppress(true);
          parentInstance.setStatusId(job.getStatusId());
          parentInstance.setInstanceTypeId(job.getInstanceTypeId());
          parentInstance.setModeOfIssuanceId(job.getModeOfIssuanceId());

          migrationService.okapiService.postInstance(tenant, token, parentInstance);

          for (ReferenceLink instanceRL : instanceRLs) {
            Instancerelationship instanceRelationship = new Instancerelationship();
            instanceRelationship.setSuperInstanceId(parentInstance.getId());
            instanceRelationship.setSubInstanceId(instanceRL.getFolioReference());
            instanceRelationship.setInstanceRelationshipTypeId(job.getInstanceRelationshipTypeId());

            migrationService.okapiService.postInstancerelationship(tenant, token, instanceRelationship);
          }

          Holdingsrecord parentHoldingsRecord = new Holdingsrecord();
          parentHoldingsRecord.setId(UUID.randomUUID().toString());
          parentHoldingsRecord.setFormerIds(existingHoldingsRecord.getFormerIds());
          parentHoldingsRecord.setCallNumber(existingHoldingsRecord.getCallNumber());
          parentHoldingsRecord.setInstanceId(parentInstance.getId());
          parentHoldingsRecord.setHoldingsTypeId(job.getHoldingsTypeId());
          parentHoldingsRecord.setCallNumberTypeId(existingHoldingsRecord.getCallNumberTypeId());
          parentHoldingsRecord.setDiscoverySuppress(true);
          parentHoldingsRecord.setPermanentLocationId(existingHoldingsRecord.getPermanentLocationId());

          migrationService.okapiService.postHoldingsrecord(tenant, token, parentHoldingsRecord);

          Items existingItems = migrationService.okapiService.fetchItemRecordsByHoldingsRecordId(tenant, token, existingHoldingsRecordId);

          for (Item existingItem : existingItems.getItems()) {
            existingItem.setHoldingsRecordId(parentHoldingsRecord.getId());
            migrationService.okapiService.putItem(tenant, token, existingItem);
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
      return Objects.nonNull(obj) && ((BoundWithPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
