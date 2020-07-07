package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
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
import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.jaxrs.model.Personal;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.UserRecord;
import org.folio.rest.migration.model.request.UserContext;
import org.folio.rest.migration.model.request.UserJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
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
  private static final String SMS_NUMBER = "SMS_NUMBER";
  private static final String CURRENT_CHARGES = "CURRENT_CHARGES";

  private static final String USER_REFERENCE_ID = "userTypeId";
  private static final String USER_EXTERNAL_REFERENCE_ID = "userExternalTypeId";

  private static final String USERNAME = "TAMU_NETID";
  private static final String PATRON = "patron";
  private static final String TEXT = "text";

  private static final String ADDRESS_LINE_1 = "ADDRESS_LINE1";
  private static final String ADDRESS_LINE_2 = "ADDRESS_LINE2";
  private static final String CITY = "CITY";
  private static final String REGION = "STATE_PROVINCE";
  private static final String POSTAL_CODE = "ZIP_POSTAL";
  private static final String COUNTRY_CODE = "COUNTRY";
  private static final String ADDRESS_STATUS = "ADDRESS_STATUS";
  private static final String ADDRESS_TYPE = "ADDRESS_DESC";
  private static final String PHONE_TYPES = "PHONE_TYPES";
  private static final String PHONE_NUMBER = "PHONE_NUMBER";

  private static final String PRIMARY = "Primary";
  private static final String MOBILE = "Mobile";

  private static final String AMDB = "AMDB";

  private static final String BARCODE = "BARCODE";
  private static final String PATRON_GROUP = "PATRON_GROUP";
  private static final String DECODE = "DECODE";

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

      Database usernameSettings = context.getExtraction().getUsernameDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      String userIdRLTypeId = job.getReferences().get(USER_REFERENCE_ID);
      String userExternalIdRLTypeId = job.getReferences().get(USER_EXTERNAL_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, usernameSettings, folioSettings);

      log.info("starting {} {}", schema, index);

      try {
        PGCopyOutputStream userRecordOutput = new PGCopyOutputStream(threadConnections.getUserConnection(),
            String.format(USERS_COPY_SQL, tenant));
        PrintWriter userRecordWriter = new PrintWriter(userRecordOutput, true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement usernameStatement = threadConnections.getUsernameConnection().createStatement();
        Statement addressesStatement = threadConnections.getAdressesConnection().createStatement();
        Statement patronGroupStatement = threadConnections.getPatronGroupConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

        while (pageResultSet.next()) {

          String patronId = pageResultSet.getString(PATRON_ID);
          String externalSystemId = pageResultSet.getString(EXTERNAL_SYSTEM_ID);
          String lastName = pageResultSet.getString(LAST_NAME);
          String firstName = pageResultSet.getString(FIRST_NAME);
          String middleName = pageResultSet.getString(MIDDLE_NAME);
          String enrollmentDateString = pageResultSet.getString(ACTIVE_DATE);
          String expirationDateString = pageResultSet.getString(EXPIRE_DATE);
          String smsNumber = pageResultSet.getString(SMS_NUMBER);
          int currentCharges = pageResultSet.getInt(CURRENT_CHARGES);

          Map<String, Object> usernameContext = new HashMap<>();
          usernameContext.put(SQL, context.getExtraction().getUsernameSql());
          usernameContext.put(SCHEMA, schema);
          usernameContext.put(EXTERNAL_SYSTEM_ID, externalSystemId);

          Map<String, Object> addressesContext = new HashMap<>();
          addressesContext.put(SQL, context.getExtraction().getAddressesSql());
          addressesContext.put(SCHEMA, schema);
          addressesContext.put(PATRON_ID, patronId);

          Optional<String> potentialUsername = getUsername(usernameStatement, usernameContext);
          if (!potentialUsername.isPresent()) {
            log.error("schema {}, patron id {}, unable to read netid", schema, patronId);
          }

          Optional<ReferenceLink> userRL = migrationService.referenceLinkRepo
              .findByTypeIdAndExternalReference(userIdRLTypeId, patronId);
          if (!userRL.isPresent()) {
            log.error("{} no user id found for patron id {}", schema, patronId);
            continue;
          }

          Optional<ReferenceLink> userExternalRL = migrationService.referenceLinkRepo
              .findByTypeIdAndExternalReference(userExternalIdRLTypeId, externalSystemId);
          if (!userExternalRL.isPresent()) {
            log.error("{} no user external id found for external system id {}", schema, externalSystemId);
            continue;
          }

          String username = potentialUsername.get();
          String id = userRL.get().getFolioReference().toString();
          Date enrollmentDate = new SimpleDateFormat("YYYYMMDD").parse(enrollmentDateString);
          Date expirationDate = new SimpleDateFormat("YYYY-MM-DD").parse(expirationDateString);
          boolean active = currentCharges > 0 ? true : enrollmentDate.compareTo(expirationDate) < 0;

          Personal personal = new Personal();
          personal.setFirstName(firstName);
          personal.setMiddleName(middleName);
          personal.setLastName(lastName);

          populateAddressesAndEmails(addressesStatement, addressesContext, personal);

          if (StringUtils.isNotEmpty(smsNumber)) {
            personal.setPreferredContactTypeId(TEXT);
          }

          Map<String, Object> patronGroupContext = new HashMap<>();
          patronGroupContext.put(SQL, context.getExtraction().getPatronGroupSql());
          patronGroupContext.put(SCHEMA, schema);
          patronGroupContext.put(PATRON_ID, patronId);

          Map<String, String> groupData;

          if (schema.equals(AMDB)) {
            patronGroupContext.put(DECODE, context.getExtraction().getAmdbDecodeString());
            groupData = getBarcodeAndPatronGroup(patronGroupStatement, patronGroupContext);
          } else {
            patronGroupContext.put(DECODE, context.getExtraction().getMsdbDecodeString());
            groupData = getBarcodeAndPatronGroup(patronGroupStatement, patronGroupContext);
          }

          String barcode = groupData.get(BARCODE);
          String patronGroup = groupData.get(PATRON_GROUP);

          String userId = job.getUserId();

          UserRecord userRecord = new UserRecord(username, id, externalSystemId, barcode, active, PATRON, patronGroup,
              personal, enrollmentDate, expirationDate);

          String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
          String createdByUserId = job.getUserId();

          try {
            String userUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(userRecord.toUser())));

            userRecordWriter.println(String.join("\t", userId, userUtf8Json, createdAt, createdByUserId, patronGroup));
          } catch (JsonProcessingException e) {
            log.error("{} user id {} error serializing user", schema, id);
          }
        }

        userRecordWriter.close();

        pageStatement.close();

        pageResultSet.close();

      } catch (SQLException e) {
        e.printStackTrace();
      } catch (ParseException e2) {
        e2.printStackTrace();
      }

      threadConnections.closeAll();

      log.info("{} {} user finished in {} milliseconds", schema, index,
          TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((UserPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private Optional<String> getUsername(Statement statement, Map<String, Object> context) throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      Optional<String> username = Optional.empty();
      while (resultSet.next()) {
        username = Optional.ofNullable(resultSet.getString(USERNAME));
        break;
      }
      return username;
    }
  }

  private void populateAddressesAndEmails(Statement statement, Map<String, Object> context, Personal personal)
      throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      List<Address> addresses = new ArrayList<>();
      while (resultSet.next()) {
        String addressTypeId = resultSet.getString(ADDRESS_TYPE);
        String addressLine1 = resultSet.getString(ADDRESS_LINE_1);

        if (addressTypeId.contains("3")) {
          personal.setEmail(addressLine1);
        } else {
          String addressLine2 = resultSet.getString(ADDRESS_LINE_2);
          String city = resultSet.getString(CITY);
          String region = resultSet.getString(REGION);
          String postalCode = resultSet.getString(POSTAL_CODE);
          String countryCode = resultSet.getString(COUNTRY_CODE);
          String addressStatus = resultSet.getString(ADDRESS_STATUS);
          String phoneTypes = resultSet.getString(PHONE_TYPES);
          String phoneNumber = resultSet.getString(PHONE_NUMBER);

          Address address = new Address();
          address.setCountryId(countryCode);
          address.setAddressLine1(addressLine1);
          address.setAddressLine2(addressLine2);
          address.setCity(city);
          address.setRegion(region);
          address.setPostalCode(postalCode);
          address.setAddressTypeId(addressTypeId);
          address.setPrimaryAddress(addressStatus.contains("N"));

          if (phoneTypes.equals(PRIMARY)) {
            personal.setPhone(phoneNumber);
          }
          if (phoneTypes.equals(MOBILE)) {
            personal.setMobilePhone(phoneNumber);
          }

          addresses.add(address);
        }
      }
      personal.setAddresses(addresses);
    }
  }

  private Map<String, String> getBarcodeAndPatronGroup(Statement statement, Map<String, Object> context) throws SQLException {
    Map<String, String> data = new HashMap<>();
    try (ResultSet resultSet = getResultSet(statement, context)) {
      data.put(BARCODE, resultSet.getString(BARCODE));
      data.put(PATRON_GROUP, resultSet.getString(PATRON_GROUP));
      return data;
    }
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database usernameSettings,
      Database folioSettings) {
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

  private class ThreadConnections {
    private Connection pageConnection;
    private Connection addressesConnection;
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

    public Connection getAdressesConnection() {
      return addressesConnection;
    }

    public void setAddressesConnection(Connection addressesConnection) {
      this.addressesConnection = addressesConnection;
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
        patronGroupConnection.close();
        usernameConnection.close();
        userConnection.close();
        addressesConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
