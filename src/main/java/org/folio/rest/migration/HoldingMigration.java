package org.folio.rest.migration;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
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
import org.folio.rest.migration.model.request.holding.HoldingContext;
import org.folio.rest.migration.model.request.holding.HoldingDefaults;
import org.folio.rest.migration.model.request.holding.HoldingJob;
import org.folio.rest.migration.model.request.holding.HoldingMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.marc4j.MarcException;
import org.marc4j.marc.Record;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import io.vertx.core.json.JsonObject;

public class HoldingMigration extends AbstractMigration<HoldingContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String LOCATIONS_MAP = "LOCATIONS_MAP";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";
  private static final String DISCOVERY_SUPPRESS = "SUPPRESS_IN_OPAC";
  private static final String CALL_NUMBER = "DISPLAY_CALL_NO";
  private static final String CALL_NUMBER_TYPE = "CALL_NO_TYPE";
  private static final String HOLDINGS_TYPE = "RECORD_TYPE";
  private static final String FIELD_008 = "FIELD_008";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";

  // (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
  private static final String HOLDING_RECORDS_COPY_SQL = "COPY %s_mod_inventory_storage.holdings_record (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid) FROM STDIN WITH NULL AS 'null'";

  private HoldingMigration(HoldingContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    log.info("available processors: {}", Runtime.getRuntime().availableProcessors());

    String token = migrationService.okapiService.getToken(tenant);

    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);

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

      Map<String, String> locationsMap = getLocationsMap(locations, job.getSchema());

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
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
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

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

      String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String holdingToBibRLTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try (
        PrintWriter holdingRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getHoldingConnection(), String.format(HOLDING_RECORDS_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement marcStatement = threadConnections.getMarcConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String mfhdId = pageResultSet.getString(MFHD_ID);

          String permanentLocationId = pageResultSet.getString(LOCATION_ID);

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

          String permanentLocationIdKey = String.format(KEY_TEMPLATE, schema, permanentLocationId);
          if (locationsMap.containsKey(permanentLocationIdKey)) {
            locationId = locationsMap.get(permanentLocationIdKey);
          } else {
            log.warn("using default permanent location for schema {} mfhdId {} location {}", schema, mfhdId, permanentLocationId);
            locationId = holdingDefaults.getPermanentLocationId();
          }

          marcContext.put(MFHD_ID, mfhdId);

          try {
            String marc = getMarc(marcStatement, marcContext);

            Optional<Record> potentialRecord = rawMarcToRecord(marc);

            if (!potentialRecord.isPresent()) {
              log.error("schema {}, mfhd id {}, marc {} unable to read record", schema, mfhdId, marc);
              continue;
            }

            HoldingRecord holdingRecord = new HoldingRecord(holdingMaps, potentialRecord.get(), mfhdId, locationId, discoverySuppress, callNumber, callNumberType, holdingsType, receiptStatus, acquisitionMethod, retentionPolicy);

            String holdingId = null, instanceId = null;

            Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);

            if (holdingRL.isPresent()) {

              holdingId = holdingRL.get().getFolioReference();

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

            Date createdDate = new Date();
            holdingRecord.setCreatedByUserId(job.getUserId());
            holdingRecord.setCreatedDate(createdDate);

            String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
            String createdByUserId = job.getUserId();

            Holdingsrecord holdingsRecord = holdingRecord.toHolding(holdingMapper, hridPrefix, hrid);

            String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(holdingsRecord)));

            // (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
            holdingRecordWriter.println(String.join("\t",
              holdingsRecord.getId(),
              hrUtf8Json,
              createdAt,
              createdByUserId,
              holdingsRecord.getInstanceId(),
              holdingsRecord.getPermanentLocationId(),
              Objects.nonNull(holdingsRecord.getTemporaryLocationId()) ? holdingsRecord.getTemporaryLocationId() : NULL,
              holdingsRecord.getHoldingsTypeId(),
              holdingsRecord.getCallNumberTypeId(),
              Objects.nonNull(holdingsRecord.getIllPolicyId()) ? holdingsRecord.getIllPolicyId() : NULL
            ));

            hrid++;
            count++;

          } catch (IOException e) {
              log.error("{} holding id {} error processing marc", schema, mfhdId);
              log.debug(e.getMessage());
          } catch (MarcException e) {
              log.error("{} holding id {} error reading marc", schema, mfhdId);
              log.debug(e.getMessage());
          }
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} holding finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((HoldingPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setMarcConnection(getConnection(voyagerSettings));
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
    private Connection marcConnection;

    private BaseConnection holdingConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getMarcConnection() {
      return marcConnection;
    }

    public void setMarcConnection(Connection marcConnection) {
      this.marcConnection = marcConnection;
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
        marcConnection.close();
        holdingConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  private Map<String, String> getLocationsMap(Locations locations, String schema) {
    Map<String, String> idToUuid = new HashMap<>();
    Map<String, Object> locationContext = new HashMap<>();
    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<String, String> locConv = context.getMaps().getLocation(); 
    try(
      Connection voyagerConnection = getConnection(voyagerSettings);
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, locationContext);
    ) {
      while (rs.next()) {
        String id = rs.getString(LOCATION_ID);
        if (Objects.nonNull(id)) {
          String key = String.format(KEY_TEMPLATE, schema, id);
          String code = locConv.containsKey(key) ? locConv.get(key) : rs.getString(LOCATION_CODE);
          Optional<Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code)).findFirst();
          if (location.isPresent()) {
            idToUuid.put(key, location.get().getId());
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToUuid;
  }

}
