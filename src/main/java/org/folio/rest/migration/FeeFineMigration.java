package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.codehaus.plexus.util.StringUtils;
import org.folio.rest.jaxrs.model.Feefineactiondata;
import org.folio.rest.jaxrs.model.Feefinedata;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.FeeFineRecord;
import org.folio.rest.migration.model.request.feefine.FeeFineContext;
import org.folio.rest.migration.model.request.feefine.FeeFineDefaults;
import org.folio.rest.migration.model.request.feefine.FeeFineJob;
import org.folio.rest.migration.model.request.feefine.FeeFineMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class FeeFineMigration extends AbstractMigration<FeeFineContext> {

  private static final String PATRON_ID = "PATRON_ID";
  private static final String ITEM_ID = "ITEM_ID";
  private static final String ITEM_BARCODE = "ITEM_BARCODE";
  private static final String FINE_FEE_ID = "FINE_FEE_ID";
  private static final String AMOUNT = "AMOUNT";
  private static final String REMAINING = "REMAINING";
  private static final String FINE_FEE_TYPE = "FINE_FEE_TYPE";
  private static final String FINE_FEE_NOTE = "FINE_FEE_NOTE";
  private static final String CREATE_DATE = "CREATE_DATE";
  private static final String MFHD_ID = "MFHD_ID";
  private static final String DISPLAY_CALL_NO = "DISPLAY_CALL_NO";
  private static final String ITEM_ENUM = "ITEM_ENUM";
  private static final String CHRON = "CHRON";
  private static final String EFFECTIVE_LOCATION = "EFFECTIVE_LOCATION";
  private static final String FINE_LOCATION = "FINE_LOCATION";
  private static final String TITLE = "TITLE";
  private static final String BIB_ID = "BIB_ID";

  private static final String MTYPE_CODE = "MTYPE_CODE";

  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";
  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String ITEM_REFERENCE_ID = "itemTypeId";

  // (id,jsonb,creation_date,created_by,ownerid)
  private static String FEEFINE_COPY_SQL = "COPY %s_mod_feesfines.feefines (id,jsonb,creation_date,created_by,ownerid) FROM STDIN WITH NULL AS 'null'";

  // (id,jsonb,creation_date,created_by)
  private static String FEEFINE_ACTION_COPY_SQL = "COPY %s_mod_feesfines.feefineactions (id,jsonb,creation_date,created_by) FROM STDIN WITH NULL AS 'null'";

  private FeeFineMigration(FeeFineContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<FeeFineContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (FeeFineJob job : context.getJobs()) {

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
        taskQueue.submit(new FeeFinePartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static FeeFineMigration with(FeeFineContext context, String tenant) {
    return new FeeFineMigration(context, tenant);
  }

  public class FeeFinePartitionTask implements PartitionTask<FeeFineContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public FeeFinePartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public FeeFinePartitionTask execute(FeeFineContext context) {
      long startTime = System.nanoTime();

      FeeFineJob job = (FeeFineJob) partitionContext.get(JOB);

      FeeFineMaps maps = context.getMaps();
      FeeFineDefaults defaults = context.getDefaults();

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      Map<String, Object> materialTypeContext = new HashMap<>();
      materialTypeContext.put(SQL, context.getExtraction().getMaterialTypeSql());
      materialTypeContext.put(SCHEMA, schema);

      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
      String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String itemRLTypeId = job.getReferences().get(ITEM_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      try (
        PrintWriter feefineWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getFeefineConnection(), String.format(FEEFINE_COPY_SQL, tenant)), true);
        PrintWriter feefineActionWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getFeefineActionConnection(), String.format(FEEFINE_ACTION_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement materialTypeStatement = threadConnections.getMaterialTypeConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {
          String patronId = pageResultSet.getString(PATRON_ID);
          String itemId = pageResultSet.getString(ITEM_ID);
          String itemBarcode = pageResultSet.getString(ITEM_BARCODE);
          String finefeeId = pageResultSet.getString(FINE_FEE_ID);
          String amount = pageResultSet.getString(AMOUNT);
          String remaining = pageResultSet.getString(REMAINING);
          String finefeeType = pageResultSet.getString(FINE_FEE_TYPE);
          String finefeeNote = pageResultSet.getString(FINE_FEE_NOTE);
          String createSate = pageResultSet.getString(CREATE_DATE);
          String mfhdId = pageResultSet.getString(MFHD_ID);
          String displayCallNo = pageResultSet.getString(DISPLAY_CALL_NO);
          String itemEnum = pageResultSet.getString(ITEM_ENUM);
          String chron = pageResultSet.getString(CHRON);
          String effectiveLocation = pageResultSet.getString(EFFECTIVE_LOCATION);
          String fineLocation = pageResultSet.getString(FINE_LOCATION);
          String title = pageResultSet.getString(TITLE);
          String bibId = pageResultSet.getString(BIB_ID);

          FeeFineRecord feefineRecord = new FeeFineRecord(
            patronId,
            itemId,
            itemBarcode,
            finefeeId,
            amount,
            remaining,
            finefeeType,
            finefeeNote,
            createSate,
            mfhdId,
            displayCallNo,
            itemEnum,
            chron,
            effectiveLocation,
            fineLocation,
            title,
            bibId
          );

          if (StringUtils.isNotEmpty(itemId)) {
            materialTypeContext.put(ITEM_ID, itemId);
            feefineRecord.setMaterialType(getMaterialType(materialTypeStatement, materialTypeContext));
            feefineRecord.setInstanceRL(migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, bibId));
            feefineRecord.setHoldingRL(migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId));
            feefineRecord.setItemRL(migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(itemRLTypeId, itemId));
          }

          Feefinedata feefine = feefineRecord.toFeefine(maps, defaults);

          Feefineactiondata feefineaction = feefineRecord.toFeefineaction(maps, defaults);

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
      return Objects.nonNull(obj) && ((FeeFinePartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private Optional<String> getMaterialType(Statement statement, Map<String, Object> context) {
    Optional<String> materialType = Optional.empty();
      try (ResultSet resultSet = getResultSet(statement, context)) {
        while (resultSet.next()) {
          materialType = Optional.ofNullable(resultSet.getString(MTYPE_CODE));
          break;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {

      }
    return materialType;
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setMaterialTypeConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setFeefineConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    try {
      threadConnections.setFeefineActionConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;
    private Connection materialTypeConnection;

    private BaseConnection feefineConnection;
    private BaseConnection feefineActionConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getMaterialTypeConnection() {
      return materialTypeConnection;
    }

    public void setMaterialTypeConnection(Connection materialTypeConnection) {
      this.materialTypeConnection = materialTypeConnection;
    }

    public BaseConnection getFeefineConnection() {
      return feefineConnection;
    }

    public void setFeefineConnection(BaseConnection feefineConnection) {
      this.feefineConnection = feefineConnection;
    }

    public BaseConnection getFeefineActionConnection() {
      return feefineActionConnection;
    }

    public void setFeefineActionConnection(BaseConnection feefineActionConnection) {
      this.feefineActionConnection = feefineActionConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        materialTypeConnection.close();
        feefineConnection.close();
        feefineActionConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
