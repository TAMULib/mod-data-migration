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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Address;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Contact;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Email;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Organization;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Url;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.VendorAccountRecord;
import org.folio.rest.migration.model.VendorAddressRecord;
import org.folio.rest.migration.model.VendorAliasRecord;
import org.folio.rest.migration.model.VendorPhoneRecord;
import org.folio.rest.migration.model.VendorRecord;
import org.folio.rest.migration.model.request.vendor.VendorContext;
import org.folio.rest.migration.model.request.vendor.VendorDefaults;
import org.folio.rest.migration.model.request.vendor.VendorJob;
import org.folio.rest.migration.model.request.vendor.VendorMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class VendorMigration extends AbstractMigration<VendorContext> {

  private static final String USER_ID = "USER_ID";

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

  private static final String NOTE = "NOTE";

  private static final String LOCATIONS = "LOCATIONS";
  private static final String STATUSES = "STATUSES";
  private static final String TYPES = "TYPES";

  private static final String CATEGORIES = "CATEGORIES";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";

  private static final Set<String> CODES = new HashSet<>();

  // (id,jsonb,creation_date,created_by)
  private static final String CONTACTS_RECORDS_COPY_SQL = "COPY %s_mod_organizations_storage.contacts (id,jsonb,creation_date,created_by) FROM STDIN WITH NULL AS 'null'";

  //(id,jsonb,creation_date,created_by)
  private static final String ORGANIZATIONS_RECORDS_COPY_SQL = "COPY %s_mod_organizations_storage.organizations (id,jsonb,creation_date,created_by) FROM STDIN WITH NULL AS 'null'";

  private VendorMigration(VendorContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<VendorContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        CODES.clear();
        migrationService.complete();
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
        partitionContext.put(LOCATIONS, job.getLocations());
        partitionContext.put(STATUSES, job.getStatuses());
        partitionContext.put(TYPES, job.getTypes());
        partitionContext.put(USER_ID, user.getId());
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new VendorPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static VendorMigration with(VendorContext context, String tenant) {
    return new VendorMigration(context, tenant);
  }

  public class VendorPartitionTask implements PartitionTask<VendorContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final ExecutorService additionalExecutor;

    public VendorPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.additionalExecutor = Executors.newFixedThreadPool(4);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public VendorPartitionTask execute(VendorContext context) {
      long startTime = System.nanoTime();

      VendorJob job = (VendorJob) partitionContext.get(JOB);

      VendorMaps maps = context.getMaps();
      VendorDefaults defaults = context.getDefaults();

      String userId = (String) partitionContext.get(USER_ID);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();
      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      Map<String, Object> vendorAccountsContext = new HashMap<>();
      vendorAccountsContext.put(SQL, context.getExtraction().getAccountSql());
      vendorAccountsContext.put(SCHEMA, job.getSchema());

      Map<String, Object> vendorAddressesContext = new HashMap<>();
      vendorAddressesContext.put(SQL, context.getExtraction().getAddressSql());
      vendorAddressesContext.put(SCHEMA, job.getSchema());

      Map<String, Object> vendorAliasesContext = new HashMap<>();
      vendorAliasesContext.put(SQL, context.getExtraction().getAliasSql());
      vendorAliasesContext.put(SCHEMA, job.getSchema());

      Map<String, Object> vendorNotesContext = new HashMap<>();
      vendorNotesContext.put(SQL, context.getExtraction().getNoteSql());
      vendorNotesContext.put(SCHEMA, job.getSchema());

      Map<String, Object> vendorAddressPhoneNumbersContext = new HashMap<>();
      vendorAddressPhoneNumbersContext.put(SQL, context.getExtraction().getPhoneSql());
      vendorAddressPhoneNumbersContext.put(SCHEMA, job.getSchema());

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      try (
        PrintWriter contactWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getContactsRecordConnection(), String.format(CONTACTS_RECORDS_COPY_SQL, tenant)), true);
        PrintWriter organizationWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getOrganizationRecordConnection(), String.format(ORGANIZATIONS_RECORDS_COPY_SQL, tenant)), true);
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

          vendorAccountsContext.put(VENDOR_ID, vendorId);
          vendorAddressesContext.put(VENDOR_ID, vendorId);
          vendorAddressPhoneNumbersContext.put(VENDOR_ID, vendorId);
          vendorAliasesContext.put(VENDOR_ID, vendorId);
          vendorNotesContext.put(VENDOR_ID, vendorId);

          Optional<ReferenceLink> vendorRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(vendorRLTypeId, vendorId);
          if (!vendorRL.isPresent()) {
            log.error("{} no vendor id found for vendor id {}", schema, vendorId);
            continue;
          }

          String referenceId = vendorRL.get().getFolioReference();

          if (maps.getVendorTypes().containsKey(vendorType)) {
            vendorType = maps.getVendorTypes().get(vendorType);
          }

          VendorRecord vendorRecord = new VendorRecord(referenceId, vendorId, vendorCode, vendorType, vendorName, vendorTaxId, vendorDefaultCurrency, vendorClaimingInterval);

          try {
            CompletableFuture.allOf(
              getVendorAccounts(accountStatement, vendorAccountsContext)
                .thenAccept((vendorAccountRecords) -> vendorRecord.setVendorAccountRecords(vendorAccountRecords)),
              getVendorAddresses(addressStatement, vendorAddressesContext)
                .thenAccept((vendorAddresses) -> vendorRecord.setVendorAddresses(vendorAddresses)),
              getVendorAliases(aliasStatement, vendorAliasesContext)
                .thenAccept((vendorAliases) -> vendorRecord.setVendorAliases(vendorAliases))
              // getVendorNotes(noteStatement, vendorNotesContext)
              //   .thenAccept((vendorNotes) -> vendorRecord.setVendorNotes(vendorNotes))
            ).get();

            List<VendorPhoneRecord> vendorPhoneNumbers = new ArrayList<>();

            List<Address> addresses = new ArrayList<>();
            List<String> contacts = new ArrayList<>();
            List<Email> emails = new ArrayList<>();
            List<Url> urls = new ArrayList<>();

            Date createdDate = new Date();
            String createdByUserId = userId;
            String createdAt = DATE_TIME_FOMATTER.format(new Date().toInstant().atOffset(ZoneOffset.UTC));

            for (VendorAddressRecord vendorAddress : vendorRecord.getVendorAddresses()) {
              vendorAddress.setVendorId(vendorId);
              vendorAddress.setCreatedDate(createdDate);
              vendorAddress.setCreatedByUserId(createdByUserId);

              List<String> categories = vendorAddress.getCategories(maps);

              if (vendorAddress.isAddress()) {
                addresses.add(vendorAddress.toAddress(categories, defaults, maps));
              } else if (vendorAddress.isContact()) {
                Contact contact = vendorAddress.toContact(categories, defaults, maps);

                String contactUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(contact)));

                contactWriter.println(String.join("\t", contact.getId(), contactUtf8Json, createdAt, createdByUserId));

                contacts.add(contact.getId());
              } else if (vendorAddress.isEmail()) {
                emails.add(vendorAddress.toEmail(categories));
              } else if (vendorAddress.isUrl()) {
                urls.add(vendorAddress.toUrl(categories));
              }
              vendorAddressPhoneNumbersContext.put(ADDRESS_ID, vendorAddress.getAddressId());
              vendorAddressPhoneNumbersContext.put(CATEGORIES, categories);
              vendorPhoneNumbers.addAll(getVendorAddressPhoneNumbers(phoneStatement, vendorAddressPhoneNumbersContext));

              vendorPhoneNumbers.forEach(vendorPhoneNumber -> {
                vendorPhoneNumber.setCreatedDate(createdDate);
                vendorPhoneNumber.setCreatedByUserId(userId);
              });
            }

            vendorRecord.setCreatedDate(createdDate);
            vendorRecord.setCreatedByUserId(userId);

            vendorRecord.setAddresses(addresses);
            vendorRecord.setContacts(contacts);
            vendorRecord.setEmails(emails);
            vendorRecord.setUrls(urls);
            vendorRecord.setVendorPhoneNumbers(vendorPhoneNumbers);

            Organization organization = vendorRecord.toOrganization(defaults);

            if (!processCode(organization.getCode().toLowerCase())) {
              log.warn("{} vendor id {} code {} already processed", schema, vendorId, organization.getCode());
              continue;
            }

            String orgUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(organization)));

            organizationWriter.println(String.join("\t", organization.getId(), orgUtf8Json, createdAt, createdByUserId));

          } catch (JsonProcessingException e) {
            log.error("{} vendor id {} error processing json", schema, vendorId);
            log.debug(e.getMessage());
            continue;
          }
        }

      } catch (SQLException | InterruptedException | ExecutionException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} vendor finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((VendorPartitionTask) obj).getIndex() == this.getIndex();
    }

    private CompletableFuture<List<VendorAccountRecord>> getVendorAccounts(Statement statement, Map<String, Object> vendorAccountsContext) {
      CompletableFuture<List<VendorAccountRecord>> future = new CompletableFuture<>();

      String vendorId = (String) vendorAccountsContext.get(VENDOR_ID);
      additionalExecutor.submit(() -> {
        List<VendorAccountRecord> vendorAccountRecords = new ArrayList<>();
        try (ResultSet resultSet = getResultSet(statement, vendorAccountsContext)) {
          while (resultSet.next()) {
            String deposit = resultSet.getString(ACCOUNT_DEPOSIT);
            String name = resultSet.getString(ACCOUNT_NAME);
            String note = resultSet.getString(ACCOUNT_NOTE);
            String number = resultSet.getString(ACCOUNT_NUMBER);
            String status = resultSet.getString(ACCOUNT_STATUS);
            vendorAccountRecords.add(new VendorAccountRecord(vendorId, deposit, name, note, number, status));
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(vendorAccountRecords);
        }
      });
      return future;
    }
  
    private CompletableFuture<List<VendorAddressRecord>> getVendorAddresses(Statement statement, Map<String, Object> vendorAddressesContext) {
      CompletableFuture<List<VendorAddressRecord>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        List<VendorAddressRecord> vendorAddresses = new ArrayList<>();
        try (
          ResultSet resultSet = getResultSet(statement, vendorAddressesContext);
        ) {
          while (resultSet.next()) {
            String addressId = resultSet.getString(ADDRESS_ID);
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
            String claimAddress = resultSet.getString(ADDRESS_CLAIM_ADDRESS);
            String orderAddress = resultSet.getString(ADDRESS_ORDER_ADDRESS);
            String otherAddress = resultSet.getString(ADDRESS_OTHER_ADDRESS);
            String paymentAddress = resultSet.getString(ADDRESS_PAYMENT_ADDRESS);
            String returnAddress = resultSet.getString(ADDRESS_RETURN_ADDRESS);
            vendorAddresses.add(new VendorAddressRecord(addressId, addressLine1, addressLine1Full, addressLine2, city, contactName, contactTitle, country, emailAddress, stateProvince, stdAddressNumber, zipPostal, claimAddress, orderAddress, otherAddress, paymentAddress, returnAddress));
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(vendorAddresses);
        }
      });
      return future;
    }

    private CompletableFuture<List<VendorAliasRecord>> getVendorAliases(Statement statement, Map<String, Object> vendorAliasesContext) {
      CompletableFuture<List<VendorAliasRecord>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        List<VendorAliasRecord> vendorAliases = new ArrayList<>();
        try (ResultSet resultSet = getResultSet(statement, vendorAliasesContext)) {
          while (resultSet.next()) {
            String altVendorName = resultSet.getString(ALIAS_ALT_VENDOR_NAME);
            if (StringUtils.isNotEmpty(altVendorName)) {
              vendorAliases.add(new VendorAliasRecord(altVendorName));
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(vendorAliases);
        }
      });
      return future;
    }

    private List<VendorPhoneRecord> getVendorAddressPhoneNumbers(Statement statement, Map<String, Object> vendorAddressPhoneNumberContext) throws SQLException {
      List<VendorPhoneRecord> vendorPhoneNumbers = new ArrayList<>();
      String schema = (String) vendorAddressPhoneNumberContext.get(SCHEMA);
      String vendorId = (String) vendorAddressPhoneNumberContext.get(VENDOR_ID);
      String addressId = (String) vendorAddressPhoneNumberContext.get(ADDRESS_ID);
      List<String> categories = (List<String>) vendorAddressPhoneNumberContext.get(CATEGORIES);
      try (ResultSet resultSet = getResultSet(statement, vendorAddressPhoneNumberContext)) {
        while (resultSet.next()) {
          String phoneNumber = resultSet.getString(PHONE_NUMBER);
          String phoneType = resultSet.getString(PHONE_TYPE);
          if (phoneNumber.contains("@") || phoneNumber.toLowerCase().matches("www\\.")) {
            log.error("{} E-mail or URL is used as phone number for vendor id {} for address id {}", schema, vendorId, addressId);
            continue;
          }
          vendorPhoneNumbers.add(new VendorPhoneRecord(addressId, phoneNumber, phoneType, categories));
        }
      }
      return vendorPhoneNumbers;
    }
  
    private CompletableFuture<String> getVendorNotes(Statement statement, Map<String, Object> vendorNotesContext) {
      CompletableFuture<String> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        StringBuilder vendorNotes = new StringBuilder();
        try (ResultSet resultSet = getResultSet(statement, vendorNotesContext)) {
          while (resultSet.next()) {
            String note = resultSet.getString(NOTE);
            if (StringUtils.isNotEmpty(note)) {
              if (vendorNotes.length() > 0) {
                vendorNotes.append(StringUtils.SPACE);
              }
              vendorNotes.append(note);
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(vendorNotes.toString());
        }
      });
      return future;
    }

  }

  private synchronized Boolean processCode(String code) {
    return CODES.add(code);
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
      try {
        pageConnection.close();
        accountConnection.close();
        addressConnection.close();
        aliasConnection.close();
        noteConnection.close();
        phoneConnection.close();
        contactsRecordConnection.close();
        organizationRecordConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
