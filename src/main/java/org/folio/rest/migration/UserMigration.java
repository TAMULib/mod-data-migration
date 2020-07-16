package org.folio.rest.migration;

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
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.Userdata;
import org.folio.rest.jaxrs.model.Usergroup;
import org.folio.rest.jaxrs.model.Usergroups;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.UserAddressRecord;
import org.folio.rest.migration.model.UserRecord;
import org.folio.rest.migration.model.request.user.UserContext;
import org.folio.rest.migration.model.request.user.UserDefaults;
import org.folio.rest.migration.model.request.user.UserJob;
import org.folio.rest.migration.model.request.user.UserMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;
import org.springframework.cache.annotation.Cacheable;

public class UserMigration extends AbstractMigration<UserContext> {

  private static final String USER_GROUPS = "USER_GROUPS";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String EXTERNAL_SYSTEM_ID = "EXTERNAL_SYSTEM_ID";
  private static final String LAST_NAME = "LAST_NAME";
  private static final String FIRST_NAME = "FIRST_NAME";
  private static final String MIDDLE_NAME = "MIDDLE_NAME";
  private static final String ACTIVE_DATE = "ACTIVE_DATE";
  private static final String EXPIRE_DATE = "EXPIRE_DATE";
  private static final String SMS_NUMBER = "SMS_NUMBER";
  private static final String CURRENT_CHARGES = "CURRENT_CHARGES";
  private static final String PATRON_BARCODE = "PATRON_BARCODE";
  private static final String PATRON_GROUP_CODE = "PATRON_GROUP_CODE";

  private static final String ADDRESS_DESC = "ADDRESS_DESC";
  private static final String ADDRESS_STATUS = "ADDRESS_STATUS";
  private static final String ADDRESS_TYPE = "ADDRESS_TYPE";
  private static final String ADDRESS_LINE1 = "ADDRESS_LINE1";
  private static final String ADDRESS_LINE2 = "ADDRESS_LINE2";
  private static final String CITY = "CITY";
  private static final String COUNTRY = "COUNTRY";
  private static final String PHONE_NUMBER = "PHONE_NUMBER";
  private static final String PHONE_DESC = "PHONE_DESC";
  private static final String STATE_PROVINCE = "STATE_PROVINCE";
  private static final String ZIP_POSTAL = "ZIP_POSTAL";

  private static final String USERNAME_NETID = "TAMU_NETID";

  private static final String DECODE = "DECODE";

  private static final String USER_REFERENCE_ID = "userTypeId";

  private static final String IGNORE_BARCODES = "barcodes";
  private static final String IGNORE_USERNAMES = "usernames";

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

    Usergroups usergroups = migrationService.okapiService.fetchUsergroups(tenant, token);

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
        partitionContext.put(USER_GROUPS, usergroups);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
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

      Usergroups usergroups = (Usergroups) partitionContext.get(USER_GROUPS);

      UserMaps maps = context.getMaps();
      UserDefaults defaults = context.getDefaults();

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database usernameSettings = context.getExtraction().getUsernameDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      Map<String, Object> usernameContext = new HashMap<>();
      usernameContext.put(SQL, context.getExtraction().getUsernameSql());
      usernameContext.put(SCHEMA, job.getSchema());

      Map<String, Object> addressContext = new HashMap<>();
      addressContext.put(SQL, context.getExtraction().getAddressSql());
      addressContext.put(SCHEMA, job.getSchema());

      Map<String, Object> patronGroupContext = new HashMap<>();
      patronGroupContext.put(SQL, context.getExtraction().getPatronGroupSql());
      patronGroupContext.put(SCHEMA, job.getSchema());
      patronGroupContext.put(DECODE, job.getDecodeSql());

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      String userIdRLTypeId = job.getReferences().get(USER_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, usernameSettings, folioSettings);

