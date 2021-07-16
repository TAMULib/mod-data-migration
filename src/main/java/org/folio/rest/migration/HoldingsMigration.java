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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import org.apache.commons.lang3.StringUtils;
import org.folio.Location;
import org.folio.Locations;
import org.folio.Statisticalcodes;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.mapping.HoldingMapper;
import org.folio.rest.migration.model.HoldingsRecord;
import org.folio.rest.migration.model.request.holdings.HoldingsContext;
import org.folio.rest.migration.model.request.holdings.HoldingsDefaults;
import org.folio.rest.migration.model.request.holdings.HoldingsJob;
import org.folio.rest.migration.model.request.holdings.HoldingsMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.marc4j.MarcException;
import org.marc4j.marc.Record;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import io.vertx.core.json.JsonObject;

public class HoldingsMigration extends AbstractMigration<HoldingsContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String STATISTICAL_CODES = "STATISTICAL_CODES";

  private static final String LOCATIONS_MAP = "LOCATIONS_MAP";

  private static final String USER_ID = "USER_ID";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String OPERATOR_ID = "OPERATOR_ID";
  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";
  private static final String SUPPRESS_IN_OPAC = "SUPPRESS_IN_OPAC";
  private static final String CALL_NUMBER = "DISPLAY_CALL_NO";
  private static final String CALL_NUMBER_TYPE = "CALL_NO_TYPE";
  private static final String HOLDINGS_TYPE = "RECORD_TYPE";

  private static final String RECEIPT_STATUS = "RECEIPT_STATUS";
  private static final String ACQ_METHOD = "ACQ_METHOD";
  private static final String RETENTION = "RETENTION";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";

  private static final String HOLDING_TO_CALL_NUMBER_ID = "holdingToCallNumberTypeId";
  private static final String HOLDING_TO_CALL_NUMBER_PREFIX_ID = "holdingToCallNumberPrefixTypeId";
  private static final String HOLDING_TO_CALL_NUMBER_SUFFIX_ID = "holdingToCallNumberSuffixTypeId";

  // (id,external_reference,folio_reference,type_id)
  private static String REFERENCE_LINK_COPY_SQL = "COPY %s.reference_links (id,external_reference,folio_reference,type_id) FROM STDIN WITH NULL AS 'null'";

  // (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
  private static final String HOLDING_RECORDS_COPY_SQL = "COPY %s_mod_inventory_storage.holdings_record (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid) FROM STDIN WITH NULL AS 'null'";

  private HoldingsMigration(HoldingsContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);
    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);
    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);
    Statisticalcodes statisticalcodes = migrationService.okapiService.fetchStatisticalCodes(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    HoldingMapper holdingMapper = new HoldingMapper();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<HoldingsContext>(context, new TaskCallback() {

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

    JsonObject holdingsHridSettings = hridSettings.getJsonObject("holdings");
    String hridPrefix = holdingsHridSettings.getString(PREFIX);
    int hridStartNumber = holdingsHridSettings.getInteger(START_NUMBER);

    int index = 0;

    for (HoldingsJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      Map<String, String> locationsMap = getLocationsMap(locations, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      Userdata user = migrationService.okapiService.lookupUserByUsername(tenant, token, job.getUser());

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
        partitionContext.put(STATISTICAL_CODES, statisticalcodes);
        partitionContext.put(USER_ID, user.getId());
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new HoldingPartitionTask(migrationService, holdingMapper, partitionContext));
        offset += limit;
        hridStartNumber += limit;
        index++;
      }
    }

    holdingsHridSettings.put(START_NUMBER, hridStartNumber);

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static HoldingsMigration with(HoldingsContext context, String tenant) {
    return new HoldingsMigration(context, tenant);
  }

  public class HoldingPartitionTask implements PartitionTask<HoldingsContext> {

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

    public HoldingPartitionTask execute(HoldingsContext context) {
      long startTime = System.nanoTime();

      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      HoldingsJob job = (HoldingsJob) partitionContext.get(JOB);

      Map<String, String> locationsMap = (Map<String, String>) partitionContext.get(LOCATIONS_MAP);

      Statisticalcodes statisticalcodes = (Statisticalcodes) partitionContext.get(STATISTICAL_CODES);

      String userId = (String) partitionContext.get(USER_ID);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database referenceLinkSettings = migrationService.referenceLinkSettings;

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      HoldingsMaps holdingMaps = context.getMaps();
      HoldingsDefaults holdingDefaults = context.getDefaults();

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

      String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
      String holdingToBibRLTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);

      String holdingToCallNumberTypeId = job.getReferences().get(HOLDING_TO_CALL_NUMBER_ID);
      String holdingToCallNumberPrefixTypeId = job.getReferences().get(HOLDING_TO_CALL_NUMBER_PREFIX_ID);
      String holdingToCallNumberSuffixTypeId = job.getReferences().get(HOLDING_TO_CALL_NUMBER_SUFFIX_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, referenceLinkSettings, folioSettings);

      String tenantSchema = migrationService.schemaService.getSchema(tenant);

      int count = 0;

      try (
        PrintWriter referenceLinkWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getReferenceLinkConnection(), String.format(REFERENCE_LINK_COPY_SQL, tenantSchema)), true);  
        PrintWriter holdingsRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getHoldingConnection(), String.format(HOLDING_RECORDS_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement marcStatement = threadConnections.getMarcConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          final String mfhdId = pageResultSet.getString(MFHD_ID);
          final String operatorId = pageResultSet.getString(OPERATOR_ID);

          final String permanentLocationId = pageResultSet.getString(LOCATION_ID);

          final String suppressInOpac = pageResultSet.getString(SUPPRESS_IN_OPAC);
          final String displayCallNumber = pageResultSet.getString(CALL_NUMBER);

          final String callNumberType = pageResultSet.getString(CALL_NUMBER_TYPE);
          final String holdingsType = pageResultSet.getString(HOLDINGS_TYPE);

          final String receiptStatusCode = pageResultSet.getString(RECEIPT_STATUS);
          final String acqMethodCode = pageResultSet.getString(ACQ_METHOD);
          final String retentionCode = pageResultSet.getString(RETENTION);

          Map<String, String> row = new HashMap<>() {{
            put(MFHD_ID, mfhdId);
            put(OPERATOR_ID, operatorId);
            put(SUPPRESS_IN_OPAC, suppressInOpac);
            put(LOCATION_ID, permanentLocationId);
            put(SUPPRESS_IN_OPAC, suppressInOpac);
            put(CALL_NUMBER, displayCallNumber);
            put(CALL_NUMBER_TYPE, callNumberType);
            put(HOLDINGS_TYPE, holdingsType);
            put(RECEIPT_STATUS, receiptStatusCode);
            put(ACQ_METHOD, acqMethodCode);
            put(RETENTION, retentionCode);
          }};

          if (exclude(job.getExclusions(), row)) {
            continue;
          }

          marcContext.put(MFHD_ID, mfhdId);

          String receiptStatus = holdingMaps.getReceiptStatus().containsKey(receiptStatusCode)
            ? holdingMaps.getReceiptStatus().get(receiptStatusCode)
            : holdingDefaults.getReceiptStatus();
          String acquisitionMethod = holdingMaps.getAcqMethod().containsKey(acqMethodCode)
            ? holdingMaps.getAcqMethod().get(acqMethodCode)
            : holdingDefaults.getAcqMethod();
          String retentionPolicy = holdingMaps.getRetentionPolicy().containsKey(retentionCode)
            ? holdingMaps.getRetentionPolicy().get(retentionCode)
            : holdingDefaults.getRetentionPolicy();

          String locationId;
          Boolean discoverySuppress;

          if (StringUtils.isNotEmpty(suppressInOpac)) {
            if (suppressInOpac.equalsIgnoreCase("y")) {
              discoverySuppress = true;
            } else if (suppressInOpac.equalsIgnoreCase("n")) {
              discoverySuppress = false;
            } else {
              discoverySuppress = holdingDefaults.getDiscoverySuppress();
            }
          } else {
            discoverySuppress = holdingDefaults.getDiscoverySuppress();
          }

          String cnType = callNumberType;
          if (StringUtils.isNotEmpty(cnType) && holdingMaps.getCallNumberType().containsKey(cnType)) {
            cnType = holdingMaps.getCallNumberType().get(cnType);
          } else {
            cnType = holdingDefaults.getCallNumberTypeId();
          }

          String hrType = holdingsType;
          if (holdingMaps.getHoldingsType().containsKey(hrType)) {
            hrType = holdingMaps.getHoldingsType().get(hrType);
          } else {
            hrType = holdingDefaults.getHoldingsTypeId();
          }

          if (locationsMap.containsKey(permanentLocationId)) {
            locationId = locationsMap.get(permanentLocationId);
          } else {
            log.warn("using default permanent location for schema {} mfhdId {} location {}", schema, mfhdId, permanentLocationId);
            locationId = holdingDefaults.getPermanentLocationId();
          }

          Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);
          Optional<ReferenceLink> instanceRL = Optional.empty();

          if (holdingRL.isPresent()) {
            Optional<ReferenceLink> holdingToBibRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingToBibRLTypeId, holdingRL.get().getId());
            if (holdingToBibRL.isPresent()) {
              instanceRL = migrationService.referenceLinkRepo.findById(holdingToBibRL.get().getFolioReference());
            }
          } else {
            log.error("{} no holdings record id found for mfhd id {}", schema, mfhdId);
            continue;
          }

          if (!instanceRL.isPresent()) {
            log.error("{} no instance id found for mfhd id {}", schema, mfhdId);
            continue;
          }

          String holdingId = holdingRL.get().getFolioReference();
          String instanceId = instanceRL.get().getFolioReference();

          try {
            String marc = getMarc(marcStatement, marcContext);

            Optional<Record> potentialRecord = rawMarcToRecord(marc);

            if (!potentialRecord.isPresent()) {
              log.error("schema {}, mfhd id {}, marc {} unable to read record", schema, mfhdId, marc);
              continue;
            }

            Set<String> matchedCodes;
            if (StringUtils.isNotEmpty(operatorId)) {
              String opId = operatorId;
              if (holdingMaps.getStatisticalCode().containsKey(opId)) {
                opId = holdingMaps.getStatisticalCode().get(opId);
              }
              matchedCodes = getMatchingStatisticalCodes(opId, statisticalcodes);
            } else {
              matchedCodes = new HashSet<>();
            }

            HoldingsRecord holdingsRecord = new HoldingsRecord(holdingMaps, potentialRecord.get(), mfhdId, locationId, matchedCodes, discoverySuppress, displayCallNumber, cnType, hrType, receiptStatus, acquisitionMethod, retentionPolicy);

            holdingsRecord.setHoldingId(holdingId);
            holdingsRecord.setInstanceId(instanceId);

            Date createdDate = new Date();
            holdingsRecord.setCreatedByUserId(userId);
            holdingsRecord.setCreatedDate(createdDate);

            String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
            String createdByUserId = userId;

            String hridString = String.format(HRID_TEMPLATE, hridPrefix, hrid);

            Holdingsrecord holdingsrecord = holdingsRecord.toHolding(holdingMapper, holdingMaps, hridString);

            String callNumber = holdingsrecord.getCallNumber();
            String callNumberPrefix = holdingsrecord.getCallNumberPrefix();
            String callNumberSuffix = holdingsrecord.getCallNumberSuffix();

            if (StringUtils.isNoneEmpty(callNumber)) {
              String rlId = UUID.randomUUID().toString();
              String holdingRlId = holdingRL.get().getId();
              referenceLinkWriter.println(String.join("\t", rlId, holdingRlId, callNumber, holdingToCallNumberTypeId));
            }

            if (StringUtils.isNoneEmpty(callNumberPrefix)) {
              String rlId = UUID.randomUUID().toString();
              String holdingRlId = holdingRL.get().getId();
              referenceLinkWriter.println(String.join("\t", rlId, holdingRlId, callNumberPrefix, holdingToCallNumberPrefixTypeId));
            }

            if (StringUtils.isNoneEmpty(callNumberSuffix)) {
              String rlId = UUID.randomUUID().toString();
              String holdingRlId = holdingRL.get().getId();
              referenceLinkWriter.println(String.join("\t", rlId, holdingRlId, callNumberSuffix, holdingToCallNumberSuffixTypeId));
            }

            String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(holdingsrecord)));

            // (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
            holdingsRecordWriter.println(String.join("\t",
              holdingsrecord.getId(),
              hrUtf8Json,
              createdAt,
              createdByUserId,
              holdingsrecord.getInstanceId(),
              holdingsrecord.getPermanentLocationId(),
              Objects.nonNull(holdingsrecord.getTemporaryLocationId()) ? holdingsrecord.getTemporaryLocationId() : NULL,
              holdingsrecord.getHoldingsTypeId(),
              holdingsrecord.getCallNumberTypeId(),
              Objects.nonNull(holdingsrecord.getIllPolicyId()) ? holdingsrecord.getIllPolicyId() : NULL
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

  private Map<String, String> getLocationsMap(Locations locations, String schema) {
    Map<String, String> idToUuid = new HashMap<>();
    Map<String, Object> locationContext = new HashMap<>();
    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<String, String> locConv = context.getMaps().getLocation().get(schema);
    try(
      Connection voyagerConnection = getConnection(voyagerSettings);
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, locationContext);
    ) {
      while (rs.next()) {
        String id = rs.getString(LOCATION_ID);
        if (Objects.nonNull(id)) {
          String code = locConv.containsKey(id) ? locConv.get(id) : rs.getString(LOCATION_CODE);
          Optional<Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code)).findFirst();
          if (location.isPresent()) {
            idToUuid.put(id, location.get().getId());
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToUuid;
  }

  private Set<String> getMatchingStatisticalCodes(String operatorId, Statisticalcodes statisticalcodes) {
    return statisticalcodes.getStatisticalCodes().stream()
      .filter(sc -> sc.getCode().equals(operatorId))
      .map(sc -> sc.getId())
      .collect(Collectors.toSet());
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database referenceLinkSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setMarcConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setReferenceLinkConnection(getConnection(referenceLinkSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
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

    private BaseConnection referenceLinkConnection;

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

    public BaseConnection getReferenceLinkConnection() {
      return referenceLinkConnection;
    }

    public void setReferenceLinkConnection(BaseConnection referenceLinkConnection) {
      this.referenceLinkConnection = referenceLinkConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        marcConnection.close();
        holdingConnection.close();
        referenceLinkConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
