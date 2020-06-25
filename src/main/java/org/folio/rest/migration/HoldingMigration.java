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
import org.folio.rest.jaxrs.model.dto.RawRecordsDto;
import org.folio.rest.jaxrs.model.dto.RawRecordsMetadata;
import org.folio.rest.jaxrs.model.dto.RawRecordsMetadata.ContentType;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.mapping.HoldingMapper;
import org.folio.rest.migration.model.HoldingRecord;
import org.folio.rest.migration.model.request.HoldingContext;
import org.folio.rest.migration.model.request.HoldingJob;
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

public class HoldingMigration extends AbstractMigration<HoldingContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String MFHD_ID = "MFHD_ID";
  private static final String LOCATION_ID = "LOCATION_ID";

  private static final String HOLDING_REFERENCE_ID = "holdingsTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";
  private static final String PERMENENT_LOCATION_REFERENCE_ID = "permenentLocationTypeId";

  private static final String DISCOVERY_SUPPRESS = "SUPPRESS_IN_OPAC";
  private static final String CALL_NUMBER = "DISPLAY_CALL_NO";

  private static final String CALL_NUMBER_TYPE = "CALL_NO_TYPE";
  private static final String HOLDINGS_TYPE = "RECORD_TYPE";
  private static final String FIELD_008 = "FIELD_008";

  private static final String RECORD_SEGMENT = "RECORD_SEGMENT";
  private static final String SEQNUM = "SEQNUM";

  private static final String T_999 = "999";

  private static final char F = 'f';

  //(id,jsonb,creation_date,created_by,instanceid,permanentlocationid,temporarylocationid,holdingstypeid,callnumbertypeid,illpolicyid)
  private static final String HOLDING_RECORDS_COPY_SQL = "COPY %s_mod_inventory_storage.holdings_record (id,jsonb,creation_date,created_by,instanceid,permanentlocationid,holdingstypeid,callnumbertypeid) FROM STDIN";

  private static final HashMap<String, String> CALL_NUMBER_MAP = new HashMap<>();
  static {
    CALL_NUMBER_MAP.put(" ", "24badefa-4456-40c5-845c-3f45ffbc4c03");
    CALL_NUMBER_MAP.put("0", "95467209-6d7b-468b-94df-0f5d7ad2747d");
    CALL_NUMBER_MAP.put("1", "03dd64d0-5626-4ecd-8ece-4531e0069f35");
    CALL_NUMBER_MAP.put("2", "054d460d-d6b9-4469-9e37-7a78a2266655");
    CALL_NUMBER_MAP.put("3", "fc388041-6cd0-4806-8a74-ebe3b9ab4c6e");
    CALL_NUMBER_MAP.put("4", "28927d76-e097-4f63-8510-e56f2b7a3ad0");
    CALL_NUMBER_MAP.put("5", "5ba6b62e-6858-490a-8102-5b1369873835");
    CALL_NUMBER_MAP.put("6", "cd70562c-dd0b-42f6-aa80-ce803d24d4a1");
    CALL_NUMBER_MAP.put("8", "6caca63e-5651-4db6-9247-3205156e9699");
  }

  private static final String CALL_NUMBER_MAP_DEFAULT = "6caca63e-5651-4db6-9247-3205156e9699";

  private static final HashMap<String, String> HOLDINGS_TYPE_MAP = new HashMap<>();
  static {
    HOLDINGS_TYPE_MAP.put("u", "61155a36-148b-4664-bb7f-64ad708e0b32");
    HOLDINGS_TYPE_MAP.put("v", "dc35d0ae-e877-488b-8e97-6e41444e6d0a");
    HOLDINGS_TYPE_MAP.put("x", "03c9c400-b9e3-4a07-ac0e-05ab470233ed");
    HOLDINGS_TYPE_MAP.put("y", "e6da6c98-6dd0-41bc-8b4b-cfd4bbd9c3ae");
  }

  private static final HashMap<String, String> RECEIPT_STATUS_MAP = new HashMap<>();
  static {
    RECEIPT_STATUS_MAP.put("0", "Unknown");
    RECEIPT_STATUS_MAP.put("1", "Other receipt or acquisition status");
    RECEIPT_STATUS_MAP.put("2", "Received and complete or ceased");
    RECEIPT_STATUS_MAP.put("3", "On order");
    RECEIPT_STATUS_MAP.put("4", "Currently received");
    RECEIPT_STATUS_MAP.put("5", "Not currently received");
    RECEIPT_STATUS_MAP.put(" ", "Unknown");
    RECEIPT_STATUS_MAP.put("|", "Unknown");
  }

  private static final HashMap<String, String> ACQ_METHOD_MAP = new HashMap<>();
  static {
    ACQ_METHOD_MAP.put("c", "Cooperative or consortial purchase");
    ACQ_METHOD_MAP.put("d", "Deposit");
    ACQ_METHOD_MAP.put("e", "Exchange");
    ACQ_METHOD_MAP.put("f", "Free");
    ACQ_METHOD_MAP.put("g", "Gift");
    ACQ_METHOD_MAP.put("l", "Legal deposit");
    ACQ_METHOD_MAP.put("m", "Membership");
    ACQ_METHOD_MAP.put("n", "Non-library purchase");
    ACQ_METHOD_MAP.put("p", "Purchase");
    ACQ_METHOD_MAP.put("l", "Lease");
    ACQ_METHOD_MAP.put("u", "Unknown");
    ACQ_METHOD_MAP.put("z", "Other method of acquisition");
    ACQ_METHOD_MAP.put("|", "Unknown");
  }

  private static final HashMap<String, String> RETENTION_POLICY_MAP = new HashMap<>();
  static {
    RETENTION_POLICY_MAP.put(" ", "Unknown");
    RETENTION_POLICY_MAP.put("|", "Unknown");
    RETENTION_POLICY_MAP.put("0", "Unknown");
    RETENTION_POLICY_MAP.put("1", "Other general retention policy");
    RETENTION_POLICY_MAP.put("2", "Retained except as replaced by updates");
    RETENTION_POLICY_MAP.put("3", "Sample issue retained");
    RETENTION_POLICY_MAP.put("4", "Retained until replaced by microform");
    RETENTION_POLICY_MAP.put("5", "Retained until replaced by cumulation, replacement volume, or revision");
    RETENTION_POLICY_MAP.put("6", "Retained for a limited period");
    RETENTION_POLICY_MAP.put("7", "Not retained");
    RETENTION_POLICY_MAP.put("8", "Permanently retained");
  }

  private HoldingMigration(HoldingContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    JsonNode hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

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

    JsonNode instancesHridSettings = hridSettings.get("instances");

    String hridPrefix = instancesHridSettings.get("prefix").asText();

    int originalHridStartNumber = instancesHridSettings.get("startNumber").asInt();

    int hridStartNumber = originalHridStartNumber;

    int index = 0;

    log.info("total jobs: {}", context.getJobs().size());

    for (HoldingJob job : context.getJobs()) {

      log.info("starting job: {}", job.getProfileInfo().getName());

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
        partitionContext.put(TOKEN, token);
        partitionContext.put(HRID_PREFIX, hridPrefix);
        partitionContext.put(HRID_START_NUMBER, hridStartNumber);

        taskQueue.submit(new HoldingPartitionTask(migrationService, holdingMapper, partitionContext, job));
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

    private final HoldingJob job;

    private int hrid;

    public HoldingPartitionTask(MigrationService migrationService, HoldingMapper holdingMapper, Map<String, Object> partitionContext, HoldingJob job) {
      this.migrationService = migrationService;
      this.holdingMapper = holdingMapper;
      this.partitionContext = partitionContext;
      this.job = job;
      this.hrid = (int) partitionContext.get(HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public String getSchema() {
      return job.getSchema();
    }

    public HoldingPartitionTask execute(HoldingContext context) {
      long startTime = System.nanoTime();

      String schema = this.getSchema();

      int index = this.getIndex();

      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      MarcFactory factory = MarcFactory.newInstance();

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

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

          log.debug("Processing page result with holding id: {}", mfhdId);

          String locationId = pageResultSet.getString(LOCATION_ID);

          String discoverySuppressString = pageResultSet.getString(DISCOVERY_SUPPRESS);
          String callNumber = pageResultSet.getString(CALL_NUMBER);

          String callNumberType = pageResultSet.getString(CALL_NUMBER_TYPE);
          String holdingsType = pageResultSet.getString(HOLDINGS_TYPE);
          String field008 = pageResultSet.getString(FIELD_008);

          String receiptStatus = null;
          String acquisitionMethod = null;
          String retentionPolicy = null;

          Boolean discoverySuppress = null;

          if (discoverySuppressString != null) {
            if (discoverySuppressString.equalsIgnoreCase("y")) {
              discoverySuppress = true;
            } else if (discoverySuppressString.equalsIgnoreCase("n")) {
              discoverySuppress = false;
            }
          }

          if (CALL_NUMBER_MAP.containsKey(callNumberType)) {
            callNumberType = CALL_NUMBER_MAP.get(callNumberType);
          } else {
            callNumberType = CALL_NUMBER_MAP_DEFAULT;
          }

          if (holdingsType != null) {
            if (HOLDINGS_TYPE_MAP.containsKey(holdingsType)) {
              holdingsType = HOLDINGS_TYPE_MAP.get(holdingsType);
            } else {
              holdingsType = null;
            }
          }

          if (field008 != null && field008.length() >= 8) {
            receiptStatus = RECEIPT_STATUS_MAP.get(field008.substring(7, 8));

            if (field008.length() >= 9) {
              acquisitionMethod = ACQ_METHOD_MAP.get(field008.substring(8, 9));
            }

            if (field008.length() >= 14) {
              retentionPolicy = RETENTION_POLICY_MAP.get(field008.substring(13, 14));
            }
          }

          marcContext.put(MFHD_ID, mfhdId);

          try {
            String marc = getMarc(marcStatement, marcContext);

            HoldingRecord holdingRecord = new HoldingRecord(mfhdId, locationId, discoverySuppress, callNumber, callNumberType, holdingsType, receiptStatus, acquisitionMethod, retentionPolicy);

            String holdingId;
            String instanceId;
            String permanentLocationId;

            if (job.isUseReferenceLinks()) {
              String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);
              Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);
              holdingId = holdingRL.isPresent() ? holdingRL.get().getFolioReference() : UUID.randomUUID().toString();

              String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
              Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, mfhdId);
              instanceId = instanceRL.isPresent() ? instanceRL.get().getFolioReference() : UUID.randomUUID().toString();

              String permenentLocationRLTypeId = job.getReferences().get(PERMENENT_LOCATION_REFERENCE_ID);
              Optional<ReferenceLink> permanentLocationRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(permenentLocationRLTypeId, mfhdId);
              permanentLocationId = permanentLocationRL.isPresent() ? holdingRL.get().getFolioReference() : UUID.randomUUID().toString();

              holdingRecord.setHoldingId(holdingId);
              holdingRecord.setInstanceId(instanceId);
              holdingRecord.setPermanentLocationId(permanentLocationId);
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

      RawRecordsDto rawRecordsDto = new RawRecordsDto();
      RawRecordsMetadata recordsMetadata = new RawRecordsMetadata();
      recordsMetadata.setLast(true);
      recordsMetadata.setCounter(count);
      recordsMetadata.setTotal(count);
      recordsMetadata.setContentType(ContentType.MARC_RAW);
      rawRecordsDto.setRecordsMetadata(recordsMetadata);


      log.info("{} {} finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

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
