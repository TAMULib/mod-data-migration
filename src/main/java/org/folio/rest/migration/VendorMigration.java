package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Contact;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Organization;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.VendorAccountRecord;
import org.folio.rest.migration.model.VendorAddressRecord;
import org.folio.rest.migration.model.VendorAliasRecord;
import org.folio.rest.migration.model.VendorPhoneRecord;
import org.folio.rest.migration.model.VendorRecord;
import org.folio.rest.migration.model.request.VendorContext;
import org.folio.rest.migration.model.request.VendorDefaults;
import org.folio.rest.migration.model.request.VendorJob;
import org.folio.rest.migration.model.request.VendorMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

public class VendorMigration extends AbstractMigration<VendorContext> {

  private static final String VENDOR_ID = "VENDOR_ID";
  private static final String VENDOR_CODE = "VENDOR_CODE";
  private static final String VENDOR_NAME = "VENDOR_NAME";
  private static final String VENDOR_FEDERAL_TAX_ID = "FEDERAL_TAX_ID";
  private static final String VENDOR_DEFAULT_CURRENCY = "DEFAULT_CURRENCY";
  private static final String VENDOR_CLAIMING_INTERVAL = "CLAIM_INTERVAL";
  private static final String VENDOR_TYPE = "VENDOR_TYPE";

  private static final String ACCOUNT_DEPOSIT = "DEPOSIT";
  private static final String ACCOUNT_NAME = "ACCOUNT_NAME";
  private static final String ACCOUNT_NOTE = "NOTE";
  private static final String ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
  private static final String ACCOUNT_STATUS = "ACCOUNT_STATUS";

  private static final String ADDRESS_ID = "ADDRESS_ID";
  private static final String ADDRESS_CITY = "CITY";
  private static final String ADDRESS_CLAIM_ADDRESS = "CLAIM_ADDRESS";
  private static final String ADDRESS_CONTACT_NAME = "CONTACT_NAME";
  private static final String ADDRESS_CONTACT_TITLE = "CONTACT_TITLE";
  private static final String ADDRESS_COUNTRY = "COUNTRY";
  private static final String ADDRESS_EMAIL_ADDRESS = "EMAIL_ADDRESS";
  private static final String ADDRESS_LINE1 = "ADDRESS_LINE1";
  private static final String ADDRESS_LINE1_FULL = "ADDRESS_LINE1_FULL";
  private static final String ADDRESS_LINE2 = "ADDRESS_LINE2";
  private static final String ADDRESS_ORDER_ADDRESS = "ORDER_ADDRESS";
  private static final String ADDRESS_OTHER_ADDRESS = "OTHER_ADDRESS";
  private static final String ADDRESS_PAYMENT_ADDRESS = "PAYMENT_ADDRESS";
  private static final String ADDRESS_RETURN_ADDRESS = "RETURN_ADDRESS";
  private static final String ADDRESS_STATE_PROVINCE = "STATE_PROVINCE";
  private static final String ADDRESS_STD_ADDRESS_NUMBER = "STD_ADDRESS_NUMBER";
  private static final String ADDRESS_ZIP_POSTAL = "ZIP_POSTAL";

  private static final String ALIAS_ALT_VENDOR_NAME = "ALT_VENDOR_NAME";

  private static final String PHONE_NUMBER = "PHONE_NUMBER";
  private static final String PHONE_TYPE = "PHONE_TYPE";

  private static final String NOTE_NOTE = "NOTE";

  private static final String MAPS = "MAPS";
  private static final String DEFAULTS = "DEFAULTS";

  private static final String LOCATIONS = "LOCATIONS";
  private static final String STATUSES = "STATUSES";
  private static final String TYPES = "TYPES";

  private static final String CLAIM = "claim";
  private static final String ORDER = "order";
  private static final String OTHER = "other";
  private static final String PAYMENT = "payment";
  private static final String RETURN = "return";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";

  // (id,jsonb,creation_date,created_by)
  private static final String CONTACTS_RECORDS_COPY_SQL = "COPY %s_mod_organizations_storage.contacts (id,jsonb,creation_date,created_by) FROM STDIN";

