package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.CheckOutByBarcodeRequest;
import org.folio.rest.jaxrs.model.Loan;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Locations;
import org.folio.rest.jaxrs.model.Servicepoint;
import org.folio.rest.jaxrs.model.Servicepoints;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.loan.LoanContext;
import org.folio.rest.migration.model.request.loan.LoanJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.springframework.cache.annotation.Cacheable;

public class LoanMigration extends AbstractMigration<LoanContext> {

  private static final String LOCATIONS_CODE_MAP = "LOCATIONS_CODE_MAP";
  private static final String SERVICE_POINTS = "SERVICE_POINTS";

  private static final String CIRC_TRANSACTION_ID = "CIRC_TRANSACTION_ID";
  private static final String CHARGE_LOCATION = "CHARGE_LOCATION";
  private static final String RENEWAL_COUNT = "RENEWAL_COUNT";

  private static final String PATRON_ID = "PATRON_ID";
  private static final String PATRON_BARCODE = "PATRON_BARCODE";

  private static final String ITEM_ID = "ITEM_ID";
  private static final String ITEM_BARCODE = "ITEM_BARCODE";

  private static final String LOAN_DATE = "LOAN_DATE";
  private static final String DUE_DATE = "DUE_DATE";

  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";

  private LoanMigration(LoanContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    String token = migrationService.okapiService.getToken(tenant);

    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);
    Servicepoints servicePoints = migrationService.okapiService.fetchServicepoints(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<LoanContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (LoanJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      Map<Integer, String> locationsCodeMap = getLocationsCodeMap(locations, job.getSchema());

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
        partitionContext.put(JOB, job);
        partitionContext.put(TOKEN, token);
        partitionContext.put(LOCATIONS_CODE_MAP, locationsCodeMap);
        partitionContext.put(SERVICE_POINTS, servicePoints);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new LoanPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static LoanMigration with(LoanContext context, String tenant) {
    return new LoanMigration(context, tenant);
  }

  public class LoanPartitionTask implements PartitionTask<LoanContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public LoanPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public LoanPartitionTask execute(LoanContext context) {
      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);

      LoanJob job = (LoanJob) partitionContext.get(JOB);

      Map<Integer, String> locationsCodeMap = (Map<Integer, String>) partitionContext.get(LOCATIONS_CODE_MAP);

      Servicepoints servicePoints = (Servicepoints) partitionContext.get(SERVICE_POINTS);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          Integer circTransactionId = pageResultSet.getInt(CIRC_TRANSACTION_ID);
          Integer chargeLocation = pageResultSet.getInt(CHARGE_LOCATION);
          Integer renewalCount = pageResultSet.getInt(RENEWAL_COUNT);

          Integer patronId = pageResultSet.getInt(PATRON_ID);
          String patronBarcode = pageResultSet.getString(PATRON_BARCODE);

          Integer itemId = pageResultSet.getInt(ITEM_ID);
          String itemBarcode = pageResultSet.getString(ITEM_BARCODE);

          String loanDate = pageResultSet.getString(LOAN_DATE);
          String dueDate = pageResultSet.getString(DUE_DATE);

          if (Objects.isNull(patronBarcode)) {
            log.debug("{} no patron barcode found for patron id {}", schema, patronId);
            continue;
          }

          if (Objects.isNull(itemBarcode)) {
            log.info("{} no item barcode found for item id {}", schema, itemId);
            continue;
          }

          String locationCode = locationsCodeMap.get(chargeLocation);

          Optional<String> servicePointId = getServicePoint(locationCode, servicePoints);

          if (!servicePointId.isPresent()) {
            log.info("{} could not find service point for item id {} with charge location {}", schema, itemId, chargeLocation);
            continue;
          }

          CheckOutByBarcodeRequest checkoutRequest = new CheckOutByBarcodeRequest();
          checkoutRequest.setItemBarcode(itemBarcode.toLowerCase());
          checkoutRequest.setUserBarcode(patronBarcode.toLowerCase());
          checkoutRequest.setServicePointId(servicePointId.get());

          try {
            Loan loan = migrationService.okapiService.checkoutByBarcode(checkoutRequest, tenant, token);

          } catch (Exception e) {
            log.error("{} failed to checkout item with barcode {} to user with barcode {} at service point {}", schema, itemBarcode, patronBarcode, servicePointId.get());
            log.error(e.getMessage());
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
      return Objects.nonNull(obj) && ((LoanPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private Map<Integer, String> getLocationsCodeMap(Locations locations, String schema) {
    Map<Integer, String> idToCode = new HashMap<>();
    Map<String, Object> locationContext = new HashMap<>();
    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<Integer, String> locConv = context.getMaps().getLocation().get(schema);
    try (Connection voyagerConnection = getConnection(voyagerSettings);
        Statement st = voyagerConnection.createStatement();
        ResultSet rs = getResultSet(st, locationContext);) {
      while (rs.next()) {
        Integer id = rs.getInt(LOCATION_ID);
        if (Objects.nonNull(id)) {
          String code = locConv.containsKey(id) ? locConv.get(id) : rs.getString(LOCATION_CODE);
          Optional<Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code))
              .findFirst();
          if (location.isPresent()) {
            idToCode.put(id, code);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToCode;
  }

  @Cacheable(value = "servicePoints", key = "code", sync = true)
  private Optional<String> getServicePoint(String code, Servicepoints servicePoints) {
    Optional<Servicepoint> servicePoint = servicePoints.getServicepoints().stream()
        .filter(sp -> sp.getCode().equals(code)).findAny();
    if (servicePoint.isPresent()) {
      return Optional.of(servicePoint.get().getId());
    }
    return Optional.empty();
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
