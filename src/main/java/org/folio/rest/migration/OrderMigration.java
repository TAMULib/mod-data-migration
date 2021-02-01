package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.finance.acq_models.mod_finance.schemas.FundCollection;
import org.folio.rest.jaxrs.model.inventory.Location;
import org.folio.rest.jaxrs.model.inventory.Locations;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.order.OrderContext;
import org.folio.rest.migration.model.request.order.OrderDefaults;
import org.folio.rest.migration.model.request.order.OrderJob;
import org.folio.rest.migration.model.request.order.OrderMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;

public class OrderMigration extends AbstractMigration<OrderContext> {

  private static final String COLUMNS = "COLUMNS";
  private static final String TABLES = "TABLES";
  private static final String CONDITIONS = "CONDITIONS";

  private static final String LOCATIONS_MAP = "LOCATIONS_MAP";
  private static final String FUNDS_MAP = "FUNDS_MAP";

  private static final String PO_ID = "PO_ID";
  private static final String PO_NUMBER = "PO_NUMBER";
  private static final String PO_STATUS = "PO_STATUS";
  private static final String VENDOR_ID = "VENDOR_ID";
  private static final String SHIPLOC = "SHIPLOC";
  private static final String BILLLOC = "BILLLOC";

  private static final String NOTE = "NOTE";

  private static final String BIB_ID = "BIB_ID";
  private static final String LINE_ITEM_ID = "LINE_ITEM_ID";
  private static final String LINE_PRICE = "LINE_PRICE";
  private static final String PO_TYPE = "PO_TYPE";
  private static final String LOCATION_CODE = "LOCATION_CODE";
  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String TITLE = "TITLE";
  private static final String LINE_ITEM_STATUS = "LINE_ITEM_STATUS";
  private static final String REQUESTER = "REQUESTER";
  private static final String VENDOR_TITLE_NUM = "VENDOR_TITLE_NUM";
  private static final String VENDOR_REF_QUAL = "VENDOR_REF_QUAL";
  private static final String VENDOR_REF_NUM = "VENDOR_REF_NUM";
  private static final String ACCOUNT_NAME = "ACCOUNT_NAME";
  private static final String FUND_CODE = "FUND_CODE";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";

  private OrderMigration(OrderContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<String> run(MigrationService migrationService) {
    log.info("running {} for tenant {}", this.getClass().getSimpleName(), tenant);

    String token = migrationService.okapiService.getToken(tenant);

    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);

    Map<String, String> fundsMap = migrationService.okapiService.fetchFunds(tenant, token)
      .getFunds().stream().collect(Collectors.toMap(fund -> fund.getCode(), fund -> fund.getId()));

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<OrderContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (OrderJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());
      countContext.put(COLUMNS, job.getPageAdditionalContext().get(COLUMNS));
      countContext.put(TABLES, job.getPageAdditionalContext().get(TABLES));
      countContext.put(CONDITIONS, job.getPageAdditionalContext().get(CONDITIONS));

      Map<String, String> locationsMap = getLocationsMap(locations, job.getSchema());

      int count = getCount(voyagerSettings, countContext);

      log.info("{} count: {}", job.getSchema(), count);

      int partitions = job.getPartitions();
      int limit = (int) Math.ceil((double) count / (double) partitions);
      int offset = 0;
      for (int i = 0; i < partitions; i++) {
        Map<String, Object> partitionContext = new HashMap<String, Object>();
        partitionContext.put(SQL, context.getExtraction().getPageSql());
        partitionContext.put(SCHEMA, job.getSchema());
        partitionContext.put(COLUMNS, job.getPageAdditionalContext().get(COLUMNS));
        partitionContext.put(TABLES, job.getPageAdditionalContext().get(TABLES));
        partitionContext.put(CONDITIONS, job.getPageAdditionalContext().get(CONDITIONS));
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        partitionContext.put(JOB, job);
        partitionContext.put(MAPS, context.getMaps());
        partitionContext.put(DEFAULTS, context.getDefaults());
        partitionContext.put(LOCATIONS_MAP, locationsMap);
        partitionContext.put(FUNDS_MAP, fundsMap);
        partitionContext.put(TOKEN, token);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new OrderPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
      }
    }

