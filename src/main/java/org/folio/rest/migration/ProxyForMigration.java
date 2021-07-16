package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

public class ProxyForMigration extends AbstractMigration<ProxyForContext> {

  private static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd";

  private static final String USER_ID = "USER_ID";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PROXY_PATRON_ID = "PROXY_PATRON_ID";
  private static final String EXPIRATION_DATE = "EXPIRATION_DATE";

  private static final String USER_REFERENCE_ID = "userTypeId";

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
        partitionContext.put(TOKEN, token);
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

      String token = (String) partitionContext.get(TOKEN);

      String userId = (String) partitionContext.get(USER_ID);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      String userRLTypeId = job.getReferences().get(USER_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {
          final String patronId = pageResultSet.getString(PATRON_ID);
          final String proxyPatronId = pageResultSet.getString(PROXY_PATRON_ID);
          final String expirationDate = pageResultSet.getString(EXPIRATION_DATE);

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

          String createdByUserId = userId;

          Metadata metadata = new Metadata();
          metadata.setCreatedByUserId(createdByUserId);
          metadata.setCreatedDate(createdDate);
          metadata.setUpdatedByUserId(createdByUserId);
          metadata.setUpdatedDate(createdDate);

          proxyfor.setMetadata(metadata);

          try {
            migrationService.okapiService.createProxyFor(proxyfor, tenant, token);
          } catch (Exception e) {
            log.error("{} error creating proxy for {}\n{}", schema, proxyfor, e.getMessage());
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
