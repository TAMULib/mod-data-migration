package org.folio.rest.migration;

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
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.UserRecord;
import org.folio.rest.migration.model.request.HoldingJob;
import org.folio.rest.migration.model.request.UserContext;
import org.folio.rest.migration.model.request.UserJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class UserMigration extends AbstractMigration<UserContext> {

  private static final String PATRON_ID = "PATRON_ID";
  private static final String EXTERNAL_SYSTEM_ID = "EXTERNAL_SYSTEM_ID";
  private static final String LAST_NAME = "LAST_NAME";
  private static final String FIRST_NAME = "FIRST_NAME";
  private static final String MIDDLE_NAME = "MIDDLE_NAME";
  private static final String ACTIVE_DATE = "ACTIVE_DATE";
  private static final String EXPIRE_DATE = "EXPIRE_DATE";
  private static final String PURGE_DATE = "PURGE_DATE";
  private static final String SMS_NUMBER = "SMS_NUMBER";

  private static final String USER_REFERENCE_ID = "userTypeId";
  private static final String USER_EXTERNAL_REFERENCE_ID = "userExternalTypeId";

  // (id,jsonb,creation_date,created_by,patrongroup)
  private static final String USERS_COPY_SQL = "COPY %s_mod_users.users (id,jsonb,creation_date,created_by,patrongroup) FROM STDIN";

  private UserMigration(UserContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<UserContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (UserJob job : context.getJobs()) {

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
        partitionContext.put(JOB, job);
        taskQueue.submit(new UserPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(true);
  }

  public static UserMigration with(UserContext context, String tenant) {
    return new UserMigration(context, tenant);
  }

  public class UserPartitionTask implements PartitionTask<UserContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public UserPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public UserPartitionTask execute(UserContext context) {
      long startTime = System.nanoTime();

      UserJob job = (UserJob) partitionContext.get(JOB);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      log.info("starting {} {}", schema, index);

      try {
        PGCopyOutputStream userRecordOutput = new PGCopyOutputStream(threadConnections.getUserConnection(), String.format(USERS_COPY_SQL, tenant));
        PrintWriter userRecordWriter = new PrintWriter(userRecordOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {

          // int patronId = pageResultSet.getInt(PATRON_ID);
          // String externalSystemId = pageResultSet.getString(EXTERNAL_SYSTEM_ID);
          // String lastName = pageResultSet.getString(LAST_NAME);
          // String firstName = pageResultSet.getString(FIRST_NAME);
          // String middleName = pageResultSet.getString(MIDDLE_NAME);
          // Date activeDate = pageResultSet.getDate(ACTIVE_DATE);
          // Date expireDate = pageResultSet.getDate(EXPIRE_DATE);
          // Date purgeDate = pageResultSet.getDate(PURGE_DATE);
          // String smsNumber = pageResultSet.getString(SMS_NUMBER);

          // System.out.println(String.join(",", String.valueOf(patronId), externalSystemId, lastName, firstName, middleName, activeDate.toString(), expireDate.toString(), purgeDate.toString(), smsNumber));

          String userId = null;
          String patronGroup = null;

          UserRecord userRecord = new UserRecord(userId);

          String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
          String createdByUserId = job.getUserId();

          // try {
          //   String userUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(userRecord.toUser())));

          //   // TODO: validate rows
          //   userRecordWriter.println(String.join("\t", userId, userUtf8Json, createdAt, createdByUserId, patronGroup));
          // } catch (JsonProcessingException e) {
          //   log.error("{} user id {} error serializing user", schema, userId);
          // }
        }

        userRecordWriter.close();

        pageStatement.close();

        pageResultSet.close();

      } catch (SQLException e) {
        e.printStackTrace();
      }

      threadConnections.closeAll();

      log.info("{} {} user finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((UserPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setUserConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {
    private Connection pageConnection;

    private BaseConnection userConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public BaseConnection getUserConnection() {
      return userConnection;
    }

    public void setUserConnection(BaseConnection userConnection) {
      this.userConnection = userConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        userConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