  //(id,jsonb,creation_date,created_by)
  private static final String ORGANIZATIONS_RECORDS_COPY_SQL = "COPY %s_mod_organizations_storage.organizations (id,jsonb,creation_date,created_by) FROM STDIN";

  private VendorMigration(VendorContext context, String tenant) {
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

    taskQueue = new PartitionTaskQueue<VendorContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (VendorJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());
      countContext.put(LOCATIONS, job.getLocations());
      countContext.put(STATUSES, job.getStatuses());
      countContext.put(TYPES, job.getTypes());

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
        partitionContext.put(LOCATIONS, job.getLocations());
        partitionContext.put(STATUSES, job.getStatuses());
        partitionContext.put(TYPES, job.getTypes());

        taskQueue.submit(new VendorPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(true);
  }

  public static VendorMigration with(VendorContext context, String tenant) {
    return new VendorMigration(context, tenant);
  }

  public class VendorPartitionTask implements PartitionTask<VendorContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final VendorJob job;
    private final VendorMaps maps;
    private final VendorDefaults defaults;

    public VendorPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.job = (VendorJob) partitionContext.get(JOB);
      this.maps = (VendorMaps) partitionContext.get(MAPS);
      this.defaults = (VendorDefaults) partitionContext.get(DEFAULTS);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public VendorPartitionTask execute(VendorContext context) {
      long startTime = System.nanoTime();

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();
      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      log.info("starting {} {}", schema, index);

      int count = 0;

      try (
        PrintWriter contactsRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getContactsRecordConnection(), String.format(CONTACTS_RECORDS_COPY_SQL, tenant)), true);
        PrintWriter organizationsRecordWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getOrganizationRecordConnection(), String.format(ORGANIZATIONS_RECORDS_COPY_SQL, tenant)), true);

        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement accountStatement = threadConnections.getAccountConnection().createStatement();
        Statement addressStatement = threadConnections.getAddressConnection().createStatement();
        Statement aliasStatement = threadConnections.getAliasConnection().createStatement();
        Statement noteStatement = threadConnections.getNoteConnection().createStatement();
        Statement phoneStatement = threadConnections.getPhoneConnection().createStatement();

        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String vendorId = pageResultSet.getString(VENDOR_ID);
          String vendorCode = pageResultSet.getString(VENDOR_CODE);
          String vendorName = pageResultSet.getString(VENDOR_NAME);
          String vendorTaxId = pageResultSet.getString(VENDOR_FEDERAL_TAX_ID);
          String vendorType = pageResultSet.getString(VENDOR_TYPE);
          String vendorDefaultCurrency = pageResultSet.getString(VENDOR_DEFAULT_CURRENCY);
          Integer vendorClaimingInterval = pageResultSet.getInt(VENDOR_CLAIMING_INTERVAL);

          VendorRecord vendorRecord = new VendorRecord(vendorId, vendorCode, vendorType, vendorName, vendorTaxId, vendorDefaultCurrency, vendorClaimingInterval);

          try {
            processVendorAccounts(context, accountStatement, vendorRecord);
            processVendorAddresses(context, addressStatement, phoneStatement, vendorRecord, contactsRecordWriter, jsonStringEncoder);
            processVendorAliases(context, aliasStatement, vendorRecord);
            processVendorNotes(context, noteStatement, vendorRecord);

            String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);
            Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(vendorRLTypeId, vendorId);

            if (holdingRL.isPresent()) {
              vendorId = holdingRL.get().getFolioReference();
            }

            if (Objects.isNull(vendorId)) {
              log.error("{} no vendor record id found for id {}", schema, vendorId);
              continue;
            }

            vendorRecord.setCreatedByUserId(job.getUserId());
            vendorRecord.setCreatedDate(new Date());
            vendorRecord.setMaps(maps);
            vendorRecord.setDefaults(defaults);

            String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
            String createdByUserId = job.getUserId();

            Organization organization = vendorRecord.toOrganization();

            String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(organization)));

            // TODO: validate rows
            organizationsRecordWriter.println(String.join("\t", organization.getId(), hrUtf8Json, createdAt, createdByUserId));

            count++;
          } catch (JsonProcessingException e) {
            log.error("{} vendor id {} error processing json", schema, vendorId);
            log.debug(e.getMessage());
            continue;
          }
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} vendor finished {} in {} milliseconds", schema, index, count, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && ((VendorPartitionTask) obj).getIndex() == this.getIndex();
    }

    private void processVendorAccounts(VendorContext vendorContext, Statement statement, VendorRecord vendorRecord) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, vendorContext.getExtraction().getAccountSql());
      context.put(SCHEMA, job.getSchema());
      context.put(VENDOR_ID, vendorRecord.getVendorId());

      ResultSet resultSet = getResultSet(statement, context);

      while (resultSet.next()) {
        String deposit = resultSet.getString(ACCOUNT_DEPOSIT);
        String name = resultSet.getString(ACCOUNT_NAME);
        String note = resultSet.getString(ACCOUNT_NOTE);
        String number = resultSet.getString(ACCOUNT_NUMBER);
        String status = resultSet.getString(ACCOUNT_STATUS);

        VendorAccountRecord record = new VendorAccountRecord(vendorRecord.getVendorId(), deposit, name, note, number, status);
        record.setMaps(vendorContext.getMaps());
        record.setDefaults(vendorContext.getDefaults());

        vendorRecord.addAccount(record.toAccount());
      }

      resultSet.close();
    }

    private void processVendorAddresses(VendorContext vendorContext, Statement statement, Statement phoneStatement, VendorRecord vendorRecord, PrintWriter contactsRecordWriter, JsonStringEncoder jsonStringEncoder) throws JsonProcessingException, SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, vendorContext.getExtraction().getAddressSql());
      context.put(SCHEMA, job.getSchema());
      context.put(VENDOR_ID, vendorRecord.getVendorId());

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {

        while (resultSet.next()) {
          String id = resultSet.getString(ADDRESS_ID);
          String addressLine1 = resultSet.getString(ADDRESS_LINE1);
          String addressLine1Full = resultSet.getString(ADDRESS_LINE1_FULL);
          String addressLine2 = resultSet.getString(ADDRESS_LINE2);
          String city = resultSet.getString(ADDRESS_CITY);
          String contactName = resultSet.getString(ADDRESS_CONTACT_NAME);
          String contactTitle = resultSet.getString(ADDRESS_CONTACT_TITLE);
          String country = resultSet.getString(ADDRESS_COUNTRY);
          String emailAddress = resultSet.getString(ADDRESS_EMAIL_ADDRESS);
          String stateProvince = resultSet.getString(ADDRESS_STATE_PROVINCE);
          String stdAddressNumber = resultSet.getString(ADDRESS_STD_ADDRESS_NUMBER);
          String zipPostal = resultSet.getString(ADDRESS_ZIP_POSTAL);
          String phoneAddressId = vendorRecord.getVendorId();

          List<String> categories = buildVendorAddressesCategories(resultSet);

          if (Objects.nonNull(stdAddressNumber)) {
            vendorRecord.setStdAddressNumber(stdAddressNumber);
          }

          VendorAddressRecord record = new VendorAddressRecord(id, vendorRecord.getVendorId(), addressLine1, addressLine1Full, addressLine2, city, contactName, contactTitle, country, emailAddress, stateProvince, zipPostal, categories);
          record.setMaps(vendorContext.getMaps());
          record.setDefaults(vendorContext.getDefaults());

          if (record.isAddress()) {
            vendorRecord.addAddress(record.toAddress());
          } else if (record.isContact()) {
            Contact contact = record.toContact();

            String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
            String createdByUserId = job.getUserId();

            String hrUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(contact)));

            phoneAddressId = contact.getId();

            // TODO: validate rows
            contactsRecordWriter.println(String.join("\t", contact.getId(), hrUtf8Json, createdAt, createdByUserId));

            vendorRecord.addContact(contact.getId());
          } else if (record.isEmail()) {
            vendorRecord.addEmail(record.toEmail());
          } else if (record.isUrl()) {
            vendorRecord.addUrl(record.toUrl());
          } else {
            log.error("{} no known address type for address id {} for vendor id {}", job.getSchema(), id, vendorRecord.getVendorId());
            continue;
          }

          processAddressPhoneNumbers(vendorContext, phoneStatement, vendorRecord, categories, phoneAddressId);
        }

      } catch (SQLException e) {
        log.error("{} vendor id {} SQL error while processing addresses", job.getSchema(), vendorRecord.getVendorId());
        log.debug(e.getMessage());

        throw e;
      }
    }

    private void processVendorAliases(VendorContext vendorContext, Statement statement, VendorRecord vendorRecord) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, vendorContext.getExtraction().getAliasSql());
      context.put(SCHEMA, job.getSchema());
      context.put(VENDOR_ID, vendorRecord.getVendorId());

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {

        while (resultSet.next()) {
          String altVendorName = resultSet.getString(ALIAS_ALT_VENDOR_NAME);

          VendorAliasRecord record = new VendorAliasRecord(altVendorName);
          record.setMaps(vendorContext.getMaps());
          record.setDefaults(vendorContext.getDefaults());

          vendorRecord.addAlias(record.toAlias());
        }

      } catch (SQLException e) {
        log.error("{} vendor id {} SQL error while processing aliases", job.getSchema(), vendorRecord.getVendorId());
        log.debug(e.getMessage());

        throw e;
      }
    }

    private void processVendorNotes(VendorContext vendorContext, Statement statement, VendorRecord vendorRecord) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, vendorContext.getExtraction().getNoteSql());
      context.put(SCHEMA, job.getSchema());
      context.put(VENDOR_ID, vendorRecord.getVendorId());

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {

        while (resultSet.next()) {
          String note = resultSet.getString(NOTE_NOTE);

          if (Objects.nonNull(note)) {
            vendorRecord.setNotes(vendorRecord.getNotes() + " " + note);
          }
        }

      } catch (SQLException e) {
        log.error("{} vendor id {} SQL error while processing notes", job.getSchema(), vendorRecord.getVendorId());
        log.debug(e.getMessage());

        throw e;
      }
    }

    private void processAddressPhoneNumbers(VendorContext vendorContext, Statement statement, VendorRecord vendorRecord, List<String> categories, String addressId) throws SQLException {
      Map<String, Object> context = new HashMap<>();
      context.put(SQL, vendorContext.getExtraction().getPhoneSql());
      context.put(SCHEMA, job.getSchema());
      context.put(VENDOR_ID, vendorRecord.getVendorId());
      context.put(ADDRESS_ID, addressId);

      try (
        ResultSet resultSet = getResultSet(statement, context);
      ) {

        while (resultSet.next()) {
          String phoneNumber = resultSet.getString(PHONE_NUMBER);
          String phoneType = resultSet.getString(PHONE_TYPE);

          if (phoneNumber.contains("@") || phoneNumber.toLowerCase().matches("www\\.")) {
            log.error("{} phone number {} is an e-mail or URL for address id {} vendor id {}", job.getSchema(), phoneNumber, addressId, vendorRecord.getVendorId());
            continue;
          }

          VendorPhoneRecord record = new VendorPhoneRecord(vendorRecord.getVendorId(), addressId, phoneNumber, phoneType, categories);
          record.setMaps(vendorContext.getMaps());
          record.setDefaults(vendorContext.getDefaults());

          vendorRecord.addPhoneNumber(record.toPhoneNumber());
        }

      } catch (SQLException e) {
        log.error("{} vendor id {} SQL error while processing phone numbers", job.getSchema(), vendorRecord.getVendorId());
        log.debug(e.getMessage());

        throw e;
      }
    }

    private List<String> buildVendorAddressesCategories(ResultSet resultSet) throws SQLException {
      List<String> categories = new ArrayList<>();
      Map<String, String> categoriesMap = maps.getCategories();

      String claimAddress = resultSet.getString(ADDRESS_CLAIM_ADDRESS);
      String orderAddress = resultSet.getString(ADDRESS_ORDER_ADDRESS);
      String otherAddress = resultSet.getString(ADDRESS_OTHER_ADDRESS);
      String paymentAddress = resultSet.getString(ADDRESS_PAYMENT_ADDRESS);
      String returnAddress = resultSet.getString(ADDRESS_RETURN_ADDRESS);

      if (Objects.nonNull(claimAddress) && claimAddress.equalsIgnoreCase("y")) {
        categories.add(categoriesMap.get(CLAIM));
      }

      if (Objects.nonNull(orderAddress) && orderAddress.equalsIgnoreCase("y")) {
        categories.add(categoriesMap.get(ORDER));
      }

      if (Objects.nonNull(otherAddress) && otherAddress.equalsIgnoreCase("y")) {
        categories.add(categoriesMap.get(OTHER));
      }

      if (Objects.nonNull(paymentAddress) && paymentAddress.equalsIgnoreCase("y")) {
        categories.add(categoriesMap.get(PAYMENT));
      }

      if (Objects.nonNull(returnAddress) && returnAddress.equalsIgnoreCase("y")) {
        categories.add(categoriesMap.get(RETURN));
      }

      return categories;
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setAccountConnection(getConnection(voyagerSettings));
    threadConnections.setAddressConnection(getConnection(voyagerSettings));
    threadConnections.setAliasConnection(getConnection(voyagerSettings));
    threadConnections.setNoteConnection(getConnection(voyagerSettings));
    threadConnections.setPhoneConnection(getConnection(voyagerSettings));

    try {
      threadConnections.setContactsRecordConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
      threadConnections.setOrganizationRecordConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }

    return threadConnections;
  }

  private class ThreadConnections {
    private Connection pageConnection;
    private Connection accountConnection;
    private Connection addressConnection;
    private Connection aliasConnection;
    private Connection noteConnection;
    private Connection phoneConnection;

    private BaseConnection contactsRecordConnection;
    private BaseConnection organizationRecordConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getAccountConnection() {
      return accountConnection;
    }

    public void setAccountConnection(Connection accountConnection) {
      this.accountConnection = accountConnection;
    }

    public Connection getAddressConnection() {
      return addressConnection;
    }

    public void setAddressConnection(Connection addressConnection) {
      this.addressConnection = addressConnection;
    }

    public Connection getAliasConnection() {
      return aliasConnection;
    }

    public void setAliasConnection(Connection aliasConnection) {
      this.aliasConnection = aliasConnection;
    }

    public Connection getNoteConnection() {
      return noteConnection;
    }

    public void setNoteConnection(Connection noteConnection) {
      this.noteConnection = noteConnection;
    }

    public Connection getPhoneConnection() {
      return phoneConnection;
    }

    public void setPhoneConnection(Connection phoneConnection) {
      this.phoneConnection = phoneConnection;
    }

    public BaseConnection getContactsRecordConnection() {
      return contactsRecordConnection;
    }

    public void setContactsRecordConnection(BaseConnection contactsRecordConnection) {
      this.contactsRecordConnection = contactsRecordConnection;
    }

    public BaseConnection getOrganizationRecordConnection() {
      return organizationRecordConnection;
    }

    public void setOrganizationRecordConnection(BaseConnection organizationRecordConnection) {
      this.organizationRecordConnection = organizationRecordConnection;
    }

    public void closeAll() {
      closeConnection(pageConnection);
      closeConnection(accountConnection);
      closeConnection(addressConnection);
      closeConnection(aliasConnection);
      closeConnection(noteConnection);
      closeConnection(phoneConnection);

      closeBaseConnection(contactsRecordConnection);
      closeBaseConnection(organizationRecordConnection);
    }

    private void closeBaseConnection(BaseConnection baseConnection) {
      try {
        baseConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    private void closeConnection(Connection connection) {
      try {
        connection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

}
