package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronContext;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronJob;
import org.folio.rest.migration.service.MigrationService;

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

  private DivITPatronMigration(DivITPatronContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

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

      String token = (String) partitionContext.get(TENANT);

      log.info("processing {} patrons", job.getName());

      try (
        Statement statement = threadConnections.getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, partitionContext);
      ) {

        while (resultSet.next()) {
          String username = resultSet.getString(USERNAME);
          String externalSystemId = resultSet.getString(EXTERNALSYSTEMID);
          String barcode = resultSet.getString(BARCODE);
          String active = resultSet.getString(ACTIVE);
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

          System.out.println("\tusername: " + username);
          System.out.println("\texternalSystemId: " + externalSystemId);
          System.out.println("\tbarcode: " + barcode);
          System.out.println("\tactive: " + active);
          System.out.println("\tpatronGroup: " + patronGroup);
          System.out.println("\tpersonal_lastName: " + personal_lastName);
          System.out.println("\tpersonal_firstName: " + personal_firstName);
          System.out.println("\tpersonal_middleName: " + personal_middleName);
          System.out.println("\tpersonal_email: " + personal_email);
          System.out.println("\tpersonal_phone: " + personal_phone);
          System.out.println("\taddresses_permanent_addressTypeId: " + addresses_permanent_addressTypeId);
          System.out.println("\taddresses_permanent_countryId: " + addresses_permanent_countryId);
          System.out.println("\taddresses_permanent_addressLine1: " + addresses_permanent_addressLine1);
          System.out.println("\taddresses_permanent_addressLine2: " + addresses_permanent_addressLine2);
          System.out.println("\taddresses_permanent_city: " + addresses_permanent_city);
          System.out.println("\taddresses_permanent_region: " + addresses_permanent_region);
          System.out.println("\taddresses_permanent_postalCode: " + addresses_permanent_postalCode);
          System.out.println("\taddresses_temporary_addressTypeId: " + addresses_temporary_addressTypeId);
          System.out.println("\taddresses_temporary_addressLine2: " + addresses_temporary_addressLine2);
          System.out.println("\taddresses_temporary_addressLine1: " + addresses_temporary_addressLine1);
          System.out.println("\taddresses_temporary_city: " + addresses_temporary_city);
          System.out.println("\taddresses_temporary_region: " + addresses_temporary_region);
          System.out.println("\taddresses_temporary_postalCode: " + addresses_temporary_postalCode);
          System.out.println("\tdepartments_0: " + departments_0);
          System.out.println("\texpirationDate: " + expirationDate);
          System.out.println("*************************************************************");
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

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
