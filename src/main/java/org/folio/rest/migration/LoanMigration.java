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
import org.folio.rest.migration.model.request.loan.LoanContext;
import org.folio.rest.migration.model.request.loan.LoanJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;

public class LoanMigration extends AbstractMigration<LoanContext> {

  private static final String CIRC_TRANSACTION_ID = "CIRC_TRANSACTION_ID";
  private static final String CHARGE_LOCATION = "CHARGE_LOCATION";
  private static final String RENEWAL_COUNT = "RENEWAL_COUNT";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PATRON_BARCODE = "PATRON_BARCODE";

  private static final String ITEM_ID = "ITEM_ID";
  private static final String ITEM_BARCODE = "ITEM_BARCODE";

  private static final String LOAN_DATE = "LOAN_DATE";
  private static final String DUE_DATE = "DUE_DATE";

  private LoanMigration(LoanContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<LoanContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (LoanJob job : context.getJobs()) {

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
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new LoanPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static LoanMigration with(LoanContext context, String tenant) {
    return new LoanMigration(context, tenant);
  }

  public class LoanPartitionTask implements PartitionTask<LoanContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public LoanPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public LoanPartitionTask execute(LoanContext context) {
      long startTime = System.nanoTime();

      LoanJob job = (LoanJob) partitionContext.get(JOB);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      try (Statement pageStatement = threadConnections.getPageConnection().createStatement();
          ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);) {

        while (pageResultSet.next()) {

          int circTransactionId = pageResultSet.getInt(CIRC_TRANSACTION_ID);
          int chargeLocation = pageResultSet.getInt(CHARGE_LOCATION);
          int renewalCount = pageResultSet.getInt(RENEWAL_COUNT);

          int patronId = pageResultSet.getInt(PATRON_ID);
          String patronBarcode = pageResultSet.getString(PATRON_BARCODE);

          int itemId = pageResultSet.getInt(ITEM_ID);
          String itemBarcode = pageResultSet.getString(ITEM_BARCODE);

          String loanDate = pageResultSet.getString(LOAN_DATE);
          String dueDate = pageResultSet.getString(DUE_DATE);

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
      return Objects.nonNull(obj) && ((LoanPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
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
