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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.notes.raml_util.schemas.tagged_record_example.Metadata;
import org.folio.rest.jaxrs.model.notes.types.notes.Link;
import org.folio.rest.jaxrs.model.notes.types.notes.Note;
import org.folio.rest.jaxrs.model.users.Addresstype;
import org.folio.rest.jaxrs.model.users.AddresstypeCollection;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.jaxrs.model.users.Usergroup;
import org.folio.rest.jaxrs.model.users.Usergroups;
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

public class UserMigration extends AbstractMigration<UserContext> {

  private static final String USER_GROUPS = "USER_GROUPS";
  private static final String ADDRESS_TYPES = "ADDRESS_TYPES";

  private static final String USER_ID = "USER_ID";

  private static final String WHERE_CLAUSE = "WHERE_CLAUSE";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String EXTERNAL_SYSTEM_ID = "EXTERNAL_SYSTEM_ID";
  private static final String LAST_NAME = "LAST_NAME";
  private static final String FIRST_NAME = "FIRST_NAME";
  private static final String MIDDLE_NAME = "MIDDLE_NAME";
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

  private static final String NOTE = "NOTE";

  private static final Set<String> BARCODES = new HashSet<>();
  private static final Set<String> USERNAMES = new HashSet<>();

  // (id,jsonb,creation_date,created_by,patrongroup)
  private static final String USERS_COPY_SQL = "COPY %s_mod_users.users (id,jsonb,creation_date,created_by,patrongroup) FROM STDIN";

  // (id,jsonb,temporary_type_id)
  private static final String NOTES_COPY_SQL = "COPY %s_mod_notes.note_data (id,jsonb) FROM STDIN";

  private UserMigration(UserContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    Usergroups usergroups = migrationService.okapiService.fetchUsergroups(tenant, token);

    AddresstypeCollection addresstypes = migrationService.okapiService.fetchAddresstypes(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<UserContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        USERNAMES.clear();
        BARCODES.clear();
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (UserJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      Userdata user = migrationService.okapiService.lookupUser(tenant, token, job.getUser());

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
        partitionContext.put(ADDRESS_TYPES, addresstypes);
        partitionContext.put(USER_ID, user.getId());
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new UserPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static UserMigration with(UserContext context, String tenant) {
    return new UserMigration(context, tenant);
  }

  public class UserPartitionTask implements PartitionTask<UserContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final ExecutorService additionalExecutor;

    public UserPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.additionalExecutor = Executors.newFixedThreadPool(3);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public UserPartitionTask execute(UserContext context) {
      long startTime = System.nanoTime();

      UserJob job = (UserJob) partitionContext.get(JOB);

      Usergroups usergroups = (Usergroups) partitionContext.get(USER_GROUPS);
      AddresstypeCollection addresstypes = (AddresstypeCollection) partitionContext.get(ADDRESS_TYPES);

      String userId = (String) partitionContext.get(USER_ID);

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

      Map<String, Object> patronNoteContext = new HashMap<>();
      patronNoteContext.put(SQL, context.getExtraction().getPatronNoteSql());
      patronNoteContext.put(SCHEMA, job.getSchema());
      patronNoteContext.put(WHERE_CLAUSE, job.getNoteWhereClause());

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, usernameSettings, folioSettings);

      try (
        PrintWriter userWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getUserConnection(), String.format(USERS_COPY_SQL, tenant)), true);
        PrintWriter noteWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getNoteConnection(), String.format(NOTES_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement usernameStatement = threadConnections.getUsernameConnection().createStatement();
        Statement addressStatement = threadConnections.getAddressConnection().createStatement();
        Statement patronGroupStatement = threadConnections.getPatronGroupConnection().createStatement();
        Statement patronNoteStatement = threadConnections.getPatronNoteConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String patronId = pageResultSet.getString(PATRON_ID);
          String externalSystemId = pageResultSet.getString(EXTERNAL_SYSTEM_ID);
          String lastName = pageResultSet.getString(LAST_NAME);
          String firstName = pageResultSet.getString(FIRST_NAME);
          String middleName = pageResultSet.getString(MIDDLE_NAME);
          String expireDate = pageResultSet.getString(EXPIRE_DATE);
          String smsNumber = pageResultSet.getString(SMS_NUMBER);
          String currentCharges = pageResultSet.getString(CURRENT_CHARGES);

          usernameContext.put(PATRON_ID, patronId);
          usernameContext.put(EXTERNAL_SYSTEM_ID, externalSystemId);
          addressContext.put(PATRON_ID, patronId);
          patronGroupContext.put(PATRON_ID, patronId);
          patronNoteContext.put(PATRON_ID, patronId);

          List<ReferenceLink> userReferenceLinks = migrationService.referenceLinkRepo.findAllByExternalReference(externalSystemId);

          if (userReferenceLinks.isEmpty()) {
            log.error("{} no user id found for patron id {} with external id {}", schema, patronId, externalSystemId);
            continue;
          }

          String referenceId = userReferenceLinks.get(0).getFolioReference().toString();

          List<PatronNote> patronNotes = new ArrayList<>();

          String createdByUserId = userId;
          Date createdDate = new Date();

          if (job.getSkipDuplicates() && userReferenceLinks.size() > 1) {

            getPatronNotes(patronNoteStatement, patronNoteContext)
              .thenAccept((pn) -> patronNotes.addAll(pn))
              .get();

            for (PatronNote patronNote : patronNotes) {
              Note note = patronNote.toNote(referenceId, job.getDbCode(), job.getNoteTypeId(), createdByUserId, createdDate);
              try {
                String noteUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(note)));
                noteWriter.println(String.join("\t", note.getId(), noteUtf8Json));
              } catch (JsonProcessingException e) {
                log.error("{} patron id {} note id {} error serializing note", schema, patronId, note.getId());
              }
            }

            continue;
          }

          UserRecord userRecord = new UserRecord(referenceId, patronId, externalSystemId, lastName, firstName, middleName, expireDate, smsNumber, currentCharges);

          PatronCodes patronCodes = new PatronCodes();

          CompletableFuture.allOf(
            getUsername(usernameStatement, usernameContext)
              .thenAccept((un) -> userRecord.setUsername(un)),
            getUserAddressRecords(addressStatement, addressContext, addresstypes)
              .thenAccept((uar) -> userRecord.setUserAddressRecords(uar)),
            getPatronCodes(patronGroupStatement, patronGroupContext)
              .thenAccept((pc) -> {
                patronCodes.setBarcode(pc.getBarcode());
                patronCodes.setGroupcode(pc.getGroupcode());
              }),
            getPatronNotes(patronNoteStatement, patronNoteContext)
              .thenAccept((pn) -> patronNotes.addAll(pn))
          ).get();

          if (!processUsername(userRecord.getUsername().toLowerCase())) {
            log.warn("{} patron id {} username {} already processed", schema, patronId, userRecord.getUsername());
            continue;
          }

          if (Objects.nonNull(patronCodes.getBarcode()) && !processBarcode(patronCodes.getBarcode().toLowerCase())) {
            log.warn("{} patron id {} barcode {} already processed", schema, patronId, patronCodes.getBarcode());
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

          userRecord.setCreatedByUserId(userId);
          userRecord.setCreatedDate(createdDate);

          Userdata userdata = userRecord.toUserdata(patronGroup.get(), defaults, maps);

          String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));

