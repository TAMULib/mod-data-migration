package org.folio.rest.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
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
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import io.vertx.core.json.JsonObject;

public class HoldingMigration extends AbstractMigration<HoldingContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String LOCATION_ID = "LOCATION_ID";

  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";
  private static final String HOLDING_TO_BIB_REFERENCE_ID = "holdingToBibTypeId";

  private static final String DISCOVERY_SUPPRESS = "SUPPRESS_IN_OPAC";
  private static final String CALL_NUMBER = "DISPLAY_CALL_NO";

  private static final String CALL_NUMBER_TYPE = "CALL_NO_TYPE";
  private static final String HOLDINGS_TYPE = "RECORD_TYPE";
  private static final String FIELD_008 = "FIELD_008";

  private static final String RECORD_SEGMENT = "RECORD_SEGMENT";
  private static final String SEQNUM = "SEQNUM";

  private static final String ID = "id";
  private static final String CODE = "code";

  private static final String T_999 = "999";

  private static final char F = 'f';

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

    log.info("total holding jobs: {}", context.getJobs().size());

    for (HoldingJob job : context.getJobs()) {

      log.info("starting holding job: {}", job.getProfileInfo().getName());

      countContext.put(SCHEMA, job.getSchema());

      HashMap<String, String> locationsMap = preloadLocationsMap(voyagerSettings, migrationService, token, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} holding count: {}", job.getSchema(), count);

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

        taskQueue.submit(new HoldingPartitionTask(migrationService, holdingMapper, partitionContext, locationsMap));
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

  private HashMap<String, String> preloadLocationsMap(Database voyagerSettings, MigrationService migrationService, String token, String schema) {
    HashMap<String, String> codeToId = new HashMap<>();
    HashMap<String, String> idToUuid = new HashMap<>();

    log.info("Pre-Loading location data for schema: {}", schema);

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

  public class HoldingPartitionTask implements PartitionTask<HoldingContext> {

    private final MigrationService migrationService;

    private final HoldingMapper holdingMapper;

    private final Map<String, Object> partitionContext;

    private final Map<String, String> locationsMap;

    private int hrid;

    public HoldingPartitionTask(MigrationService migrationService, HoldingMapper holdingMapper, Map<String, Object> partitionContext, HashMap<String, String> locationsMap) {
      this.migrationService = migrationService;
      this.holdingMapper = holdingMapper;
      this.partitionContext = partitionContext;
      this.locationsMap = locationsMap;
      this.hrid = (int) partitionContext.get(HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public HoldingPartitionTask execute(HoldingContext context) {
      long startTime = System.nanoTime();

      int index = this.getIndex();

      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);
      HoldingJob job = (HoldingJob) partitionContext.get(JOB);

      String schema = job.getSchema();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      MarcFactory factory = MarcFactory.newInstance();

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

      HoldingMaps holdingMaps = context.getHoldingMaps();
      HoldingDefaults holdingDefaults = context.getHoldingDefaults();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try {
        PGCopyOutputStream holdingRecordOutput = new PGCopyOutputStream(threadConnections.getHoldingConnection(), String.format(HOLDING_RECORDS_COPY_SQL, tenant));
        PrintWriter holdingRecordWriter = new PrintWriter(holdingRecordOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement marcStatement = threadConnections.getMarcConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {
          String mfhdId = pageResultSet.getString(MFHD_ID);

          log.debug("Processing page result with mfhd id: {}", mfhdId);

          String permanentLocation = pageResultSet.getString(LOCATION_ID);
          String locationId = null;

          String discoverySuppressString = pageResultSet.getString(DISCOVERY_SUPPRESS);
          String callNumber = pageResultSet.getString(CALL_NUMBER);

          String callNumberType = pageResultSet.getString(CALL_NUMBER_TYPE);
          String holdingsType = pageResultSet.getString(HOLDINGS_TYPE);
          String field008 = pageResultSet.getString(FIELD_008);

          String receiptStatus;
          String acquisitionMethod;
          String retentionPolicy;

          Boolean discoverySuppress;

          if (discoverySuppressString != null) {
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

          if (field008 != null && field008.length() >= 8) {
            if (field008.length() >= 7) {
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
          }
          else {
            locationId = holdingDefaults.getPermanentLocationId();
          }

          marcContext.put(MFHD_ID, mfhdId);

          try {
            String marc = getMarc(marcStatement, marcContext);

            HoldingRecord holdingRecord = new HoldingRecord(mfhdId, locationId, discoverySuppress, callNumber, callNumberType, holdingsType, receiptStatus, acquisitionMethod, retentionPolicy);

            if (job.isUseReferenceLinks()) {
              String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
              Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);
              String holdingId = holdingRL.isPresent() ? holdingRL.get().getFolioReference() : UUID.randomUUID().toString();

              String instanceRLTypeId = job.getReferences().get(HOLDING_TO_BIB_REFERENCE_ID);
              Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, mfhdId);
              String instanceId = instanceRL.isPresent() ? instanceRL.get().getFolioReference() : holdingDefaults.getInstanceId();

              holdingRecord.setHoldingId(holdingId);
              holdingRecord.setInstanceId(instanceId);
            }
            else {
              holdingRecord.setHoldingId(UUID.randomUUID().toString());
              holdingRecord.setInstanceId(holdingDefaults.getInstanceId());
            }

            Optional<Record> potentialRecord = rawMarcToRecord(marc);

            if (potentialRecord.isPresent()) {
              Record record = potentialRecord.get();

              DataField dataField = getDataField(factory, record);

              record.addVariableField(dataField);

              try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                MarcWriter streamWriter = new MarcStreamWriter(os, DEFAULT_CHARSET.name());
                // use stream writer to recalculate leader
                streamWriter.write(record);
                streamWriter.close();
              } catch (IOException e) {
                e.printStackTrace();
              }

              String marcJson = recordToJson(record);

              JsonNode marcJsonNode = migrationService.objectMapper.readTree(marcJson);

              holdingRecord.setMarcRecord(record);
              holdingRecord.setMarcJson(marcJsonNode);

              Date currentDate = new Date();
              holdingRecord.setCreatedByUserId(job.getUserId());
              holdingRecord.setCreatedDate(currentDate);

              String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
              String createdByUserId = job.getUserId();

              Holdingsrecord holdingsRecord = holdingRecord.toHolding(holdingMapper, hridPrefix, hrid);

              String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(holdingRecord)));

              // TODO: validate rows
              holdingRecordWriter.println(String.join("\t", holdingsRecord.getId(), hrUtf8Json, createdAt, createdByUserId, holdingsRecord.getInstanceId(), holdingsRecord.getPermanentLocationId(), holdingsRecord.getHoldingsTypeId(), holdingsRecord.getCallNumberTypeId()));

              hrid++;
              count++;
            } else {
                log.error("{} holding id {} no record found", schema, mfhdId);
            }

          } catch (IOException e) {
              log.error("{} holding id {} error processing marc", schema, mfhdId);
          } catch (MarcException e) {
              log.error("{} holding id {} error reading marc", schema, mfhdId);
          }
        }

        holdingRecordWriter.close();

        pageStatement.close();
        marcStatement.close();

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
    threadConnections.setMarcConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setHoldingConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private String getMarc(Statement statement, Map<String, Object> context) throws SQLException, IOException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      List<SequencedMarc> marcSequence = new ArrayList<>();
      while (resultSet.next()) {
        InputStream recordSegment = resultSet.getBinaryStream(RECORD_SEGMENT);
        int seqnum = resultSet.getInt(SEQNUM);
        marcSequence.add(new SequencedMarc(seqnum, recordSegment));
      }
      List<InputStream> asciiStreams = marcSequence.stream().sorted((sm1, sm2) -> sm1.getSeqnum().compareTo(sm2.getSeqnum())).map(sm -> sm.getRecordSegment()).collect(Collectors.toList());
      SequenceInputStream sequenceInputStream = new SequenceInputStream(Collections.enumeration(asciiStreams));
      return IOUtils.toString(sequenceInputStream, DEFAULT_CHARSET);
    }
  }

  private Optional<Record> rawMarcToRecord(String rawMarc) throws IOException, MarcException {
    StringBuilder marc = new StringBuilder(rawMarc);
    // leader/22 must be 0 to avoid missing field terminator at end of directory
    marc.setCharAt(22, '0');
    try (InputStream in = new ByteArrayInputStream(marc.toString().getBytes(DEFAULT_CHARSET))) {
      MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    }
    return Optional.empty();
  }

  private String recordToJson(Record record) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      MarcJsonWriter writer = new MarcJsonWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    }
  }

  private DataField getDataField(MarcFactory factory, Record marcRecord) {
    VariableField variableField = getSingleFieldByIndicators(marcRecord.getVariableFields(T_999), F, F);
    if (variableField != null && ((DataField) variableField).getIndicator1() == F && ((DataField) variableField).getIndicator2() == F) {
      marcRecord.removeVariableField(variableField);
      return (DataField) variableField;
    } else {
      return factory.newDataField(T_999, F, F);
    }
  }

  private VariableField getSingleFieldByIndicators(List<VariableField> list, char ind1, char ind2) {
    return list.stream().filter(f -> ((DataField) f).getIndicator1() == ind1 && ((DataField) f).getIndicator2() == ind2).findFirst().orElse(null);
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

  private class SequencedMarc {
    private Integer seqnum;
    private InputStream recordSegment;

    public SequencedMarc(Integer seqnum, InputStream recordSegment) {
      this.seqnum = seqnum;
      this.recordSegment = recordSegment;
    }

    public Integer getSeqnum() {
      return seqnum;
    }

    public InputStream getRecordSegment() {
      return recordSegment;
    }
  }

}
