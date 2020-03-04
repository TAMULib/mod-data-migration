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
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.mapping.InstanceMapper;
import org.folio.rest.migration.mapping.MappingParameters;
import org.folio.rest.migration.model.BibRecord;
import org.folio.rest.migration.model.generated.inventory_storage.Instance;
import org.folio.rest.migration.model.generated.source_record_manager.InitJobExecutionsRqDto;
import org.folio.rest.migration.model.generated.source_record_manager.InitJobExecutionsRqDto.SourceType;
import org.folio.rest.migration.model.generated.source_record_manager.InitJobExecutionsRsDto;
import org.folio.rest.migration.model.generated.source_record_manager.JobExecution;
import org.folio.rest.migration.model.generated.source_record_manager.RawRecordsDto;
import org.folio.rest.migration.model.generated.source_record_manager.RawRecordsMetadata;
import org.folio.rest.migration.model.generated.source_record_manager.RawRecordsMetadata.ContentType;
import org.folio.rest.migration.model.generated.source_record_storage.ParsedRecord;
import org.folio.rest.migration.model.generated.source_record_storage.RawRecord;
import org.folio.rest.migration.model.generated.source_record_storage.RecordModel;
import org.folio.rest.migration.model.request.BibContext;
import org.folio.rest.migration.model.request.BibJob;
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

public class BibMigration extends AbstractMigration<BibContext> {

  private static String HRID_PREFIX = "HRID_PREFIX";
  private static String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static String BIB_ID = "BIB_ID";
  private static String SUPPRESS_IN_OPAC = "SUPPRESS_IN_OPAC";

  private static String SOURCE_RECORD_REFERENCE_ID = "sourceRecordTypeId";
  private static String INSTANCE_REFERENCE_ID = "instanceTypeId";

  public static String RECORD_SEGMENT = "RECORD_SEGMENT";
  public static String SEQNUM = "SEQNUM";

  public static String EMPTY_JSON = "{}";

  public static String T_999 = "999";

  public static char F = 'f';
  public static char I = 'i';
  public static char S = 's';

  // (_id,jsonb,creation_date,created_by)
  private static String RAW_RECORDS_COPY_SQL = "COPY tern_mod_source_record_storage.raw_records (_id,jsonb,creation_date,created_by) FROM STDIN";

  // (_id,jsonb,creation_date,created_by)
  private static String PARSED_RECORDS_COPY_SQL = "COPY tern_mod_source_record_storage.marc_records (_id,jsonb,creation_date,created_by) FROM STDIN";

  // (_id,jsonb,creation_date,created_by,jobexecutionid)
  private static String RECORDS_COPY_SQL = "COPY tern_mod_source_record_storage.records (_id,jsonb,creation_date,created_by) FROM STDIN";

  // (id,jsonb,creation_date,created_by,instancestatusid,modeofissuanceid,instancetypeid)
  private static String INSTANCE_COPY_SQL = "COPY tern_mod_inventory_storage.instance (id,jsonb,creation_date,created_by,instancetypeid) FROM STDIN";

  private final BibContext context;

  private final String tenant;