          try {
            String userUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(userdata)));

            userWriter.println(String.join("\t", userdata.getId(), userUtf8Json, createdAt, createdByUserId, userdata.getPatronGroup()));

            for (PatronNote patronNote : patronNotes) {
              Note note = patronNote.toNote(referenceId, job.getDbCode(), job.getNoteTypeId(), createdByUserId, createdDate);
              try {
                String noteUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(note)));
                noteWriter.println(String.join("\t", note.getId(), noteUtf8Json));
              } catch (JsonProcessingException e) {
                log.error("{} patron id {} note id {} error serializing note", schema, patronId, note.getId());
              }
            }
          } catch (JsonProcessingException e) {
            log.error("{} patron id {} error serializing user", schema, patronId);
          }
        }

      } catch (SQLException | InterruptedException | ExecutionException e) {
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

    private CompletableFuture<String> getUsername(Statement statement, Map<String, Object> usernameContext) {
      CompletableFuture<String> future = new CompletableFuture<>();
      String schema = (String) usernameContext.get(SCHEMA);
      String patronId = (String) usernameContext.get(PATRON_ID);
      additionalExecutor.submit(() -> {
        String username = String.format("%s_%s", schema, patronId);
        try (ResultSet resultSet = getResultSet(statement, usernameContext)) {
          while (resultSet.next()) {
            String netid = resultSet.getString(USERNAME_NETID);
            if (StringUtils.isNotEmpty(netid)) {
              username = netid.toLowerCase();
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(username);
        }
      });
      return future;
    }
  
    private CompletableFuture<List<UserAddressRecord>> getUserAddressRecords(Statement statement, Map<String, Object> addressContext, AddresstypeCollection addresstypes) {
      CompletableFuture<List<UserAddressRecord>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
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
            Optional<String> addressTypeId = getAddressTypeId(addressDescription, addresstypes);
            userAddressRecords.add(new UserAddressRecord(addressTypeId.isPresent() ? addressTypeId.get() : addressDescription, addressStatus, addressType, addressLine1, addressLine2, city, country, phoneNumber, phoneDescription, stateProvince, zipPostal));
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(userAddressRecords);
        }
      });
      return future;
    }
  
    private CompletableFuture<PatronCodes> getPatronCodes(Statement statement, Map<String, Object> patronGroupContext) {
      CompletableFuture<PatronCodes> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
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
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(patronCodes);
        }
      });
      return future;
    }

    private CompletableFuture<List<PatronNote>> getPatronNotes(Statement statement, Map<String, Object> patronNoteContext) {
      CompletableFuture<List<PatronNote>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        List<PatronNote> patronNotes = new ArrayList<>();
        try (ResultSet resultSet = getResultSet(statement, patronNoteContext)) {
          while(resultSet.next()) {
            String note = String.format("<p>%s</p>", resultSet.getString(NOTE));
            patronNotes.add(new PatronNote(note));
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(patronNotes);
        }
      });
      return future;
    }

  }

  private synchronized Boolean processUsername(String username) {
    return USERNAMES.add(username);
  }

  private synchronized Boolean processBarcode(String barcode) {
    return BARCODES.add(barcode);
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database usernameSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setAddressConnection(getConnection(voyagerSettings));
    threadConnections.setPatronGroupConnection(getConnection(voyagerSettings));
    threadConnections.setPatronNoteConnection(getConnection(voyagerSettings));
    threadConnections.setUsernameConnection(getConnection(usernameSettings));

    try {
      threadConnections.setUserConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
      threadConnections.setNoteConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }

    return threadConnections;
  }

  private Optional<String> getPatronGroup(String groupcode, Usergroups usergroups) {
    Optional<Usergroup> usergroup = usergroups.getUsergroups().stream()
      .filter(ug -> ug.getGroup().equals(groupcode))
      .findAny();
    if (usergroup.isPresent()) {
      return Optional.of(usergroup.get().getId());
    }
    return Optional.empty();
  }

  private Optional<String> getAddressTypeId(String addressDescription, AddresstypeCollection addresstypes) {
    Optional<Addresstype> addresstype = addresstypes.getAddressTypes().stream()
      .filter(at -> at.getAddressType().equalsIgnoreCase(addressDescription))
      .findAny();
    if (addresstype.isPresent()) {
      return Optional.of(addresstype.get().getId());
    }
    return Optional.empty();
  }

  private class ThreadConnections {

    private Connection pageConnection;
    private Connection addressConnection;
    private Connection patronGroupConnection;
    private Connection patronNoteConnection;
    private Connection usernameConnection;

    private BaseConnection userConnection;
    private BaseConnection noteConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getAddressConnection() {
      return addressConnection;
    }

    public void setAddressConnection(Connection addressConnection) {
      this.addressConnection = addressConnection;
    }

    public Connection getPatronGroupConnection() {
      return patronGroupConnection;
    }

    public void setPatronGroupConnection(Connection patronGroupConnection) {
      this.patronGroupConnection = patronGroupConnection;
    }

    public Connection getPatronNoteConnection() {
      return patronNoteConnection;
    }

    public void setPatronNoteConnection(Connection patronNoteConnection) {
      this.patronNoteConnection = patronNoteConnection;
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

    public BaseConnection getNoteConnection() {
      return noteConnection;
    }

    public void setNoteConnection(BaseConnection noteConnection) {
      this.noteConnection = noteConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        addressConnection.close();
        patronGroupConnection.close();
        patronNoteConnection.close();
        usernameConnection.close();
        userConnection.close();
        noteConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

  private class PatronCodes {

    private String groupcode;
    private String barcode;

    public PatronCodes() {

    }

    public PatronCodes(String groupcode, String barcode) {
      this.groupcode = groupcode;
      this.barcode = barcode;
    }

    public String getGroupcode() {
      return groupcode;
    }

    public void setGroupcode(String groupcode) {
      this.groupcode = groupcode;
    }

    public String getBarcode() {
      return barcode;
    }

    public void setBarcode(String barcode) {
      this.barcode = barcode;
    }

  }

  private class PatronNote {

    private String content;

    public PatronNote(final String content) {
      this.content = content;
    }

    public Note toNote(String userId, String dbCode, String noteTypeId, String createdByUserId, Date createdDate) {
      Note note = new Note();

      note.setId(UUID.randomUUID().toString());
      note.setTitle(String.format("Patron note (migrated %s)", dbCode));
      note.setDomain("users");
      note.setTypeId(noteTypeId);

      note.setContent(content.replaceAll("(\r\n|\n)", "<br />"));

      List<Link> links = new ArrayList<>();
      Link link = new Link();
      link.setId(userId);
      link.setType("user");

      links.add(link);

      note.setLinks(links);

      Metadata metadata = new Metadata();
      metadata.setCreatedByUserId(createdByUserId);
      metadata.setCreatedDate(createdDate);
      metadata.setUpdatedByUserId(createdByUserId);
      metadata.setUpdatedDate(createdDate);
      note.setMetadata(metadata);

      return note;
    }

  }

}
