package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.inventory.Location;
import org.folio.rest.jaxrs.model.inventory.Locations;
import org.folio.rest.jaxrs.model.inventory.Servicepoint;
import org.folio.rest.jaxrs.model.inventory.Servicepoints;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.request.RequestContext;
import org.folio.rest.migration.model.request.request.RequestJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;

public class RequestMigration extends AbstractMigration<RequestContext> {

  private static final String LOCATIONS_CODE_MAP = "LOCATIONS_CODE_MAP";
  private static final String SERVICE_POINTS = "SERVICE_POINTS";

  private static final String REQUESTTYPE = "REQUESTTYPE";
  private static final String REQUESTDATE = "REQUESTDATE";
  private static final String STATUS = "STATUS";
  private static final String POSITION = "POSITION";
  private static final String ITEM_ID = "ITEM_ID";
  private static final String ITEM_TITLE = "ITEM_TITLE";
  private static final String ITEM_BARCODE = "ITEM_BARCODE";
  private static final String REQUESTER_EXTERNAL_SYSTEM_ID = "REQUESTER_EXTERNAL_SYSTEM_ID";
  private static final String REQUESTER_LASTNAME = "REQUESTER_LASTNAME";
  private static final String REQUESTER_FIRSTNAME = "REQUESTER_FIRSTNAME";
  private static final String FULFILMENTPREFERENCE = "FULFILMENTPREFERENCE";
  private static final String REQUESTEXPIRATIONDATE = "REQUESTEXPIRATIONDATE";
  private static final String HOLDSHELFEXPIRATIONDATE = "HOLDSHELFEXPIRATIONDATE";
  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";

  private static final String ITEM_REFERENCE_ID = "itemTypeId";

  private RequestMigration(RequestContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);

    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);
    Servicepoints servicePoints = migrationService.okapiService.fetchServicepoints(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<RequestContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (RequestJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      Map<String, String> locationsCodeMap = getLocationsCodeMap(locations, job.getSchema());

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
        taskQueue.submit(new RequestPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static RequestMigration with(RequestContext context, String tenant) {
    return new RequestMigration(context, tenant);
  }

  public class RequestPartitionTask implements PartitionTask<RequestContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    public RequestPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public RequestPartitionTask execute(RequestContext context) {
      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);

      RequestJob job = (RequestJob) partitionContext.get(JOB);

      Map<String, String> locationsCodeMap = (Map<String, String>) partitionContext.get(LOCATIONS_CODE_MAP);

      Servicepoints servicePoints = (Servicepoints) partitionContext.get(SERVICE_POINTS);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      String itemRLTypeId = job.getReferences().get(ITEM_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          String requestType = pageResultSet.getString(REQUESTTYPE);
          String requestDate = pageResultSet.getString(REQUESTDATE);
          String status = pageResultSet.getString(STATUS);
          Integer position = pageResultSet.getInt(POSITION);
          String itemId = pageResultSet.getString(ITEM_ID);
          String itemTitle = pageResultSet.getString(ITEM_TITLE);
          String itemBarcode = pageResultSet.getString(ITEM_BARCODE);
          String requesterExternalSystemId = pageResultSet.getString(REQUESTER_EXTERNAL_SYSTEM_ID);
          String requesterLastname = pageResultSet.getString(REQUESTER_LASTNAME);
          String requesterFirstname = pageResultSet.getString(REQUESTER_FIRSTNAME);
          String fulfilmentPreference = pageResultSet.getString(FULFILMENTPREFERENCE);
          String requestExpirationDate = pageResultSet.getString(REQUESTEXPIRATIONDATE);
          String holdshelfExpirationDate = pageResultSet.getString(HOLDSHELFEXPIRATIONDATE);
          String locationId = pageResultSet.getString(LOCATION_ID);

          List<ReferenceLink> userReferenceLinks = migrationService.referenceLinkRepo.findAllByExternalReference(requesterExternalSystemId);

          if (userReferenceLinks.isEmpty()) {
            log.error("{} no user id found for with external id {}", schema, requesterExternalSystemId);
            continue;
          }

          String userReferenceId = userReferenceLinks.get(0).getFolioReference().toString();

          Optional<ReferenceLink> itemRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(itemRLTypeId, itemId);

          if (!itemRL.isPresent()) {
            log.error("{} no item id found for item id {}", schema, itemId);
            continue;
          }

          String locationCode = locationsCodeMap.get(locationId);

          Optional<String> servicePointId = getServicePoint(locationCode, servicePoints);

          if (!servicePointId.isPresent()) {
            log.info("{} could not find service point for item id {} with location id {} and code", schema, itemId, locationId, locationCode);
            continue;
          }

          ObjectNode request = migrationService.objectMapper.createObjectNode();

          request.put("id", UUID.randomUUID().toString());

          request.put("requesterId", userReferenceId);
          request.put("requestType", requestType);
          request.put("requestDate", requestDate);
          request.put("status", status);
          request.put("position", position);
          request.put("itemId", itemRL.get().getFolioReference());

          ObjectNode item = request.with("item");

          item.put("title", itemTitle);
          item.put("barcode", itemBarcode);

          ObjectNode requester = request.with("requester");

          requester.put("firstName", requesterFirstname);
          requester.put("lastName", requesterLastname);

          request.put("fulfilmentPreference", fulfilmentPreference);

          request.put("pickupServicePointId", servicePointId.get());


          if (StringUtils.isNoneEmpty(holdshelfExpirationDate)) {
            request.put("holdShelfExpirationDate", holdshelfExpirationDate);
          }

          if (StringUtils.isNoneEmpty(requestExpirationDate)) {
            request.put("requestExpirationDate", requestExpirationDate);
          }

          try {
            migrationService.okapiService.createRequest(request, tenant, token);
          } catch(Exception e) {
            log.error("{} failed creating request for user external id {} and item id {} \n {} \n {}", schema, requesterExternalSystemId, itemId, request, e.getMessage());
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
      return Objects.nonNull(obj) && ((RequestPartitionTask) obj).getIndex() == this.getIndex();
    }

  }

  private Map<String, String> getLocationsCodeMap(Locations locations, String schema) {
    Map<String, String> idToCode = new HashMap<>();
    Map<String, Object> locationContext = new HashMap<>();
    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<String, String> locConv = context.getMaps().getLocation().get(schema);
    try (
      Connection voyagerConnection = getConnection(voyagerSettings);
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, locationContext);
    ) {
      while (rs.next()) {
        String id = rs.getString(LOCATION_ID);
        if (Objects.nonNull(id)) {
          String code = locConv.containsKey(id) ? locConv.get(id) : rs.getString(LOCATION_CODE);
          Optional<Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code)).findFirst();
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

  private Optional<String> getServicePoint(String code, Servicepoints servicePoints) {
    final String folioLocationCode = context.getMaps().getLocationCode().containsKey(code)
        ? context.getMaps().getLocationCode().get(code)
        : code;
    Optional<Servicepoint> servicePoint = servicePoints.getServicepoints().stream()
        .filter(sp -> sp.getCode().equals(folioLocationCode)).findAny();
    if (servicePoint.isPresent()) {
      return Optional.of(servicePoint.get().getId());
    }
    return Optional.empty();
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    return threadConnections;
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
