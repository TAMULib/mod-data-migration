package org.folio.rest.migration;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.Holdingsrecord;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Locations;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.mapping.HoldingMapper;
import org.folio.rest.migration.model.HoldingRecord;
import org.folio.rest.migration.model.request.HoldingContext;
import org.folio.rest.migration.model.request.HoldingDefaults;
import org.folio.rest.migration.model.request.HoldingJob;
import org.folio.rest.migration.model.request.HoldingMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.marc4j.MarcException;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import io.vertx.core.json.JsonObject;

public class HoldingMigration extends AbstractMigration<HoldingContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String LOCATIONS_MAP = "LOCATIONS_MAP";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String LOCATION_ID = "LOCATION_ID";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";

  private static final String DISCOVERY_SUPPRESS = "SUPPRESS_IN_OPAC";
  private static final String CALL_NUMBER = "DISPLAY_CALL_NO";

  private static final String CALL_NUMBER_TYPE = "CALL_NO_TYPE";
  private static final String HOLDINGS_TYPE = "RECORD_TYPE";
  private static final String FIELD_008 = "FIELD_008";

  private static final String ID = "id";
  private static final String CODE = "code";

  //(id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
  private static final String HOLDING_RECORDS_COPY_SQL = "COPY %s_mod_inventory_storage.holdings_record (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,holdingstypeid,callnumbertypeid) FROM STDIN";

  private HoldingMigration(HoldingContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    HoldingMapper holdingMapper = new HoldingMapper();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<HoldingContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    JsonObject holdingsHridSettings = hridSettings.getJsonObject("holdings");
    String hridPrefix = holdingsHridSettings.getString("prefix");

    int originalHridStartNumber = holdingsHridSettings.getInteger("startNumber");
    int hridStartNumber = originalHridStartNumber;

    int index = 0;

    for (HoldingJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      HashMap<String, String> locationsMap = preloadLocationsMap(voyagerSettings, migrationService, token, job.getSchema());

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
        partitionContext.put(TOKEN, token);
        partitionContext.put(HRID_PREFIX, hridPrefix);
        partitionContext.put(HRID_START_NUMBER, hridStartNumber);
        partitionContext.put(JOB, job);
        partitionContext.put(LOCATIONS_MAP, locationsMap);
        taskQueue.submit(new HoldingPartitionTask(migrationService, holdingMapper, partitionContext));
        offset += limit;
        index++;
        if (i < partitions) {
          hridStartNumber += limit;
        } else {
          hridStartNumber = originalHridStartNumber + count;
        }
      }
    }

    return CompletableFuture.completedFuture(true);
  }

  public static HoldingMigration with(HoldingContext context, String tenant) {
    return new HoldingMigration(context, tenant);
  }

  public class HoldingPartitionTask implements PartitionTask<HoldingContext> {

    private final MigrationService migrationService;

    private final HoldingMapper holdingMapper;

    private final Map<String, Object> partitionContext;

    private int hrid;

    public HoldingPartitionTask(MigrationService migrationService, HoldingMapper holdingMapper, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.holdingMapper = holdingMapper;
      this.partitionContext = partitionContext;
      this.hrid = (int) partitionContext.get(HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public HoldingPartitionTask execute(HoldingContext context) {
      long startTime = System.nanoTime();

      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      HoldingJob job = (HoldingJob) partitionContext.get(JOB);

      Map<String, String> locationsMap = (Map<String, String>) partitionContext.get(LOCATIONS_MAP);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      HoldingMaps holdingMaps = context.getMaps();
      HoldingDefaults holdingDefaults = context.getDefaults();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      log.info("starting {} {}", schema, index);

      int count = 0;

      try {
        PGCopyOutputStream holdingRecordOutput = new PGCopyOutputStream(threadConnections.getHoldingConnection(), String.format(HOLDING_RECORDS_COPY_SQL, tenant));
        PrintWriter holdingRecordWriter = new PrintWriter(holdingRecordOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {
          String mfhdId = pageResultSet.getString(MFHD_ID);

          String permanentLocation = pageResultSet.getString(LOCATION_ID);

          String discoverySuppressString = pageResultSet.getString(DISCOVERY_SUPPRESS);
          String callNumber = pageResultSet.getString(CALL_NUMBER);

          String callNumberType = pageResultSet.getString(CALL_NUMBER_TYPE);
          String holdingsType = pageResultSet.getString(HOLDINGS_TYPE);
          String field008 = pageResultSet.getString(FIELD_008);

          String locationId;
          String receiptStatus;
          String acquisitionMethod;
          String retentionPolicy;

          Boolean discoverySuppress;

          if (Objects.nonNull(discoverySuppressString)) {
            if (discoverySuppressString.equalsIgnoreCase("y")) {
              discoverySuppress = true;
            } else if (discoverySuppressString.equalsIgnoreCase("n")) {
              discoverySuppress = false;
            } else {
              discoverySuppress = holdingDefaults.getDiscoverySuppress();
            }
          } else {
            discoverySuppress = holdingDefaults.getDiscoverySuppress();
          }

          if (holdingMaps.getCallNumberType().containsKey(callNumberType)) {
            callNumberType = holdingMaps.getCallNumberType().get(callNumberType);
          } else {
            callNumberType = holdingDefaults.getCallNumberTypeId();
          }

          if (holdingMaps.getHoldingsType().containsKey(holdingsType)) {
            holdingsType = holdingMaps.getHoldingsType().get(holdingsType);
          } else {
            holdingsType = holdingDefaults.getHoldingsTypeId();
          }

          if (Objects.nonNull(field008) && field008.length() >= 7) {
            if (field008.length() >= 8) {
              receiptStatus = holdingMaps.getAcqMethod().get(field008.substring(7, 8));
            } else {
              receiptStatus = holdingDefaults.getReceiptStatus();
            }

            if (field008.length() >= 9) {
              acquisitionMethod = holdingMaps.getAcqMethod().get(field008.substring(8, 9));
            } else {
              acquisitionMethod = holdingDefaults.getAcqMethod();
            }

            if (field008.length() >= 14) {
              retentionPolicy = holdingMaps.getRetentionPolicy().get(field008.substring(13, 14));
            } else {
              retentionPolicy = holdingDefaults.getRetentionPolicy();
            }
          } else {
            receiptStatus = holdingDefaults.getReceiptStatus();
            acquisitionMethod = holdingDefaults.getAcqMethod();
            retentionPolicy = holdingDefaults.getRetentionPolicy();
          }

          if (locationsMap.containsKey(permanentLocation)) {
            locationId = locationsMap.get(permanentLocation);
          } else {
            locationId = holdingDefaults.getPermanentLocationId();
          }

          try {
            HoldingRecord holdingRecord = new HoldingRecord(mfhdId, locationId, discoverySuppress, callNumber, callNumberType, holdingsType, receiptStatus, acquisitionMethod, retentionPolicy);

            String holdingId = null, instanceId = null;
            
            String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
            Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);

            if (holdingRL.isPresent()) {

              holdingId = holdingRL.get().getFolioReference();

              String holdingToBibRLTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);
              Optional<ReferenceLink> holdingToBibRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingToBibRLTypeId, holdingRL.get().getId());

              if (holdingToBibRL.isPresent()) {
                Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findById(holdingToBibRL.get().getFolioReference());

                if (instanceRL.isPresent()) {
                  instanceId = instanceRL.get().getFolioReference();
                }
              }
            }

            if (Objects.isNull(holdingId)) {
              log.error("{} no holdings record id found for mfhd id {}", schema, mfhdId);
              continue;
            }

            if (Objects.isNull(instanceId)) {
              log.error("{} no instance id found for mfhd id {}", schema, mfhdId);
              continue;
            }

            holdingRecord.setHoldingId(holdingId);
            holdingRecord.setInstanceId(instanceId);

            holdingRecord.setCreatedByUserId(job.getUserId());
            holdingRecord.setCreatedDate(new Date());

            String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
            String createdByUserId = job.getUserId();

            Holdingsrecord holdingsRecord = holdingRecord.toHolding(holdingMapper, hridPrefix, hrid);

            String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(holdingsRecord)));

            // TODO: validate rows
            holdingRecordWriter.println(String.join("\t", holdingsRecord.getId(), hrUtf8Json, createdAt, createdByUserId, holdingsRecord.getInstanceId(), holdingsRecord.getPermanentLocationId(), holdingsRecord.getHoldingsTypeId(), holdingsRecord.getCallNumberTypeId()));

            hrid++;
            count++;

          } catch (IOException e) {
              log.error("{} holding id {} error processing marc", schema, mfhdId);
          } catch (MarcException e) {
              log.error("{} holding id {} error reading marc", schema, mfhdId);
          }
        }

        holdingRecordWriter.close();

        pageStatement.close();

        pageResultSet.close();

      } catch (SQLException e) {
        e.printStackTrace();
      }

      threadConnections.closeAll();

      log.info("{} {} holding finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && ((HoldingPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setHoldingConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {
    private Connection pageConnection;

    private BaseConnection holdingConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public BaseConnection getHoldingConnection() {
      return holdingConnection;
    }

    public void setHoldingConnection(BaseConnection holdingConnection) {
      this.holdingConnection = holdingConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        holdingConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  private HashMap<String, String> preloadLocationsMap(Database voyagerSettings, MigrationService migrationService, String token, String schema) {
    HashMap<String, String> codeToId = new HashMap<>();
    HashMap<String, String> idToUuid = new HashMap<>();

    Connection voyagerConnection = getConnection(voyagerSettings);

    Map<String, Object> locationContext = new HashMap<>();

    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);

    try {
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, locationContext);

      while (rs.next()) {
        String id = rs.getString(ID);
        String code = rs.getString(CODE);

        if (id != null) {
          codeToId.put(code, id);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (!voyagerConnection.isClosed()) {
          voyagerConnection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);

    for (Location location : locations.getLocations()) {
      if (codeToId.containsKey(location.getCode())) {
        idToUuid.put(codeToId.get(location.getCode()), location.getId());
      }
    }

    return idToUuid;
  }

}