      try (
        PrintWriter userWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getUserConnection(), String.format(USERS_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement usernameStatement = threadConnections.getUsernameConnection().createStatement();
        Statement addressStatement = threadConnections.getAdressConnection().createStatement();
        Statement patronGroupStatement = threadConnections.getPatronGroupConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String patronId = pageResultSet.getString(PATRON_ID);
          String externalSystemId = pageResultSet.getString(EXTERNAL_SYSTEM_ID);
          String lastName = pageResultSet.getString(LAST_NAME);
          String firstName = pageResultSet.getString(FIRST_NAME);
          String middleName = pageResultSet.getString(MIDDLE_NAME);
          String activeDate = pageResultSet.getString(ACTIVE_DATE);
          String expireDate = pageResultSet.getString(EXPIRE_DATE);
          String smsNumber = pageResultSet.getString(SMS_NUMBER);
          String currentCharges = pageResultSet.getString(CURRENT_CHARGES);

          Optional<ReferenceLink> userRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(userIdRLTypeId, patronId);
          if (!userRL.isPresent()) {
            log.error("{} no user id found for patron id {}", schema, patronId);
            continue;
          }

          if (job.getSkipDuplicates() && migrationService.referenceLinkRepo.countByExternalReference(externalSystemId) > 1) {
            continue;
          }

          String referenceId = userRL.get().getFolioReference().toString();

          UserRecord userRecord = new UserRecord(referenceId, patronId, externalSystemId, lastName, firstName, middleName, activeDate, expireDate, smsNumber, currentCharges);

          usernameContext.put(PATRON_ID, patronId);
          usernameContext.put(EXTERNAL_SYSTEM_ID, externalSystemId);
          addressContext.put(PATRON_ID, patronId);
          patronGroupContext.put(PATRON_ID, patronId);

          String username = getUsername(usernameStatement, usernameContext);

          if (maps.getIgnore().get(IGNORE_USERNAMES).contains(username.toLowerCase())) {
            log.warn("{} ignoring patron id {} username {}", schema, patronId, username);
            continue;
          }

          userRecord.setUsername(username);

          List<UserAddressRecord> userAddressRecords = getUserAddressRecords(addressStatement, addressContext);

          userRecord.setUserAddressRecords(userAddressRecords);

          PatronCodes patronCodes = getPatronCodes(patronGroupStatement, patronGroupContext);

          if (Objects.nonNull(patronCodes.getBarcode()) && maps.getIgnore().get(IGNORE_BARCODES).contains(patronCodes.getBarcode().toLowerCase())) {
            log.warn("{} ignoring patron id {} barcode {}", schema, patronId, patronCodes.getBarcode());
            continue;
          }

          Map<String, String> patronGroupMap = maps.getPatronGroup();

          if (patronGroupMap.containsKey(patronCodes.getGroupcode().toLowerCase())) {
            userRecord.setGroupcode(patronGroupMap.get(patronCodes.getGroupcode().toLowerCase()));
          } else {
            userRecord.setGroupcode(patronCodes.getGroupcode());
          }
          userRecord.setBarcode(patronCodes.getBarcode());

          Optional<String> patronGroup = getPatronGroup(userRecord.getGroupcode(), usergroups);

          if (!patronGroup.isPresent()) {
            log.error("{} no patron group found for patron id {} and group code {}", schema, patronId, userRecord.getGroupcode());
            continue;
          }

          Userdata userdata = userRecord.toUserdata(patronGroup.get(), defaults);

          Date createdDate = new Date();

          String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
          String createdByUserId = job.getUserId();

          try {
            String userUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(userdata)));

            userWriter.println(String.join("\t", userdata.getId(), userUtf8Json, createdAt, createdByUserId, userdata.getPatronGroup()));
          } catch (JsonProcessingException e) {
            log.error("{} user id {} error serializing user", schema, userRecord.getPatronId());
          }
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} user finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((UserPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database usernameSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setAddressesConnection(getConnection(voyagerSettings));
    threadConnections.setPatronGroupConnection(getConnection(voyagerSettings));
    threadConnections.setUsernameConnection(getConnection(usernameSettings));

    try {
      threadConnections.setUserConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }

    return threadConnections;
  }

  private String getUsername(Statement statement, Map<String, Object> usernameContext) throws SQLException {
    String schema = (String) usernameContext.get(SCHEMA);
    String patronId = (String) usernameContext.get(PATRON_ID);
    String username = String.format("%s_%s", schema, patronId);
    try (ResultSet resultSet = getResultSet(statement, usernameContext)) {
      while (resultSet.next()) {
        String netid = resultSet.getString(USERNAME_NETID);
        if (StringUtils.isNotEmpty(netid)) {
          username = netid.toLowerCase();
        }
      }
    }
    return username;
  }

  private List<UserAddressRecord> getUserAddressRecords(Statement statement, Map<String, Object> addressContext) throws SQLException {
    List<UserAddressRecord> userAddressRecords = new ArrayList<>();
    try (ResultSet resultSet = getResultSet(statement, addressContext)) {
      while (resultSet.next()) {
        String addressDescription = resultSet.getString(ADDRESS_DESC);
        String addressStatus = resultSet.getString(ADDRESS_STATUS);
        String addressType = resultSet.getString(ADDRESS_TYPE);
        String addressLine1 = resultSet.getString(ADDRESS_LINE1);
        String addressLine2 = resultSet.getString(ADDRESS_LINE2);
        String city = resultSet.getString(CITY);
        String country = resultSet.getString(COUNTRY);
        String phoneNumber = resultSet.getString(PHONE_NUMBER);
        String phoneDescription = resultSet.getString(PHONE_DESC);
        String stateProvince = resultSet.getString(STATE_PROVINCE);
        String zipPostal = resultSet.getString(ZIP_POSTAL);
        userAddressRecords.add(new UserAddressRecord(addressDescription, addressStatus, addressType, addressLine1, addressLine2, city, country, phoneNumber, phoneDescription, stateProvince, zipPostal));
      }
    }
    return userAddressRecords;
  }

  private PatronCodes getPatronCodes(Statement statement, Map<String, Object> patronGroupContext) throws SQLException {
    PatronCodes patronCodes = null;
    String groupcode = null, barcode = null;
    try (ResultSet resultSet = getResultSet(statement, patronGroupContext)) {
      while(resultSet.next()) {
        String patronGroupcode = resultSet.getString(PATRON_GROUP_CODE);
        String patronBarcode = resultSet.getString(PATRON_BARCODE);
        if (Objects.nonNull(patronGroupcode)) {
          groupcode = patronGroupcode.toLowerCase();
        }
        if (Objects.nonNull(patronBarcode)) {
          barcode = patronBarcode.toLowerCase();
        }
        patronCodes = new PatronCodes(groupcode, barcode);
        break;
      }
    }
    return patronCodes;
  }

  @Cacheable(value = "patronGroups", key = "groupcode", sync = true)
  private Optional<String> getPatronGroup(String groupcode, Usergroups usergroups) {
    Optional<Usergroup> usergroup = usergroups.getUsergroups().stream()
      .filter(ug -> ug.getGroup().equals(groupcode))
      .findAny();
    if (usergroup.isPresent()) {
      return Optional.of(usergroup.get().getId());
    }
    return Optional.empty();
  }

  private class ThreadConnections {

    private Connection pageConnection;
    private Connection addressConnection;
    private Connection patronGroupConnection;
    private Connection usernameConnection;

    private BaseConnection userConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getAdressConnection() {
      return addressConnection;
    }

    public void setAddressesConnection(Connection addressConnection) {
      this.addressConnection = addressConnection;
    }

    public Connection getPatronGroupConnection() {
      return patronGroupConnection;
    }

    public void setPatronGroupConnection(Connection patronGroupConnection) {
      this.patronGroupConnection = patronGroupConnection;
    }

    public Connection getUsernameConnection() {
      return usernameConnection;
    }

    public void setUsernameConnection(Connection usernameConnection) {
      this.usernameConnection = usernameConnection;
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
        addressConnection.close();
        patronGroupConnection.close();
        usernameConnection.close();
        userConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

  private class PatronCodes {

    private final String groupcode;
    private final String barcode;

    public PatronCodes(String groupcode, String barcode) {
      this.groupcode = groupcode;
      this.barcode = barcode;
    }

    public String getGroupcode() {
      return groupcode;
    }

    public String getBarcode() {
      return barcode;
    }

  }

}
