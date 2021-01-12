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
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecords;
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

import io.vertx.core.json.JsonObject;

public class BoundWithMigration extends AbstractMigration<BoundWithContext> {

  private static final String INSTANCE_HRID_PREFIX = "INSTANCE_HRID_PREFIX";
  private static final String INSTANCE_HRID_START_NUMBER = "INSTANCE_HRID_START_NUMBER";

  private static final String HOLDINGS_HRID_PREFIX = "HOLDINGS_HRID_PREFIX";
  private static final String HOLDINGS_HRID_START_NUMBER = "HOLDINGS_HRID_START_NUMBER";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String BOUND_WITH = "BOUND_WITH";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";

  private BoundWithMigration(BoundWithContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);
    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<BoundWithContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        try {
          migrationService.okapiService.updateHridSettings(hridSettings, tenant, token);
          log.info("updated hrid settings: {}", hridSettings);
        } catch (Exception e) {
          log.error("failed to updated hrid settings: {}", e.getMessage());
        }
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    JsonObject instancesHridSettings = hridSettings.getJsonObject("instances");
    String instanceHridPrefix = instancesHridSettings.getString(PREFIX);
    int instanceHridStartNumber = instancesHridSettings.getInteger(START_NUMBER);

    JsonObject holdingsHridSettings = hridSettings.getJsonObject("holdings");
    String holdingsHridPrefix = holdingsHridSettings.getString(PREFIX);
    int holdingsHridStartNumber = holdingsHridSettings.getInteger(START_NUMBER);

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
        partitionContext.put(INSTANCE_HRID_PREFIX, instanceHridPrefix);
        partitionContext.put(INSTANCE_HRID_START_NUMBER, instanceHridStartNumber);
        partitionContext.put(HOLDINGS_HRID_PREFIX, holdingsHridPrefix);
        partitionContext.put(HOLDINGS_HRID_START_NUMBER, holdingsHridStartNumber);
        partitionContext.put(JOB, job);
        partitionContext.put(TOKEN, token);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new BoundWithPartitionTask(migrationService, partitionContext));
        offset += limit;
        instanceHridStartNumber += limit;
        holdingsHridStartNumber += limit;
        index++;
      }
    }

    instancesHridSettings.put(START_NUMBER, ++instanceHridStartNumber);
    holdingsHridSettings.put(START_NUMBER, ++holdingsHridStartNumber);

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static BoundWithMigration with(BoundWithContext context, String tenant) {
    return new BoundWithMigration(context, tenant);
  }

  public class BoundWithPartitionTask implements PartitionTask<BoundWithContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private int instanceHrid;

    private int holdingsHrid;

    public BoundWithPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.instanceHrid = (int) partitionContext.get(INSTANCE_HRID_START_NUMBER);
      this.holdingsHrid = (int) partitionContext.get(HOLDINGS_HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public BoundWithPartitionTask execute(BoundWithContext context) {
      long startTime = System.nanoTime();

      String instanceHridPrefix = (String) partitionContext.get(INSTANCE_HRID_PREFIX);
      String holdingsHridPrefix = (String) partitionContext.get(HOLDINGS_HRID_PREFIX);

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

          Holdingsrecord existingHoldingsRecord;
          try {
            existingHoldingsRecord = migrationService.okapiService.fetchHoldingsRecordById(tenant, token, existingHoldingsRecordId);
          } catch (Exception e) {
            log.error("failed to fetch holdings record by id {}: {}", existingHoldingsRecordId, e.getMessage());
            continue;
          }

          String parentInstanceTitle = String.format("%s_bound_with_%s", schema, mfhdId);
          Instance parentInstance = new Instance();
          parentInstance.setId(craftUUID("bound-with-parent-instance", schema, mfhdId));
          parentInstance.setHrid(String.format(HRID_TEMPLATE, instanceHridPrefix, instanceHrid));
          parentInstance.setSource("FOLIO");
          parentInstance.setTitle(parentInstanceTitle);
          parentInstance.setDiscoverySuppress(true);
          parentInstance.setStatusId(job.getStatusId());
          parentInstance.setInstanceTypeId(job.getInstanceTypeId());
          parentInstance.setModeOfIssuanceId(job.getModeOfIssuanceId());

          try {
            migrationService.okapiService.postInstance(tenant, token, parentInstance);
            instanceHrid++;
          } catch (Exception e) {
            log.error("failed to create parent instance {}: {}", parentInstanceTitle, e.getMessage());
            continue;
          }

          for (ReferenceLink instanceRL : instanceRLs) {
            Holdingsrecords holdingsRecords = migrationService.okapiService.fetchHoldingsRecordsByIdAndInstanceId(tenant, token, existingHoldingsRecordId, instanceRL.getFolioReference());
            if (holdingsRecords.getTotalRecords() == 0) {
              Holdingsrecord childHoldingsRecord = existingHoldingsRecord;
              String bibId = instanceRL.getExternalReference();
              childHoldingsRecord.setId(craftUUID("bound-with-child-holdings-record", schema, mfhdId + ":" + bibId));
              childHoldingsRecord.setHrid(String.format(HRID_TEMPLATE, holdingsHridPrefix, holdingsHrid));
              childHoldingsRecord.setInstanceId(instanceRL.getFolioReference());
              try {
                migrationService.okapiService.postHoldingsrecord(tenant, token, childHoldingsRecord);
                holdingsHrid++;
              } catch (Exception e) {
                log.error("failed to create duplicate child holdings record for child instance {}: {}", instanceRL.getFolioReference(), e.getMessage());
                continue;
              }
            }
          }

          for (ReferenceLink instanceRL : instanceRLs) {
            Instancerelationship instanceRelationship = new Instancerelationship();
            instanceRelationship.setSuperInstanceId(parentInstance.getId());
            instanceRelationship.setSubInstanceId(instanceRL.getFolioReference());
            instanceRelationship.setInstanceRelationshipTypeId(job.getInstanceRelationshipTypeId());

            try {
              migrationService.okapiService.postInstancerelationship(tenant, token, instanceRelationship);
            } catch (Exception e) {
              log.error("failed to create instance relationship between super instance {} amd sub instance {}: {}", parentInstance.getId(), instanceRL.getFolioReference(), e.getMessage());
            }
          }

          Holdingsrecord parentHoldingsRecord = new Holdingsrecord();

          parentHoldingsRecord.setId(craftUUID("bound-with-parent-holdings-record", schema, mfhdId));
          parentHoldingsRecord.setInstanceId(parentInstance.getId());
          parentHoldingsRecord.setHoldingsTypeId(job.getHoldingsTypeId());
          parentHoldingsRecord.setDiscoverySuppress(true);

          parentHoldingsRecord.setFormerIds(existingHoldingsRecord.getFormerIds());
          parentHoldingsRecord.setCallNumber(existingHoldingsRecord.getCallNumber());
          parentHoldingsRecord.setCallNumberTypeId(existingHoldingsRecord.getCallNumberTypeId());
          parentHoldingsRecord.setPermanentLocationId(existingHoldingsRecord.getPermanentLocationId());

          try {
            migrationService.okapiService.postHoldingsrecord(tenant, token, parentHoldingsRecord);
          } catch (Exception e) {
            log.error("failed to create parent holdings record for parent instance {}: {}", parentInstance.getId(), e.getMessage());
            continue;
          }

          Items existingItems;
          try {
            existingItems = migrationService.okapiService.fetchItemRecordsByHoldingsRecordId(tenant, token, existingHoldingsRecordId);
          } catch (Exception e) {
            log.error("failed to fetch item records by holdings record {}: {}", existingHoldingsRecordId, e.getMessage());
            continue;
          }

          for (Item existingItem : existingItems.getItems()) {
            existingItem.setHoldingsRecordId(parentHoldingsRecord.getId());
            try {
              migrationService.okapiService.putItem(tenant, token, existingItem);
            } catch (Exception e) {
              log.error("failed to update item {}: {}", existingItem.getId(), e.getMessage());
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
