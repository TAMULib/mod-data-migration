package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.plexus.util.StringUtils;
import org.folio.rest.jaxrs.model.userimport.schemas.Address;
import org.folio.rest.jaxrs.model.userimport.schemas.ImportResponse;
import org.folio.rest.jaxrs.model.userimport.schemas.Personal;
import org.folio.rest.jaxrs.model.userimport.schemas.Userdataimport;
import org.folio.rest.jaxrs.model.userimport.schemas.UserdataimportCollection;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronContext;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.FormatUtility;
import org.folio.rest.migration.utility.TimingUtility;

public class DivITPatronMigration extends AbstractMigration<DivITPatronContext> {

  private static final String USERNAME = "USERNAME";
  private static final String EXTERNALSYSTEMID = "EXTERNALSYSTEMID";
  private static final String BARCODE = "BARCODE";
  private static final String ACTIVE = "ACTIVE";
  private static final String PATRONGROUP = "PATRONGROUP";
  private static final String PERSONAL_LASTNAME = "PERSONAL_LASTNAME";
  private static final String PERSONAL_FIRSTNAME = "PERSONAL_FIRSTNAME";
  private static final String PERSONAL_MIDDLENAME = "PERSONAL_MIDDLENAME";
  private static final String PERSONAL_EMAIL = "PERSONAL_EMAIL";
  private static final String PERSONAL_PHONE = "PERSONAL_PHONE";
  private static final String ADDRESSES_PERMANENT_ADDRESSTYPEID = "ADDRESSES_PERMANENT_ADDRESSTYPEID";
  private static final String ADDRESSES_PERMANENT_COUNTRYID = "ADDRESSES_PERMANENT_COUNTRYID";
  private static final String ADDRESSES_PERMANENT_ADDRESSLINE1 = "ADDRESSES_PERMANENT_ADDRESSLINE1";
  private static final String ADDRESSES_PERMANENT_ADDRESSLINE2 = "ADDRESSES_PERMANENT_ADDRESSLINE2";
  private static final String ADDRESSES_PERMANENT_CITY = "ADDRESSES_PERMANENT_CITY";
  private static final String ADDRESSES_PERMANENT_REGION = "ADDRESSES_PERMANENT_REGION";
  private static final String ADDRESSES_PERMANENT_POSTALCODE = "ADDRESSES_PERMANENT_POSTALCODE";
  private static final String ADDRESSES_TEMPORARY_ADDRESSTYPEID = "ADDRESSES_TEMPORARY_ADDRESSTYPEID";
  private static final String ADDRESSES_TEMPORARY_ADDRESSLINE1 = "ADDRESSES_TEMPORARY_ADDRESSLINE1";
  private static final String ADDRESSES_TEMPORARY_ADDRESSLINE2 = "ADDRESSES_TEMPORARY_ADDRESSLINE2";
  private static final String ADDRESSES_TEMPORARY_CITY = "ADDRESSES_TEMPORARY_CITY";
  private static final String ADDRESSES_TEMPORARY_REGION = "ADDRESSES_TEMPORARY_REGION";
  private static final String ADDRESSES_TEMPORARY_POSTALCODE = "ADDRESSES_TEMPORARY_POSTALCODE";
  private static final String DEPARTMENTS_0 = "DEPARTMENTS_0";
  private static final String EXPIRATIONDATE = "EXPIRATIONDATE";

  private static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd";

  private DivITPatronMigration(DivITPatronContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);

    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<DivITPatronContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    int index = 0;

