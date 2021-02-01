package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.order.OrderContext;
import org.folio.rest.migration.model.request.order.OrderJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;

public class OrderMigration extends AbstractMigration<OrderContext> {

  private static final String COLUMNS = "COLUMNS";
  private static final String TABLES = "TABLES";
  private static final String CONDITIONS = "CONDITIONS";

  private static final String PO_ID = "PO_ID";
  private static final String PO_NUMBER = "PO_NUMBER";
  private static final String PO_STATUS = "PO_STATUS";
  private static final String VENDOR_ID = "VENDOR_ID";
  private static final String SHIPLOC = "SHIPLOC";
  private static final String BILLLOC = "BILLLOC";

  private static final String NOTE = "NOTE";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";

  private OrderMigration(OrderContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    // String token = migrationService.okapiService.getToken(tenant);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<OrderContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (OrderJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());
      countContext.put(COLUMNS, job.getColumns());
      countContext.put(TABLES, job.getTables());
      countContext.put(CONDITIONS, job.getConditions());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      int partitions = job.getPartitions();
      int limit = (int) Math.ceil((double) count / (double) partitions);
      int offset = 0;
      for (int i = 0; i < partitions; i++) {
        Map<String, Object> partitionContext = new HashMap<String, Object>();
        partitionContext.put(SQL, context.getExtraction().getPageSql());
        partitionContext.put(SCHEMA, job.getSchema());
        partitionContext.put(COLUMNS, job.getColumns());
        partitionContext.put(TABLES, job.getTables());
        partitionContext.put(CONDITIONS, job.getConditions());
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        partitionContext.put(JOB, job);
        // partitionContext.put(TOKEN, token);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new OrderPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static OrderMigration with(OrderContext context, String tenant) {
    return new OrderMigration(context, tenant);
  }

  public class OrderPartitionTask implements PartitionTask<OrderContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final ExecutorService additionalExecutor;

    public OrderPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.additionalExecutor = Executors.newFixedThreadPool(1);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public OrderPartitionTask execute(OrderContext context) {
      long startTime = System.nanoTime();

      // String token = (String) partitionContext.get(TOKEN);

      OrderJob job = (OrderJob) partitionContext.get(JOB);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Map<String, Object> lineItemNoteContext = new HashMap<>();
      lineItemNoteContext.put(SQL, context.getExtraction().getLineItemNotesSql());
      lineItemNoteContext.put(SCHEMA, schema);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement lineItemNoteStatement = threadConnections.getLineItemNoteConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          String poId = pageResultSet.getString(PO_ID);
          String poNumber = pageResultSet.getString(PO_NUMBER);
          String poStatus = pageResultSet.getString(PO_STATUS);
          String vendorId = pageResultSet.getString(VENDOR_ID);
          String shipLoc = pageResultSet.getString(SHIPLOC);
          String billLoc = pageResultSet.getString(BILLLOC);

          lineItemNoteContext.put(PO_ID, poId);

          System.out.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s", index, schema, poId, poNumber, poStatus, vendorId, shipLoc, billLoc));

          ObjectNode po = migrationService.objectMapper.createObjectNode();

          po.put("id", UUID.randomUUID().toString());

          po.put("approved", false);
          po.put("workflowStatus", "Pending");
          po.put("manualPo", false);

          if (StringUtils.isNotEmpty(poNumber)) {
            po.put("poNumber", StringUtils.deleteWhitespace(String.format("%s%s", job.getPoNumberPrefix(), poNumber)));
          }

          if (job.getIncludeAddresses()) {
            if (StringUtils.isNotEmpty(poNumber)) {

            } else {
  
            }
  
            if (StringUtils.isNotEmpty(poNumber)) {
  
            } else {
              
            }
          }

          po.put("orderType", "Ongoing");

          Optional<ReferenceLink> vendorRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(vendorRLTypeId, vendorId);
          if (!vendorRL.isPresent()) {
            log.error("{} no vendor id found for vendor id {}", schema, vendorId);
            continue;
          }

          po.put("vendor", vendorRL.get().getFolioReference());

          ObjectNode ongoingObject = migrationService.objectMapper.createObjectNode();

          ongoingObject.put("interval", 365);
          ongoingObject.put("isSubscription", true);
          ongoingObject.put("manualRenewal", true);

          po.set("ongoing", ongoingObject);

          CompletableFuture.allOf(
            getLineItemNotes(lineItemNoteStatement, lineItemNoteContext)
              .thenAccept((notes) -> po.set("notes", notes))
          ).get();

          System.out.println(po);

        }
      } catch (SQLException | InterruptedException | ExecutionException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }
      log.info("{} {} finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((OrderPartitionTask) obj).getIndex() == this.getIndex();
    }

    private CompletableFuture<ArrayNode> getLineItemNotes(Statement statement, Map<String, Object> context) {
      CompletableFuture<ArrayNode> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        ArrayNode notes =  migrationService.objectMapper.createArrayNode();
        try (ResultSet resultSet = getResultSet(statement, context)) {
          while (resultSet.next()) {
            String note = resultSet.getString(NOTE);
            notes.add(note);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(notes);
        }
      });
      return future;
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setLineItemNoteConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private Connection lineItemNoteConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getLineItemNoteConnection() {
      return lineItemNoteConnection;
    }

    public void setLineItemNoteConnection(Connection lineItemNoteConnection) {
      this.lineItemNoteConnection = lineItemNoteConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        lineItemNoteConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