  private BibMigration(BibContext context, String tenant) {
    this.context = context;
    this.tenant = tenant;
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    MappingParameters mappingParameters = migrationService.okapiService.getMappingParamaters(tenant, token);

    JsonNode rules = migrationService.okapiService.fetchRules(tenant, token);

    JsonNode hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();

    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    InstanceMapper instanceMapper = new InstanceMapper(mappingParameters, migrationService.objectMapper, rules);

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<BibContext>(context, new TaskCallback() {

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

    for (BibJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      int partitions = job.getPartitions();
      int limit = count / partitions;
      if (limit * partitions < count) {
        limit++;
      }
      int offset = 0;
      for (int i = 0; i <= partitions; i++) {
        Map<String, Object> partitionContext = new HashMap<String, Object>();
        partitionContext.put(SQL, context.getExtraction().getPageSql());
        partitionContext.put(SCHEMA, job.getSchema());
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        partitionContext.put(TOKEN, token);
        partitionContext.put(HRID_PREFIX, hridPrefix);
        partitionContext.put(HRID_START_NUMBER, hridStartNumber);
        taskQueue.submit(new BibPartitionTask(migrationService, instanceMapper, partitionContext, job));
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

  public static BibMigration with(BibContext context, String tenant) {
    return new BibMigration(context, tenant);
  }

  private void preActions(Database settings, List<String> preActions) {
    preActions.stream().forEach(actionSql -> action(settings, actionSql));
  }

  private void postActions(Database settings, List<String> postActions) {
    postActions.stream().forEach(actionSql -> action(settings, actionSql));
  }

  private void action(Database settings, String actionSql) {
    try (

        Connection connection = getConnection(settings);
        Statement statement = connection.createStatement();

    ) {
      log.info(actionSql);
      statement.execute(actionSql);
    } catch (SQLException e) {
      log.error(e.getMessage());
    }
  }

  public class BibPartitionTask implements PartitionTask<BibContext> {

    private final MigrationService migrationService;

    private final InstanceMapper instanceMapper;

    private final Map<String, Object> partitionContext;

    private final BibJob job;

    private int hrid;

    public BibPartitionTask(MigrationService migrationService, InstanceMapper instanceMapper, Map<String, Object> partitionContext, BibJob job) {
      this.migrationService = migrationService;
      this.instanceMapper = instanceMapper;
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

    public BibPartitionTask execute(BibContext context) {

      String schema = this.getSchema();

      int index = this.getIndex();

      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);
      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      InitJobExecutionsRqDto jobExecutionRqDto = new InitJobExecutionsRqDto();
      jobExecutionRqDto.setSourceType(SourceType.ONLINE);
      jobExecutionRqDto.setJobProfileInfo(job.getProfileInfo());
      jobExecutionRqDto.setUserId(job.getUserId());

      InitJobExecutionsRsDto JobExecutionRsDto = migrationService.okapiService.createJobExecution(tenant, token, jobExecutionRqDto);

      JobExecution jobExecution = JobExecutionRsDto.getJobExecutions().get(0);

      String jobExecutionId = jobExecution.getId();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      MarcFactory factory = MarcFactory.newInstance();

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

      String sourceRecordRLTypeId = job.getReferences().get(SOURCE_RECORD_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try {

        PGCopyOutputStream rawRecordOutput = new PGCopyOutputStream(threadConnections.getRawRecordConnection(), RAW_RECORDS_COPY_SQL);
        PrintWriter rawRecordWriter = new PrintWriter(rawRecordOutput, true);

        PGCopyOutputStream parsedRecordOutput = new PGCopyOutputStream(threadConnections.getParsedRecordConnection(), PARSED_RECORDS_COPY_SQL);
        PrintWriter parsedRecordWriter = new PrintWriter(parsedRecordOutput, true);

        PGCopyOutputStream recordOutput = new PGCopyOutputStream(threadConnections.getRecordConnection(), RECORDS_COPY_SQL);
        PrintWriter recordWriter = new PrintWriter(recordOutput, true);

        PGCopyOutputStream instanceOutput = new PGCopyOutputStream(threadConnections.getInstanceConnection(), INSTANCE_COPY_SQL);
        PrintWriter instanceWriter = new PrintWriter(instanceOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement marcStatement = threadConnections.getMarcConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {

          String bibId = pageResultSet.getString(BIB_ID);
          Boolean suppressInOpac = pageResultSet.getBoolean(SUPPRESS_IN_OPAC);

          marcContext.put(BIB_ID, bibId);

          try {
            String marc = getMarc(marcStatement, marcContext);

            BibRecord bibRecord = new BibRecord(bibId, suppressInOpac);

            String sourceRecordId, instanceId;
            if (job.isUseReferenceLinks()) {
              Optional<ReferenceLink> sourceRecordRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(sourceRecordRLTypeId, bibId);
              sourceRecordId = sourceRecordRL.isPresent() ? sourceRecordRL.get().getFolioReference() : UUID.randomUUID().toString();
              Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, bibId);
              instanceId = instanceRL.isPresent() ? instanceRL.get().getFolioReference() : UUID.randomUUID().toString();
            } else {
              sourceRecordId = UUID.randomUUID().toString();
              instanceId = UUID.randomUUID().toString();
            }

            Optional<Record> potentialRecord = rawMarcToRecord(marc);

            if (potentialRecord.isPresent()) {
              Record record = potentialRecord.get();

              DataField dataField = getDataField(factory, record);

              bibRecord.setSourceRecordId(sourceRecordId);
              dataField.addSubfield(factory.newSubfield(S, sourceRecordId));

              bibRecord.setInstanceId(instanceId);
              dataField.addSubfield(factory.newSubfield(I, instanceId));

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

              bibRecord.setMarc(marc);
              bibRecord.setRecord(record);
              bibRecord.setMarcJson(marcJsonNode);

              Date currentDate = new Date();
              bibRecord.setCreatedByUserId(job.getUserId());
              bibRecord.setCreatedDate(currentDate);

              RawRecord rawRecord = bibRecord.toRawRecord();
              ParsedRecord parsedRecord = bibRecord.toParsedRecord();
              RecordModel recordModel = bibRecord.toRecordModel(jobExecutionId);

              String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
              String createdByUserId = job.getUserId();

              Instance instance = bibRecord.toInstance(instanceMapper, hridPrefix, hrid);

              String rrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(rawRecord)));
              String prUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(parsedRecord)));
              String rmUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(recordModel)));
              String iUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(instance)));

              // TODO: validate rows

              rawRecordWriter.println(String.join("\t", rawRecord.getId(), rrUtf8Json, createdAt, createdByUserId));
              parsedRecordWriter.println(String.join("\t", parsedRecord.getId(), prUtf8Json, createdAt, createdByUserId));
              recordWriter.println(String.join("\t", recordModel.getId(), rmUtf8Json, createdAt, createdByUserId));
              instanceWriter.println(String.join("\t", instance.getId(), iUtf8Json, createdAt, createdByUserId, instance.getInstanceTypeId()));

              hrid++;
              count++;

            } else {
              log.error("{} bib id {} no record found", schema, bibId);
            }

          } catch (IOException e) {
            log.error("{} bib id {} error processing marc", schema, bibId);
            log.debug(e.getMessage());
          } catch (MarcException e) {
            log.error("{} bib id {} error reading marc", schema, bibId);
            log.debug(e.getMessage());
          }
        }

        rawRecordWriter.close();
        parsedRecordWriter.close();
        recordWriter.close();
        instanceWriter.close();

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

      migrationService.okapiService.finishJobExecution(tenant, token, jobExecutionId, rawRecordsDto);

      log.info("{} {} finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && ((BibPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setMarcConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setRawRecordConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
      threadConnections.setParsedRecordConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
      threadConnections.setRecordConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
      threadConnections.setInstanceConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
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
      List<InputStream> asciiStreams = marcSequence.stream().sorted((sm1, sm2) -> sm1.getSeqnum().compareTo(sm2.getSeqnum())).map(sm -> sm.getRecordSegment())
          .collect(Collectors.toList());
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

    private BaseConnection rawRecordConnection;
    private BaseConnection parsedRecordConnection;
    private BaseConnection recordConnection;
    private BaseConnection instanceConnection;

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

    public BaseConnection getRawRecordConnection() {
      return rawRecordConnection;
    }

    public void setRawRecordConnection(BaseConnection rawRecordConnection) {
      this.rawRecordConnection = rawRecordConnection;
    }

    public BaseConnection getParsedRecordConnection() {
      return parsedRecordConnection;
    }

    public void setParsedRecordConnection(BaseConnection parsedRecordConnection) {
      this.parsedRecordConnection = parsedRecordConnection;
    }

    public BaseConnection getRecordConnection() {
      return recordConnection;
    }

    public void setRecordConnection(BaseConnection recordConnection) {
      this.recordConnection = recordConnection;
    }

    public BaseConnection getInstanceConnection() {
      return instanceConnection;
    }

    public void setInstanceConnection(BaseConnection instanceConnection) {
      this.instanceConnection = instanceConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        marcConnection.close();
        rawRecordConnection.close();
        parsedRecordConnection.close();
        recordConnection.close();
        instanceConnection.close();
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
