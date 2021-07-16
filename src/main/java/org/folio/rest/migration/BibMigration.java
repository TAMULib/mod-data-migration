package org.folio.rest.migration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import org.apache.commons.lang3.StringUtils;
import org.folio.Instance;
import org.folio.Statisticalcodes;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.common.Status;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.ParsedRecord;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.RawRecord;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_source_record_storage.RecordModel;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_source_record_storage.Snapshot;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.mapping.InstanceMapper;
import org.folio.rest.migration.model.BibRecord;
import org.folio.rest.migration.model.request.bib.BibContext;
import org.folio.rest.migration.model.request.bib.BibJob;
import org.folio.rest.migration.model.request.bib.BibMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.marc4j.MarcException;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import io.vertx.core.json.JsonObject;

public class BibMigration extends AbstractMigration<BibContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String STATISTICAL_CODES = "STATISTICAL_CODES";

  private static final String USER_ID = "USER_ID";

  private static final String BIB_ID = "BIB_ID";
  private static final String SUPPRESS_IN_OPAC = "SUPPRESS_IN_OPAC";
  private static final String OPERATOR_ID = "OPERATOR_ID";

  private static final String SOURCE_RECORD_REFERENCE_ID = "sourceRecordTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";

  private static final String T_999 = "999";
  private static final String C_001 = "001";
  private static final String C_003 = "003";

  private static final char F = 'f';
  private static final char I = 'i';
  private static final char S = 's';

  // (id,content)
  private static String RAW_RECORDS_COPY_SQL = "COPY %s_mod_source_record_storage.raw_records_lb (id,content) FROM STDIN WITH NULL AS 'null'";

  // (id,content)
  private static String PARSED_RECORDS_COPY_SQL = "COPY %s_mod_source_record_storage.marc_records_lb (id,content) FROM STDIN WITH NULL AS 'null'";

  // (id,snapshot_id,matched_id,generation,record_type,instance_id,state,leader_record_status,\"order\",suppress_discovery,created_by_user_id,created_date,updated_by_user_id,updated_date)
  private static String RECORDS_COPY_SQL = "COPY %s_mod_source_record_storage.records_lb (id,snapshot_id,matched_id,generation,record_type,instance_id,state,leader_record_status,\"order\",suppress_discovery,created_by_user_id,created_date,updated_by_user_id,updated_date) FROM STDIN WITH NULL AS 'null'";

  // (id,jsonb,creation_date,created_by,instancestatusid,modeofissuanceid,instancetypeid)
  private static String INSTANCE_COPY_SQL = "COPY %s_mod_inventory_storage.instance (id,jsonb,creation_date,created_by,instancestatusid,modeofissuanceid,instancetypeid) FROM STDIN WITH NULL AS 'null'";

  private BibMigration(BibContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);
    MappingParameters mappingParameters = migrationService.okapiService.getMappingParamaters(tenant, token);
    JsonObject mappingRules = migrationService.okapiService.fetchRules(tenant, token);
    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);
    Statisticalcodes statisticalcodes = migrationService.okapiService.fetchStatisticalCodes(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    InstanceMapper instanceMapper = new InstanceMapper(mappingParameters, mappingRules);

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<BibContext>(context, new TaskCallback() {

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
    String hridPrefix = instancesHridSettings.getString(PREFIX);
    int hridStartNumber = instancesHridSettings.getInteger(START_NUMBER);

    int index = 0;

    for (BibJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

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
        partitionContext.put(STATISTICAL_CODES, statisticalcodes);
        partitionContext.put(USER_ID, user.getId());
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new BibPartitionTask(migrationService, instanceMapper, partitionContext));
        offset += limit;
        hridStartNumber += limit;
        index++;
      }
    }

    instancesHridSettings.put(START_NUMBER, hridStartNumber);

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static BibMigration with(BibContext context, String tenant) {
    return new BibMigration(context, tenant);
  }

  public class BibPartitionTask implements PartitionTask<BibContext> {

    private final MigrationService migrationService;

    private final InstanceMapper instanceMapper;

    private final Map<String, Object> partitionContext;

    private int hrid;

    public BibPartitionTask(MigrationService migrationService, InstanceMapper instanceMapper, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.instanceMapper = instanceMapper;
      this.partitionContext = partitionContext;
      this.hrid = (int) partitionContext.get(HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public BibPartitionTask execute(BibContext context) {
      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);
      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      BibJob job = (BibJob) partitionContext.get(JOB);

      Statisticalcodes statisticalcodes = (Statisticalcodes) partitionContext.get(STATISTICAL_CODES);

      String userId = (String) partitionContext.get(USER_ID);

      String schema = job.getSchema();

      int index = this.getIndex();

      Snapshot snapshot = new Snapshot();

      snapshot.setJobExecutionId(UUID.randomUUID().toString());
      snapshot.setStatus(Status.COMMITTED);
      try {
        snapshot = migrationService.okapiService.createSnapshot(snapshot, tenant, token);
      } catch (Exception e) {
        log.error("failed to create snapshot: {}", e.getMessage());
        return this;
      }

      String jobExecutionId = snapshot.getJobExecutionId();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      BibMaps bibMaps = context.getMaps();

      MarcFactory factory = MarcFactory.newInstance();

      Map<String, Object> marcContext = new HashMap<>();
      marcContext.put(SQL, context.getExtraction().getMarcSql());
      marcContext.put(SCHEMA, schema);

      String sourceRecordRLTypeId = job.getReferences().get(SOURCE_RECORD_REFERENCE_ID);
      String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try (
        PrintWriter rawRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getRawRecordConnection(), String.format(RAW_RECORDS_COPY_SQL, tenant)), true);
        PrintWriter parsedRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getParsedRecordConnection(), String.format(PARSED_RECORDS_COPY_SQL, tenant)), true);
        PrintWriter recordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getRecordConnection(), String.format(RECORDS_COPY_SQL, tenant)), true);
        PrintWriter instanceWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getInstanceConnection(), String.format(INSTANCE_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement marcStatement = threadConnections.getMarcConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String bibId = pageResultSet.getString(BIB_ID);
          String suppressInOpac = pageResultSet.getString(SUPPRESS_IN_OPAC);
          String operatorId = pageResultSet.getString(OPERATOR_ID);

          if (exclude(job.getExclusions(), pageResultSet)) {
            continue;
          }

          Boolean suppressDiscovery = suppressInOpac.equals("Y");

          marcContext.put(BIB_ID, bibId);

          try {
            String marc = getMarc(marcStatement, marcContext);

            Optional<Record> potentialRecord = rawMarcToRecord(marc);

            if (!potentialRecord.isPresent()) {
              log.error("schema {}, bib id {}, marc {} unable to read record", schema, bibId, marc);
              continue;
            }

            List<String> matchedCodes = new ArrayList<>();
            if (StringUtils.isNotEmpty(operatorId)) {
              if (bibMaps.getStatisticalCode().containsKey(operatorId)) {
                operatorId = bibMaps.getStatisticalCode().get(operatorId);
              }
              matchedCodes = getMatchingStatisticalCodes(operatorId, statisticalcodes);
            }

            BibRecord bibRecord = new BibRecord(bibId, job.getInstanceStatusId(), suppressDiscovery, matchedCodes);

            Optional<ReferenceLink> sourceRecordRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(sourceRecordRLTypeId, bibId);
            if (!sourceRecordRL.isPresent()) {
              log.error("{} no source record id found for bib id {}", schema, bibId);
              continue;
            }

            Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, bibId);
            if (!instanceRL.isPresent()) {
              log.error("{} no instance id found for bib id {}", schema, bibId);
              continue;
            }

            String sourceRecordId = sourceRecordRL.get().getFolioReference();

            String instanceId = instanceRL.get().getFolioReference();

            Record record = potentialRecord.get();

            String originalMarcJson = recordToJson(record);
            JsonObject originalMarcJsonObject = new JsonObject(originalMarcJson);

            bibRecord.setOriginalParsedRecord(originalMarcJsonObject);

            String hridString = String.format(HRID_TEMPLATE, hridPrefix, hrid);

            DataField field999 = getDataField(factory, record);

            bibRecord.setSourceRecordId(sourceRecordId);
            field999.addSubfield(factory.newSubfield(S, sourceRecordId));

            bibRecord.setInstanceId(instanceId);
            field999.addSubfield(factory.newSubfield(I, instanceId));

            record.addVariableField(field999);

            ControlField field001 = factory.newControlField(C_001, hridString);
            record.addVariableField(field001);

            ControlField field003 = factory.newControlField(C_003, job.getControlNumberIdentifier());
            record.addVariableField(field003);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
              MarcWriter streamWriter = new MarcStreamWriter(os, DEFAULT_CHARSET.name());
              // use stream writer to recalculate leader
              streamWriter.write(record);
              streamWriter.close();
            }

            String marcJson = recordToJson(record);

            JsonObject marcJsonObject = new JsonObject(marcJson);

            String leaderRecordStatus = null;
            String leader = marcJsonObject.getString("leader");
            if (Objects.nonNull(leader) && leader.length() > 5) {
              leaderRecordStatus = String.valueOf(leader.charAt(5));
            }

            bibRecord.setMarc(marc);
            bibRecord.setParsedRecord(marcJsonObject);

            Date createdDate = new Date();
            bibRecord.setCreatedByUserId(userId);
            bibRecord.setCreatedDate(createdDate);

            RawRecord rawRecord = bibRecord.toRawRecord();
            ParsedRecord parsedRecord = bibRecord.toParsedRecord();
            RecordModel recordModel = bibRecord.toRecordModel(jobExecutionId, count);

            Instance instance = bibRecord.toInstance(instanceMapper, hridString);

            if (Objects.isNull(instance)) {
              log.error("schema {}, bib id {} unable to map record to instance", schema, bibId);
              continue;
            }

            String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
            String createdByUserId = userId;

            String rrcUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(rawRecord.getContent())));
            String prcUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(parsedRecord.getContent())));
            String iUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(instance)));

            // (id,snapshot_id,matched_id,generation,record_type,instance_id,state,leader_record_status,\"order\",suppress_discovery,created_by_user_id,created_date,updated_by_user_id,updated_date)
            recordWriter.println(String.join("\t",
              recordModel.getId(),
              recordModel.getSnapshotId(),
              recordModel.getMatchedId(),
              recordModel.getGeneration().toString(),
              recordModel.getRecordType().toString(),
              recordModel.getExternalIdsHolder().getInstanceId(),
              recordModel.getState().toString(),
              leaderRecordStatus,
              recordModel.getOrder().toString(),
              recordModel.getAdditionalInfo().getSuppressDiscovery().toString(),
              createdByUserId,
              createdAt,
              createdByUserId,
              createdAt
            ));

            // (id,content)
            rawRecordWriter.println(String.join("\t", rawRecord.getId(), rrcUtf8Json));

            // (id,content)
            parsedRecordWriter.println(String.join("\t", parsedRecord.getId(), prcUtf8Json));

            instanceWriter.println(String.join("\t", instance.getId(), iUtf8Json, createdAt, createdByUserId, instance.getStatusId(), instance.getModeOfIssuanceId(), instance.getInstanceTypeId()));

            hrid++;
            count++;

          } catch (IOException e) {
            log.error("{} bib id {} error processing marc", schema, bibId);
            log.debug(e.getMessage());
          } catch (MarcException e) {
            log.error("{} bib id {} error reading marc", schema, bibId);
            log.debug(e.getMessage());
          }
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((BibPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private List<String> getMatchingStatisticalCodes(String operatorId, Statisticalcodes statisticalcodes) {
    return statisticalcodes.getStatisticalCodes().stream()
      .filter(sc -> sc.getCode().equals(operatorId))
      .map(sc -> sc.getId())
      .collect(Collectors.toList());
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

}
