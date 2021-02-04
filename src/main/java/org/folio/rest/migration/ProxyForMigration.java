package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.folio.rest.jaxrs.model.users.Metadata;
import org.folio.rest.jaxrs.model.users.Proxyfor;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.proxyfor.ProxyForContext;
import org.folio.rest.migration.model.request.proxyfor.ProxyForJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class ProxyForMigration extends AbstractMigration<ProxyForContext> {

  private static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd";

  private static final String USER_ID = "USER_ID";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PROXY_PATRON_ID = "PROXY_PATRON_ID";
  private static final String EXPIRATION_DATE = "EXPIRATION_DATE";

  private static final String USER_REFERENCE_ID = "userTypeId";

  // (id,jsonb,creation_date,created_by)
  private static String PROXY_FOR_COPY_SQL = "COPY %s_mod_users.proxyfor (id,jsonb,creation_date,created_by) FROM STDIN WITH NULL AS 'null'";

  private ProxyForMigration(ProxyForContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<ProxyForContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (ProxyForJob job : context.getJobs()) {

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
        partitionContext.put(JOB, job);
        partitionContext.put(USER_ID, user.getId());
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new ProxyForPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static ProxyForMigration with(ProxyForContext context, String tenant) {
    return new ProxyForMigration(context, tenant);
  }

  public class ProxyForPartitionTask implements PartitionTask<ProxyForContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public ProxyForPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public ProxyForPartitionTask execute(ProxyForContext context) {
      long startTime = System.nanoTime();

      ProxyForJob job = (ProxyForJob) partitionContext.get(JOB);

      String userId = (String) partitionContext.get(USER_ID);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      String userRLTypeId = job.getReferences().get(USER_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      try (
        PrintWriter proxyForWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getProxyForConnection(), String.format(PROXY_FOR_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {
          String patronId = pageResultSet.getString(PATRON_ID);
          String proxyPatronId = pageResultSet.getString(PROXY_PATRON_ID);
          String expirationDate = pageResultSet.getString(EXPIRATION_DATE);

          Optional<ReferenceLink> userRL = migrationService.referenceLinkRepo
              .findByTypeIdAndExternalReference(userRLTypeId, patronId);

          if (!userRL.isPresent()) {
            log.error("{} no user id found for patron id {}", schema, patronId);
            continue;
          }

          Optional<ReferenceLink> proxyUserRL = migrationService.referenceLinkRepo
              .findByTypeIdAndExternalReference(userRLTypeId, proxyPatronId);

          if (!proxyUserRL.isPresent()) {
            log.error("{} no proxy user id found for proxy patron id {}", schema, proxyPatronId);
            continue;
          }

          Proxyfor proxyfor = new Proxyfor();
          proxyfor.setId(UUID.randomUUID().toString());
          proxyfor.setUserId(userRL.get().getFolioReference());
          proxyfor.setProxyUserId(proxyUserRL.get().getFolioReference());

          if (StringUtils.isNotEmpty(expirationDate)) {
            try {
              proxyfor.setExpirationDate(DateUtils.parseDate(expirationDate, EXPIRATION_DATE_FORMAT));
            } catch (ParseException e) {
              log.error("{} proxy for patron {} failed to parse date {}", schema, patronId, expirationDate);
              e.printStackTrace();
            }
          }

          Date createdDate = new Date();

          String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
          String createdByUserId = userId;

          Metadata metadata = new Metadata();
          metadata.setCreatedByUserId(createdByUserId);
          metadata.setCreatedDate(createdDate);
          metadata.setUpdatedByUserId(createdByUserId);
          metadata.setUpdatedDate(createdDate);

          proxyfor.setMetadata(metadata);

          try {

            String pfUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(proxyfor)));

            // (id,jsonb,creation_date,created_by)
            proxyForWriter.println(String.join("\t", proxyfor.getId(), pfUtf8Json, createdAt, createdByUserId));

          } catch (JsonProcessingException e) {
            log.error("{} proxy for patron {} error serializing proxyfor", schema, patronId);
            log.debug(e.getMessage());
          }

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
      return Objects.nonNull(obj) && ((ProxyForPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setProxyForConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private BaseConnection proxyForConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public BaseConnection getProxyForConnection() {
      return proxyForConnection;
    }

    public void setProxyForConnection(BaseConnection proxyForConnection) {
      this.proxyForConnection = proxyForConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        proxyForConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