    return CompletableFuture.completedFuture(IN_PROGRESS_RESPONSE_MESSAGE);
  }

  public static OrderMigration with(OrderContext context, String tenant) {
    return new OrderMigration(context, tenant);
  }

  public class OrderPartitionTask implements PartitionTask<OrderContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private final ExecutorService additionalExecutor;

    public OrderPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.additionalExecutor = Executors.newFixedThreadPool(1);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    public OrderPartitionTask execute(OrderContext context) {
      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);

      OrderJob job = (OrderJob) partitionContext.get(JOB);

      OrderMaps maps = (OrderMaps) partitionContext.get(MAPS);

      OrderDefaults defaults = (OrderDefaults) partitionContext.get(DEFAULTS);

      Map<String, String> locationsMap = (Map<String, String>) partitionContext.get(LOCATIONS_MAP);
      Map<String, String> fundsMap = (Map<String, String>) partitionContext.get(FUNDS_MAP);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Map<String, Object> lineItemNoteContext = new HashMap<>();
      lineItemNoteContext.put(SQL, context.getExtraction().getLineItemNotesSql());
      lineItemNoteContext.put(SCHEMA, schema);

      Map<String, Object> poLinesContext = new HashMap<>();
      poLinesContext.put(SQL, context.getExtraction().getPoLinesSql());
      poLinesContext.put(SCHEMA, schema);
      poLinesContext.put(COLUMNS, job.getPoLinesAdditionalContext().get(COLUMNS));
      poLinesContext.put(TABLES, job.getPoLinesAdditionalContext().get(TABLES));
      poLinesContext.put(CONDITIONS, job.getPoLinesAdditionalContext().get(CONDITIONS));

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement lineItemNoteStatement = threadConnections.getLineItemNoteConnection().createStatement();
        Statement poLinesStatement = threadConnections.getPurchaseOrderLinesConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          String poId = pageResultSet.getString(PO_ID);
          String poNumber = pageResultSet.getString(PO_NUMBER);
          String poStatus = pageResultSet.getString(PO_STATUS);
          String vendorId = pageResultSet.getString(VENDOR_ID);
          String shipLoc = pageResultSet.getString(SHIPLOC);
          String billLoc = pageResultSet.getString(BILLLOC);

          lineItemNoteContext.put(PO_ID, poId);
          poLinesContext.put(PO_ID, poId);

          System.out.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s", index, schema, poId, poNumber, poStatus, vendorId, shipLoc, billLoc));

          ObjectNode po = migrationService.objectMapper.createObjectNode();

          po.put("id", UUID.randomUUID().toString());

          po.put("approved", false);
          po.put("workflowStatus", "Pending");
          po.put("manualPo", false);

          if (StringUtils.isNotEmpty(poNumber)) {
            po.put("poNumber", StringUtils.deleteWhitespace(String.format("%s%s", job.getPoNumberPrefix(), poNumber)));
          }

          if (job.getIncludeAddresses()) {
            String billToKey = StringUtils.isNotEmpty(billLoc) ? billLoc : defaults.getAqcAddressCode();
            String shipToKey = StringUtils.isNotEmpty(shipLoc) ? shipLoc : defaults.getAqcAddressCode();
            po.put("billTo", maps.getAcqAddresses().get(billToKey));
            po.put("shipTo", maps.getAcqAddresses().get(shipToKey));
          }

          po.put("orderType", "Ongoing");

          Optional<ReferenceLink> vendorRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(vendorRLTypeId, vendorId);
          if (!vendorRL.isPresent()) {
            log.error("{} no vendor id found for vendor id {}", schema, vendorId);
            continue;
          }

          String vendorReferenceId = vendorRL.get().getFolioReference();

          po.put("vendor", vendorReferenceId);

          ObjectNode ongoingObject = migrationService.objectMapper.createObjectNode();

          ongoingObject.put("interval", 365);
          ongoingObject.put("isSubscription", true);
          ongoingObject.put("manualRenewal", true);

          po.set("ongoing", ongoingObject);

          CompletableFuture.allOf(
            getLineItemNotes(lineItemNoteStatement, lineItemNoteContext)
              .thenAccept((notes) -> po.set("notes", notes)),
            getPurchaseOrderLines(poLinesStatement, poLinesContext, job, maps, defaults, locationsMap, fundsMap, vendorReferenceId)
              .thenAccept((notes) -> po.set("compositePoLines", notes))
          ).get();

          System.out.println(po);

        }
      } catch (SQLException | InterruptedException | ExecutionException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }
      log.info("{} {} finished in {} milliseconds", schema, index, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return Objects.nonNull(obj) && ((OrderPartitionTask) obj).getIndex() == this.getIndex();
    }

    private CompletableFuture<ArrayNode> getLineItemNotes(Statement statement, Map<String, Object> context) {
      CompletableFuture<ArrayNode> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        ArrayNode notes =  migrationService.objectMapper.createArrayNode();
        try (ResultSet resultSet = getResultSet(statement, context)) {
          while (resultSet.next()) {
            String note = resultSet.getString(NOTE);
            if (StringUtils.isNotEmpty(note)) {
              notes.add(note);
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(notes);
        }
      });
      return future;
    }

    private CompletableFuture<ArrayNode> getPurchaseOrderLines(Statement statement, Map<String, Object> context, OrderJob job, OrderMaps maps, OrderDefaults defaults, Map<String, String> locationsMap, Map<String, String> fundsMap, String vendorId) {
      CompletableFuture<ArrayNode> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {

        String poId = (String) context.get(PO_ID);
        String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);

        Map<String, String> expenseClasses = maps.getExpenseClasses().get(job.getSchema());
        Map<String, String> fundCodes = maps.getFundCodes().get(job.getSchema());

        ArrayNode poLines =  migrationService.objectMapper.createArrayNode();
        try (ResultSet resultSet = getResultSet(statement, context)) {
          while (resultSet.next()) {
            String bibId = resultSet.getString(BIB_ID);
            String lineItemId = resultSet.getString(LINE_ITEM_ID);
            String linePrice = resultSet.getString(LINE_PRICE);
            String poType = resultSet.getString(PO_TYPE);
            String locationCode = resultSet.getString(LOCATION_CODE);
            String locationId = resultSet.getString(LOCATION_ID);
            String title = resultSet.getString(TITLE);
            String lineItemStatus = resultSet.getString(LINE_ITEM_STATUS);
            String requester = resultSet.getString(REQUESTER);
            String vendorTitleNumber = resultSet.getString(VENDOR_TITLE_NUM);
            String vendorRefQual = resultSet.getString(VENDOR_REF_QUAL);
            String vendorRefNumber = resultSet.getString(VENDOR_REF_NUM);
            String accountName = resultSet.getString(ACCOUNT_NAME);
            String fundCode = resultSet.getString(FUND_CODE);
            String note = resultSet.getString(NOTE);

            Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, bibId);
            if (!instanceRL.isPresent()) {
              log.error("{} no instance id found for bib id {}", job.getSchema(), bibId);
              continue;
            }

            ObjectNode compositePurchaseOrderLineObject = migrationService.objectMapper.createObjectNode();
            ObjectNode costObject = migrationService.objectMapper.createObjectNode();
            ObjectNode locationObject = migrationService.objectMapper.createObjectNode();
            String folioLocationId;

            compositePurchaseOrderLineObject.put("id", UUID.randomUUID().toString());
            compositePurchaseOrderLineObject.put("instanceId", instanceRL.get().getFolioReference());
            compositePurchaseOrderLineObject.put("titleOrPackage", title);
            compositePurchaseOrderLineObject.put("source", "User");
            compositePurchaseOrderLineObject.put("acquisitionMethod", maps.getPoLineAcqMethods().get(poType));

            if (StringUtils.isNotEmpty(locationId)) {
              if (locationsMap.containsKey(locationId)) {
                folioLocationId = locationsMap.get(locationId);
              } else {
                log.error("{} mapped location id {} not found for po id {}", job.getSchema(), locationId, poId);
                continue;
              }
            } else {
              folioLocationId = job.getDefaultLocationId();
            }

            if (StringUtils.isEmpty(folioLocationId)) {
              log.error("{} order location id {} not found for po id {}", job.getSchema(), locationId, poId);
              continue;
            }

            int cost = 0;
            costObject.put("currency", "USD");
            if (StringUtils.isNotEmpty(locationCode) && !locationCode.startsWith("www")) {
              compositePurchaseOrderLineObject.put("orderFormat", "Physical Resource");
              compositePurchaseOrderLineObject.with("physical").put("createInventory", "None");
              costObject.put("listUnitPrice", cost);
              costObject.put("quantityPhysical", 1);
              locationObject.put("locationId", folioLocationId);
              locationObject.put("quantityPhysical", 1);
            } else {
              ObjectNode eResourceObject = compositePurchaseOrderLineObject.with("eresource");
              eResourceObject.put("createInventory", "None");
              eResourceObject.put("accessProvider", vendorId);
              compositePurchaseOrderLineObject.put("orderFormat", "Electronic Resource");
              costObject.put("listUnitPriceElectronic", cost);
              costObject.put("quantityElectronic", 1);
              locationObject.put("locationId", folioLocationId);
              locationObject.put("quantityElectronic", 1);
            }

            compositePurchaseOrderLineObject.set("cost", costObject);
            compositePurchaseOrderLineObject.withArray("locations")
              .add(locationObject);

            if (StringUtils.isNotEmpty(fundCode)) {

              ObjectNode fundDistributionObject = migrationService.objectMapper.createObjectNode();
              fundDistributionObject.put("distributionType", "percentage");
              fundDistributionObject.put("value", 100);

              // NOTE: conditioning on schema :(
              if (job.getSchema().equals("AMDB")) {

                if (fundCode.startsWith("msv")) {
                  fundCode = fundCode.substring(3);
                }

                switch (fundCode) {
                  case "seri": fundCode = "serials"; break;
                  case "serial": fundCode = "serials"; break;
                  case "qatar": fundCode = "etxtqatar"; break;
                  case "btetext": fundCode = "etxt"; break;
                  case "btetxt": fundCode = "etxt"; break;
                  case "e-72997": fundCode = "barclay"; break;
                  case "chargeback":
                  case "access":
                    fundDistributionObject.put("expenseClassId", expenseClasses.get(fundCode));
                    fundCode = "etxt";
                    break;
                  case "costshare":
                   // delete po ref by po id
                  break;
                  default:
                  break;
                }

                if (fundsMap.containsKey(fundCode)) {
                  fundDistributionObject.put("fundId", fundsMap.get(fundCode));
                } else {
                  log.error("{} fund code {} not found for po {}", job.getSchema(), fundCode, poId);
                }

              } else if (job.getSchema().equals("MSDB")) {

                String fundCodePrefix = fundCode.substring(0, 2);
                if (fundCodes.containsKey(fundCodePrefix)) {
                  String mappedFunCode = fundCodes.get(fundCodePrefix);
                  if (fundsMap.containsKey(mappedFunCode)) {
                    fundDistributionObject.put("fundId", fundsMap.get(mappedFunCode));
                  } else {
                    log.error("{} fund code {} not found for po {}", job.getSchema(), mappedFunCode, poId);
                  }
                } else {
                  log.error("{} fund code {} as {} not mapped", job.getSchema(), fundCode, fundCodePrefix);
                }

                if (expenseClasses.containsKey(fundCode)) {
                  fundDistributionObject.put("expenseClassId", expenseClasses.get(fundCode));
                } else {
                  log.error("{} expense class not mapped from {}", job.getSchema(), fundCode);
                }

              }

              compositePurchaseOrderLineObject.withArray("fundDistribution")
                .add(fundDistributionObject);

            } else {
              log.error("{} no fund code for po {}", job.getSchema(), poId);
            }

            // NOTE: conditioning on schema again :(
            if (job.getSchema().equals("AMDB")) {
              ObjectNode vendorDetailsObject = migrationService.objectMapper.createObjectNode();
              if (StringUtils.isNotEmpty(accountName)) {
                vendorDetailsObject.put("vendorAccount", accountName);
              }
              vendorDetailsObject.put("instructions", StringUtils.SPACE);

              if (StringUtils.isNotEmpty(vendorRefNumber)) {
                vendorDetailsObject.put("refNumberType", maps.getVendorRefQual().get(vendorRefQual));
                vendorDetailsObject.put("refNumber", vendorRefNumber);
              } else if (StringUtils.isNotEmpty(vendorTitleNumber)) {
                vendorDetailsObject.put("refNumberType", maps.getVendorRefQual().get("VN"));
                vendorDetailsObject.put("refNumber", vendorTitleNumber);
              }

              compositePurchaseOrderLineObject.set("vendorDetail", vendorDetailsObject);
            }

            // System.out.println(
            //   String.format("\t%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            //     bibId,
            //     lineItemId,
            //     linePrice,
            //     locationCode,
            //     locationId,
            //     title,
            //     lineItemStatus,
            //     requester,
            //     vendorTitleNumber,
            //     vendorRefQual,
            //     vendorRefNumber,
            //     accountName,
            //     fundCode,
            //     note
            //   )
            // );

          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          future.complete(poLines);
        }
      });
      return future;
    }

  }

  private Map<String, String> getLocationsMap(Locations locations, String schema) {
    Map<String, String> idToUuid = new HashMap<>();
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
            idToUuid.put(id, location.get().getId());
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToUuid;
  }

  private ThreadConnections getThreadConnections(Database voyagerSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setLineItemNoteConnection(getConnection(voyagerSettings));
    threadConnections.setPurchaseOrderLinesConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private Connection lineItemNoteConnection;

    private Connection purchaseOrderLinesConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getLineItemNoteConnection() {
      return lineItemNoteConnection;
    }

    public void setLineItemNoteConnection(Connection lineItemNoteConnection) {
      this.lineItemNoteConnection = lineItemNoteConnection;
    }

    public Connection getPurchaseOrderLinesConnection() {
      return purchaseOrderLinesConnection;
    }

    public void setPurchaseOrderLinesConnection(Connection purchaseOrderLinesConnection) {
      this.purchaseOrderLinesConnection = purchaseOrderLinesConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        lineItemNoteConnection.close();
        purchaseOrderLinesConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