    for (DivITPatronJob job : context.getJobs()) {
      Map<String, Object> partitionContext = new HashMap<>();
      partitionContext.put(SQL, job.getSql());
      partitionContext.put(INDEX, index);
      partitionContext.put(TOKEN, token);
      partitionContext.put(JOB, job);
      log.info("submitting job {} index {}", job.getName(), index++);
      taskQueue.submit(new DivITPatronPartitionTask(migrationService, partitionContext));
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static DivITPatronMigration with(DivITPatronContext context, String tenant) {
    return new DivITPatronMigration(context, tenant);
  }

  public class DivITPatronPartitionTask implements PartitionTask<DivITPatronContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public DivITPatronPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public DivITPatronPartitionTask execute(DivITPatronContext context) {
      long startTime = System.nanoTime();

      Database settings = context.getDatabase();

      ThreadConnections threadConnections = getThreadConnections(settings);

      DivITPatronJob job = (DivITPatronJob) partitionContext.get(JOB);

      String token = (String) partitionContext.get(TOKEN);

      log.info("processing {} patrons", job.getName());

      UserdataimportCollection userImportCollection = new UserdataimportCollection();

      List<Userdataimport> users = new ArrayList<>();

      try (
        Statement statement = threadConnections.getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, partitionContext);
      ) {

        while (resultSet.next()) {
          String username = resultSet.getString(USERNAME);
          String externalSystemId = resultSet.getString(EXTERNALSYSTEMID);
          String barcode = resultSet.getString(BARCODE);
          Boolean active = resultSet.getBoolean(ACTIVE);
          String patronGroup = resultSet.getString(PATRONGROUP);
          String personal_lastName = resultSet.getString(PERSONAL_LASTNAME);
          String personal_firstName = resultSet.getString(PERSONAL_FIRSTNAME);
          String personal_middleName = resultSet.getString(PERSONAL_MIDDLENAME);
          String personal_email = resultSet.getString(PERSONAL_EMAIL);
          String personal_phone = resultSet.getString(PERSONAL_PHONE);
          String addresses_permanent_addressTypeId = resultSet.getString(ADDRESSES_PERMANENT_ADDRESSTYPEID);
          String addresses_permanent_countryId = resultSet.getString(ADDRESSES_PERMANENT_COUNTRYID);
          String addresses_permanent_addressLine1 = resultSet.getString(ADDRESSES_PERMANENT_ADDRESSLINE1);
          String addresses_permanent_addressLine2 = resultSet.getString(ADDRESSES_PERMANENT_ADDRESSLINE2);
          String addresses_permanent_city = resultSet.getString(ADDRESSES_PERMANENT_CITY);
          String addresses_permanent_region = resultSet.getString(ADDRESSES_PERMANENT_REGION);
          String addresses_permanent_postalCode = resultSet.getString(ADDRESSES_PERMANENT_POSTALCODE);
          String addresses_temporary_addressTypeId = resultSet.getString(ADDRESSES_TEMPORARY_ADDRESSTYPEID);
          String addresses_temporary_addressLine1 = resultSet.getString(ADDRESSES_TEMPORARY_ADDRESSLINE1);
          String addresses_temporary_addressLine2 = resultSet.getString(ADDRESSES_TEMPORARY_ADDRESSLINE2);
          String addresses_temporary_city = resultSet.getString(ADDRESSES_TEMPORARY_CITY);
          String addresses_temporary_region = resultSet.getString(ADDRESSES_TEMPORARY_REGION);
          String addresses_temporary_postalCode = resultSet.getString(ADDRESSES_TEMPORARY_POSTALCODE);
          String departments_0 = resultSet.getString(DEPARTMENTS_0);
          String expirationDate = resultSet.getString(EXPIRATIONDATE);

          Userdataimport userImport = new Userdataimport();
          userImport.setUsername(username);

          if (StringUtils.isEmpty(barcode)) {
            log.warn("{} patron {} does not have a barcode, using external system id {}", job.getName(), username, externalSystemId);
            userImport.setBarcode(externalSystemId);
          } else {
            userImport.setBarcode(barcode);
          }

          userImport.setExternalSystemId(externalSystemId);
          userImport.setBarcode(barcode);
          userImport.setActive(active);
          userImport.setPatronGroup(patronGroup);

          Personal personal = new Personal();

          personal.setLastName(personal_lastName);
          personal.setFirstName(personal_firstName);
          personal.setMiddleName(personal_middleName);
          personal.setEmail(personal_email);
          personal.setPhone(FormatUtility.normalizePhoneNumber(personal_phone));

          if (StringUtils.isNotEmpty(addresses_permanent_addressLine1)) {
            Address permanentAddress = new Address();

            permanentAddress.setAddressTypeId(addresses_permanent_addressTypeId);
            permanentAddress.setCountryId(addresses_permanent_countryId);
            permanentAddress.setAddressLine1(addresses_permanent_addressLine1);
            permanentAddress.setAddressLine2(addresses_permanent_addressLine2);
            permanentAddress.setCity(addresses_permanent_city);
            permanentAddress.setRegion(addresses_permanent_region);
            permanentAddress.setPostalCode(FormatUtility.normalizePostalCode(addresses_permanent_postalCode));

            personal.getAddresses().add(permanentAddress);
          }

          if (StringUtils.isNotEmpty(addresses_temporary_addressLine1)) {
            Address temporaryAddress = new Address();

            temporaryAddress.setAddressTypeId(addresses_temporary_addressTypeId);
            temporaryAddress.setAddressLine1(addresses_temporary_addressLine1);
            temporaryAddress.setAddressLine2(addresses_temporary_addressLine2);
            temporaryAddress.setCity(addresses_temporary_city);
            temporaryAddress.setRegion(addresses_temporary_region);
            temporaryAddress.setPostalCode(FormatUtility.normalizePostalCode(addresses_temporary_postalCode));

            personal.getAddresses().add(temporaryAddress);
          }

          userImport.setPersonal(personal);

          if (StringUtils.isNotEmpty(departments_0)) {
            // userImport.getDepartments().add(departments_0);
          }

          try {
            userImport.setExpirationDate(DateUtils.parseDate(expirationDate, EXPIRATION_DATE_FORMAT));
          } catch (ParseException e) {
            log.error("failed to parse expiration date {}", expirationDate);
          }

          users.add(userImport);
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      if (users.isEmpty()) {
        log.info("{} has no patrons at this time", job.getName());
        return this;
      }

      userImportCollection.setUsers(users);
      userImportCollection.setTotalRecords(users.size());

      userImportCollection.setDeactivateMissingUsers(false);
      userImportCollection.setUpdateOnlyPresentFields(true);

      log.info("submitting user import with {} users", userImportCollection.getTotalRecords());

      try {
        ImportResponse importResponse = migrationService.okapiService.postUserdataimportCollection(tenant, token, userImportCollection);

        if (StringUtils.isNotEmpty(importResponse.getError())) {
          log.error("{}", importResponse.getError());
        } else {
          log.info("{}: {}", job.getName(), importResponse.getMessage());
  
          log.info("{} total records", importResponse.getTotalRecords());
  
          log.info("{} newly created users", importResponse.getCreatedRecords());
  
          log.info("{} updated users", importResponse.getUpdatedRecords());
  
          log.info("{} failed records", importResponse.getFailedRecords());
  
          if (importResponse.getFailedRecords() > 0) {
            importResponse.getFailedUsers().forEach(failedUser -> {
              log.info("{} {} {}", failedUser.getUsername(), failedUser.getExternalSystemId(), failedUser.getErrorMessage());
            });
          }
        }

      } catch (Exception e) {
        log.error("failed to import users: {}", e.getMessage());
      }

      log.info("{} finished in {} milliseconds", job.getName(), TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((DivITPatronPartitionTask) obj).getIndex() == this.getIndex();
    }
  }

  private ThreadConnections getThreadConnections(Database settings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setConnection(getConnection(settings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection connection;

    public ThreadConnections() {

    }

    public Connection getConnection() {
      return connection;
    }

    public void setConnection(Connection connection) {
      this.connection = connection;
    }

    public void closeAll() {
      try {
        connection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
