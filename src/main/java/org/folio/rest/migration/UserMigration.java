package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.UserAddressRecord;
import org.folio.rest.migration.model.UserRecord;
import org.folio.rest.migration.model.request.UserContext;
import org.folio.rest.migration.model.request.UserDefaults;
import org.folio.rest.migration.model.request.UserJob;
import org.folio.rest.migration.model.request.UserMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class UserMigration extends AbstractMigration<UserContext> {

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PATRON_INSTITUTION_ID = "INSTITUTION_ID";
  private static final String PATRON_LAST_NAME = "LAST_NAME";
  private static final String PATRON_FIRST_NAME = "FIRST_NAME";
  private static final String PATRON_MIDDLE_NAME = "MIDDLE_NAME";
  private static final String PATRON_ACTIVE_DATE = "ACTIVE_DATE";
  private static final String PATRON_EXPIRE_DATE = "EXPIRE_DATE";
  private static final String PATRON_SMS_NUMBER = "SMS_NUMBER";
  private static final String PATRON_CURRENT_CHARGES = "CURRENT_CHARGES";
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

  private static final String PHONE_PRIMARY = "Primary";
  private static final String PHONE_MOBILE = "Mobile";

  private static final String MAPS = "MAPS";
  private static final String DEFAULTS = "DEFAULTS";
  private static final String JOIN_FROM = "JOIN_FROM";
  private static final String JOIN_WHERE = "JOIN_WHERE";
  private static final String DECODE = "DECODE";

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
      countContext.put(JOIN_FROM, job.getJoinFromSql());
      countContext.put(JOIN_WHERE, job.getJoinWhereSql());

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
        partitionContext.put(MAPS, context.getMaps());
        partitionContext.put(DEFAULTS, context.getDefaults());
        partitionContext.put(JOIN_FROM, job.getJoinFromSql());
        partitionContext.put(JOIN_WHERE, job.getJoinWhereSql());
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

    private final UserJob job;
    private final UserMaps maps;
    private final UserDefaults defaults;

    public UserPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.job = (UserJob) partitionContext.get(JOB);
      this.maps = (UserMaps) partitionContext.get(MAPS);
      this.defaults = (UserDefaults) partitionContext.get(DEFAULTS);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public UserPartitionTask execute(UserContext context) {
      long startTime = System.nanoTime();

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

      try (
        PrintWriter userRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getUserConnection(), String.format(USERS_COPY_SQL, tenant)), true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement usernameStatement = threadConnections.getUsernameConnection().createStatement();
        Statement addressStatement = threadConnections.getAdressConnection().createStatement();
        Statement patronGroupStatement = threadConnections.getPatronGroupConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {

          String patronId = pageResultSet.getString(PATRON_ID);
          String institutionId = pageResultSet.getString(PATRON_INSTITUTION_ID);
          String lastName = pageResultSet.getString(PATRON_LAST_NAME);
          String firstName = pageResultSet.getString(PATRON_FIRST_NAME);
          String middleName = pageResultSet.getString(PATRON_MIDDLE_NAME);
          String activeDate = pageResultSet.getString(PATRON_ACTIVE_DATE);
          String expireDate = pageResultSet.getString(PATRON_EXPIRE_DATE);
          String smsNumber = pageResultSet.getString(PATRON_SMS_NUMBER);
          String currentCharges = pageResultSet.getString(PATRON_CURRENT_CHARGES);

          Optional<ReferenceLink> userRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(userIdRLTypeId, patronId);
          if (!userRL.isPresent()) {
            log.error("{} no user id found for patron id {}", schema, patronId);
            continue;
          }

          Optional<ReferenceLink> userExternalRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(userExternalIdRLTypeId, institutionId);
          if (!userExternalRL.isPresent()) {
            log.error("{} no user external id found for external system id {}", schema, institutionId);
            continue;
          }

          String referenceId = userRL.get().getFolioReference().toString();

          UserRecord userRecord = new UserRecord(referenceId, patronId, institutionId, lastName, firstName, middleName, activeDate, expireDate, smsNumber, currentCharges);
          userRecord.setSchema(schema);
          userRecord.setMaps(maps);
          userRecord.setDefaults(defaults);

          processUsername(context, usernameStatement, userRecord);
          processAddressesEmailsPhones(context, addressStatement, userRecord);
          processBarcodeAndPatronGroup(context, patronGroupStatement, userRecord);

          String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
          String createdByUserId = job.getUserId();

          try {
            String userUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(userRecord.toUserdata())));

            userRecordWriter.println(String.join("\t", job.getUserId(), userUtf8Json, createdAt, createdByUserId, userRecord.getGroupcode()));
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

    private void processAddressesEmailsPhones(UserContext userContext, Statement statement, UserRecord userRecord) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, userContext.getExtraction().getAddressSql());
      context.put(SCHEMA, job.getSchema());
      context.put(PATRON_ID, userRecord.getPatronId());

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {
        boolean permanentStatusNormal = false;
        boolean temporaryStatusNormal = false;

        List<String> phoneNumbers = new ArrayList<>();
        List<String> phoneTypes = new ArrayList<>();

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

          UserAddressRecord userAddressRecord = new UserAddressRecord(addressDescription, addressStatus, addressType, addressLine1, addressLine2, city, country, phoneNumber, phoneDescription, stateProvince, zipPostal);

          if (userAddressRecord.isEmail()) {
            userRecord.setEmail(userAddressRecord.toEmail());
          } else {
            if (userAddressRecord.hasPhoneNumber()) {
              // phone type is stored as phone description.
              phoneNumbers.add(userAddressRecord.getPhoneNumber());
              phoneTypes.add(userAddressRecord.getPhoneDescription());
            } else {
              phoneNumbers.add("");
              phoneTypes.add("");
            }

            if (userAddressRecord.isPrimary()) {
              if (userAddressRecord.isNormal()) {
                permanentStatusNormal = true;
              }
            } else if (userAddressRecord.isTemporary()) {
              if (userAddressRecord.isNormal()) {
                temporaryStatusNormal = true;
              }
            }

            userRecord.addAddresses(userAddressRecord.toAddress());
          }
        }

        if (permanentStatusNormal && temporaryStatusNormal) {
          userRecord.getAddresses().forEach(userAddressRecord -> {
            if (userAddressRecord.getPrimaryAddress()) {
              userAddressRecord.setPrimaryAddress(defaults.getPrimaryAddress());
            } else {
              userAddressRecord.setPrimaryAddress(!defaults.getPrimaryAddress());
            }
          });
        }

        for (int i = 0; i < userRecord.getAddresses().size(); i++) {
          Address address = userRecord.getAddresses().get(i);
          if (address.getPrimaryAddress()) {
            if (phoneTypes.get(i).equalsIgnoreCase(PHONE_PRIMARY)) {
              userRecord.setPhone(phoneNumbers.get(i));
            }
            else if (phoneTypes.get(i).equalsIgnoreCase(PHONE_MOBILE)) {
              userRecord.setMobilePhone(phoneNumbers.get(i));
            }
          }
        }
      } catch (SQLException e) {
        log.error("{} user id {} SQL error while processing addresses, emails, and phone numbers", job.getSchema(), userRecord.getPatronId());
        log.debug(e.getMessage());

        throw e;
      }
    }

    private void processUsername(UserContext userContext, Statement statement, UserRecord userRecord) throws SQLException {
      if (Objects.nonNull(userRecord.getInstitutionId())) {
        Map<String, Object> context = new HashMap<>();
        context.put(SQL, userContext.getExtraction().getUsernameSql());
        context.put(SCHEMA, job.getSchema());
        context.put(PATRON_INSTITUTION_ID, userRecord.getInstitutionId());
  
        try (
          ResultSet resultSet = getResultSet(statement, context);
        ) {
          boolean found = false;

          while (resultSet.next()) {
            String username = resultSet.getString(USERNAME_NETID);

            if (Objects.nonNull(username)) {
              found = true;
              userRecord.setUsername(username);
            }
          }

          if (!found) {
            userRecord.setUsername(job.getSchema() + "_" + userRecord.getPatronId());
          }
        } catch (SQLException e) {
          log.error("{} user id {} SQL error while processing username", job.getSchema(), userRecord.getPatronId());
          log.debug(e.getMessage());

          throw e;
        }
      } else {
        userRecord.setUsername(job.getSchema() + "_" + userRecord.getPatronId());
      }
    }

    private void processBarcodeAndPatronGroup(UserContext userContext, Statement statement, UserRecord userRecord) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, userContext.getExtraction().getPatronGroupSql());
      context.put(SCHEMA, job.getSchema());
      context.put(DECODE, job.getDecodeSql());
      context.put(PATRON_ID, userRecord.getPatronId());

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {
        Map<String, String> patronGroupMap = maps.getPatronGroup().get(job.getSchema());

        while(resultSet.next()) {
          String barcode = resultSet.getString(PATRON_BARCODE);
          String groupCode = resultSet.getString(PATRON_GROUP_CODE);

          userRecord.setBarcode(barcode);

          if (Objects.nonNull(groupCode) && patronGroupMap.containsKey(groupCode.toLowerCase())) {
            userRecord.setGroupcode(patronGroupMap.get(groupCode.toLowerCase()));
          }

          // only grab the first row found. 
          break;
        }
      } catch (SQLException e) {
        log.error("{} user id {} SQL error while processing barcode and patron group", job.getSchema(), userRecord.getPatronId());
        log.debug(e.getMessage());

        throw e;
      }
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
        patronGroupConnection.close();
        usernameConnection.close();
        userConnection.close();
        addressConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
