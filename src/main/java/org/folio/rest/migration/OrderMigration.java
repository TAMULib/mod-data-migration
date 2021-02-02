package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.FundDistribution;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePoLine;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePurchaseOrder;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Cost;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Details;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Eresource;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Ongoing;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Physical;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.VendorDetail;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePoLine.ReceiptStatus;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders_storage.schemas.Piece;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders_storage.schemas.Title;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders_storage.schemas.Piece.PieceFormat;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders_storage.schemas.Piece.ReceivingStatus;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Location;
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
  // NOTE: ignored
  // private static final String PO_STATUS = "PO_STATUS";
  private static final String VENDOR_ID = "VENDOR_ID";
  private static final String SHIPLOC = "SHIPLOC";
  private static final String BILLLOC = "BILLLOC";

  private static final String NOTE = "NOTE";

  private static final String BIB_ID = "BIB_ID";
  private static final String LINE_ITEM_ID = "LINE_ITEM_ID";
  // NOTE: ignored
  // private static final String LINE_PRICE = "LINE_PRICE";
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

  // NOTE: ignored
  // private static final String PREDICT = "PREDICT";
  // private static final String OPAC_SUPPRESSED = "OPAC_SUPPRESSED";
  private static final String RECEIVING_NOTE = "RECEIVING_NOTE";
  private static final String ENUMCHRON = "ENUMCHRON";
  private static final String RECEIVED_DATE = "RECEIVED_DATE";

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

    Map<String, String> fundsMap = migrationService.okapiService.fetchFunds(tenant, token).getFunds().stream()
      .collect(Collectors.toMap(fund -> fund.getCode().toLowerCase(), fund -> fund.getId()));

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

      Map<String, Object> piecesContext = new HashMap<>();
      piecesContext.put(SQL, context.getExtraction().getPiecesSql());
      piecesContext.put(SCHEMA, schema);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement lineItemNoteStatement = threadConnections.getLineItemNoteConnection().createStatement();
        Statement poLinesStatement = threadConnections.getPurchaseOrderLinesConnection().createStatement();
        Statement piecesStatement = threadConnections.getPiecesConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {
        while (pageResultSet.next()) {

          String poId = pageResultSet.getString(PO_ID);
          String poNumber = pageResultSet.getString(PO_NUMBER);
          // NOTE: ignored
          // String poStatus = pageResultSet.getString(PO_STATUS);
          String vendorId = pageResultSet.getString(VENDOR_ID);
          String shipLoc = pageResultSet.getString(SHIPLOC);
          String billLoc = pageResultSet.getString(BILLLOC);

          lineItemNoteContext.put(PO_ID, poId);
          poLinesContext.put(PO_ID, poId);

          final CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();

          compositePurchaseOrder.setId(UUID.randomUUID().toString());

          compositePurchaseOrder.setApproved(false);
          compositePurchaseOrder.setWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.PENDING);
          compositePurchaseOrder.setManualPo(false);

          if (StringUtils.isNotEmpty(poNumber)) {
            compositePurchaseOrder.setPoNumber(StringUtils.deleteWhitespace(String.format("%s%s", job.getPoNumberPrefix(), poNumber)));
          }

          if (job.getIncludeAddresses()) {
            String billToKey = StringUtils.isNotEmpty(billLoc) ? billLoc : defaults.getAqcAddressCode();
            String shipToKey = StringUtils.isNotEmpty(shipLoc) ? shipLoc : defaults.getAqcAddressCode();
            compositePurchaseOrder.setBillTo(maps.getAcqAddresses().get(billToKey));
            compositePurchaseOrder.setShipTo(maps.getAcqAddresses().get(shipToKey));
          }

          compositePurchaseOrder.setOrderType(CompositePurchaseOrder.OrderType.ONGOING);

          Optional<ReferenceLink> vendorRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(vendorRLTypeId, vendorId);
          if (!vendorRL.isPresent()) {
            log.error("{} no vendor id found for vendor id {}", schema, vendorId);
            continue;
          }

          String vendorReferenceId = vendorRL.get().getFolioReference();

          compositePurchaseOrder.setVendor(vendorReferenceId);

          Ongoing ongoing = new Ongoing();
          ongoing.setInterval(365);
          ongoing.setIsSubscription(true);
          ongoing.setManualRenewal(true);

          compositePurchaseOrder.setOngoing(ongoing);

          Map<String, List<Piece>> pieces = new HashMap<>();

          CompletableFuture.allOf(
            getLineItemNotes(lineItemNoteStatement, lineItemNoteContext)
              .thenAccept((notes) -> compositePurchaseOrder.setNotes(notes)),
            getPurchaseOrderLines(poLinesStatement, poLinesContext, piecesStatement, piecesContext, job, maps, defaults, locationsMap, fundsMap, vendorReferenceId)
              .thenAccept((poLines) -> {
                List<CompositePoLine> cPoLines = new ArrayList<>();
                poLines.stream().forEach(cpowp -> {
                  cPoLines.add(cpowp.getCompositePoLine());
                  pieces.put(cpowp.getCompositePoLine().getPoLineNumber(), cpowp.getPieces());
                });
                compositePurchaseOrder.setCompositePoLines(cPoLines);
              })
          ).get();

          try {
            migrationService.okapiService.postCompositePurchaseOrder(tenant, token, compositePurchaseOrder)
              .getCompositePoLines().forEach(cpol -> {
                String poLineNumber = cpol.getPoLineNumber();
                try {
                  Title title = migrationService.okapiService.fetchTitleByPurchaseOrderLineNumber(tenant, token, poLineNumber);
                  pieces.get(poLineNumber).forEach(piece -> {
                    piece.setTitleId(title.getId());
                    try {
                      migrationService.okapiService.postPiece(tenant, token, piece);
                    } catch (Exception e) {
                      log.error("Failed to post piece {}\n {}", piece, e.getMessage());
                    }
                  });
                } catch (Exception e) {
                  log.error("Failed to fetch title by purchase order line number {}\n{}", poLineNumber, e.getMessage());
                }
              });
          } catch (Exception e) {
            log.error("Failed to post composite purchase order {}\n {}", compositePurchaseOrder, e.getMessage());
          }

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

    private CompletableFuture<List<String>> getLineItemNotes(Statement statement, Map<String, Object> context) {
      CompletableFuture<List<String>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {
        List<String> notes =  new ArrayList<>();
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

    private CompletableFuture<List<CompositePoLineWithPieces>> getPurchaseOrderLines(Statement poLinesStatement, Map<String, Object> poLinesContext, Statement piecesStatement, Map<String, Object> piecesContext, OrderJob job, OrderMaps maps, OrderDefaults defaults, Map<String, String> locationsMap, Map<String, String> fundsMap, String vendorId) {
      CompletableFuture<List<CompositePoLineWithPieces>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {

        String poId = (String) poLinesContext.get(PO_ID);
        String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);

        Map<String, String> expenseClasses = maps.getExpenseClasses().get(job.getSchema());
        Map<String, String> fundCodes = maps.getFundCodes().get(job.getSchema());

        List<CompositePoLineWithPieces> poLines = new ArrayList<>();
        try (ResultSet poLinesResultSet = getResultSet(poLinesStatement, poLinesContext)) {
          while (poLinesResultSet.next()) {
            String bibId = poLinesResultSet.getString(BIB_ID);
            String lineItemId = poLinesResultSet.getString(LINE_ITEM_ID);
            // NOTE: ignored
            // String linePrice = poLinesResultSet.getString(LINE_PRICE);
            String poType = poLinesResultSet.getString(PO_TYPE);
            String locationCode = poLinesResultSet.getString(LOCATION_CODE);
            String locationId = poLinesResultSet.getString(LOCATION_ID);
            String title = poLinesResultSet.getString(TITLE);
            String lineItemStatus = poLinesResultSet.getString(LINE_ITEM_STATUS);
            String requester = poLinesResultSet.getString(REQUESTER);
            String vendorTitleNumber = poLinesResultSet.getString(VENDOR_TITLE_NUM);
            String vendorRefQual = poLinesResultSet.getString(VENDOR_REF_QUAL);
            String vendorRefNumber = poLinesResultSet.getString(VENDOR_REF_NUM);
            String accountName = poLinesResultSet.getString(ACCOUNT_NAME);
            String fundCode = poLinesResultSet.getString(FUND_CODE);
            // NOTE: ignored
            // String note = poLinesResultSet.getString(NOTE);

            Optional<ReferenceLink> instanceRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(instanceRLTypeId, bibId);
            if (!instanceRL.isPresent()) {
              log.error("{} no instance id found for bib id {}", job.getSchema(), bibId);
              continue;
            }

            CompositePoLine compositePoLine = new CompositePoLine();
            String folioLocationId;

            compositePoLine.setId(UUID.randomUUID().toString());
            compositePoLine.setInstanceId(instanceRL.get().getFolioReference());
            compositePoLine.setTitleOrPackage(title);
            compositePoLine.setSource(CompositePoLine.Source.USER);
            compositePoLine.setAcquisitionMethod(CompositePoLine.AcquisitionMethod.fromValue(maps.getPoLineAcqMethods().get(poType)));

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

            Location location = new Location();
            Cost cost = new Cost();
            cost.setCurrency("USD");
            if (StringUtils.isNotEmpty(locationCode) && !locationCode.toLowerCase().startsWith("www")) {
              compositePoLine.setOrderFormat(CompositePoLine.OrderFormat.PHYSICAL_RESOURCE);
              Physical physical = new Physical();
              physical.setCreateInventory(Physical.CreateInventory.NONE);
              compositePoLine.setPhysical(physical);
              cost.setListUnitPrice(0.0);
              cost.setQuantityPhysical(1);
              location.setLocationId(folioLocationId);
              location.setQuantityPhysical(1);
            } else {
              Eresource eresource = new Eresource();
              eresource.setCreateInventory(Eresource.CreateInventory.NONE);
              eresource.setAccessProvider(vendorId);
              compositePoLine.setEresource(eresource);
              compositePoLine.setOrderFormat(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE);
              cost.setListUnitPriceElectronic(0.0);
              cost.setQuantityElectronic(1);
              location.setLocationId(folioLocationId);
              location.setQuantityElectronic(1);
            }

            compositePoLine.setCost(cost);
            List<Location> locations = new ArrayList<>();
            locations.add(location);
            compositePoLine.setLocations(locations);

            if (StringUtils.isNotEmpty(fundCode)) {

              fundCode = fundCode.toLowerCase();

              FundDistribution fundDistribution = new FundDistribution();
              fundDistribution.setDistributionType(FundDistribution.DistributionType.PERCENTAGE);
              fundDistribution.setValue(100.0);

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
                    fundDistribution.setExpenseClassId(expenseClasses.get(fundCode));
                    fundCode = "etxt";
                    break;
                  case "costshare":
                   // delete po ref by po id
                  break;
                  default:
                  break;
                }

                if (fundsMap.containsKey(fundCode)) {
                  fundDistribution.setFundId(fundsMap.get(fundCode));
                } else {
                  log.error("{} fund code {} not found for po {}", job.getSchema(), fundCode, poId);
                }

              } else if (job.getSchema().equals("MSDB")) {

                String fundCodePrefix = fundCode.substring(0, 2);
                if (fundCodes.containsKey(fundCodePrefix)) {
                  String mappedFundCode = fundCodes.get(fundCodePrefix);
                  if (fundsMap.containsKey(mappedFundCode)) {
                    fundDistribution.setFundId(fundsMap.get(mappedFundCode));
                  } else {
                    log.error("{} fund code {} not found for po {}", job.getSchema(), mappedFundCode, poId);
                  }
                } else {
                  log.error("{} fund code {} as {} not mapped", job.getSchema(), fundCode, fundCodePrefix);
                }

                if (expenseClasses.containsKey(fundCode)) {
                  fundDistribution.setExpenseClassId(expenseClasses.get(fundCode));
                } else {
                  log.error("{} expense class not mapped from {}", job.getSchema(), fundCode);
                }

              }

              List<FundDistribution> fundDistributions = new ArrayList<>();
              compositePoLine.setFundDistribution(fundDistributions);

            } else {
              log.debug("{} no fund code for po {}", job.getSchema(), poId);
            }

            // NOTE: conditioning on schema again :(
            if (job.getSchema().equals("AMDB")) {
              VendorDetail vendorDetail = new VendorDetail();
              if (StringUtils.isNotEmpty(accountName)) {
                vendorDetail.setVendorAccount(accountName);
              }
              vendorDetail.setInstructions(StringUtils.SPACE);

              if (StringUtils.isNotEmpty(vendorRefNumber)) {
                vendorDetail.setRefNumberType(VendorDetail.RefNumberType.fromValue(maps.getVendorRefQual().get(vendorRefQual)));
                vendorDetail.setRefNumber(vendorRefNumber);
              } else if (StringUtils.isNotEmpty(vendorTitleNumber)) {
                vendorDetail.setRefNumberType(VendorDetail.RefNumberType.fromValue(maps.getVendorRefQual().get(defaults.getVendorRefQual())));
                vendorDetail.setRefNumber(vendorTitleNumber);
              }

              compositePoLine.setVendorDetail(vendorDetail);
            }

            if (StringUtils.isNotEmpty(lineItemStatus)) {
              compositePoLine.setReceiptStatus(ReceiptStatus.fromValue(maps.getPoLineReceiptStatus().get(lineItemStatus)));
            }
            if (StringUtils.isNotEmpty(requester)) {
              compositePoLine.setRequester(requester);
            }

            piecesContext.put(LINE_ITEM_ID, lineItemId);

            String note = StringUtils.EMPTY;
            List<Piece> pieces = new ArrayList<>();
            try (ResultSet piecesResultSet = getResultSet(piecesStatement, piecesContext)) {
              while (piecesResultSet.next()) {
                // NOTE: ignored
                // String predict = piecesResultSet.getString(PREDICT);
                // String opacSuppressed = piecesResultSet.getString(OPAC_SUPPRESSED);
                String receivingNote = piecesResultSet.getString(RECEIVING_NOTE);
                String enumchron = piecesResultSet.getString(ENUMCHRON);
                String receivedDate = piecesResultSet.getString(RECEIVED_DATE);

                Piece piece = new Piece();

                piece.setId(UUID.randomUUID().toString());
                piece.setPoLineId(compositePoLine.getId());
                piece.setLocationId(compositePoLine.getLocations().get(0).getLocationId());
                piece.setCaption(enumchron);
                piece.setFormat(PieceFormat.PHYSICAL);
                piece.setReceivingStatus(ReceivingStatus.RECEIVED);
                piece.setReceivedDate(Date.from(Instant.parse(receivedDate)));

                pieces.add(piece);

                if (StringUtils.isNotEmpty(receivingNote)) {
                  note = receivingNote
                    .replaceAll("[\\n]", StringUtils.EMPTY)
                    .replaceAll("\\s+", StringUtils.SPACE)
                    .replaceAll("(.*?)(MFHD<.*?>)(.*)", "$1$3")
                    .replaceAll("(.*?)(LOC<.*?>)(.*)", "$1$3");
                }

              }
            } catch (SQLException e) {
              e.printStackTrace();
            } finally {

              if (StringUtils.isNotEmpty(note)) {
                Details details = new Details();
                details.setReceivingNote(note);
                compositePoLine.setDetails(details);
              }
  
              compositePoLine.setCheckinItems(!pieces.isEmpty());
  
              poLines.add(new CompositePoLineWithPieces(compositePoLine, pieces));

            }
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

  private class CompositePoLineWithPieces {

    private final CompositePoLine compositePoLine;

    private final List<Piece> pieces;

    public CompositePoLineWithPieces(CompositePoLine compositePoLine, List<Piece> pieces) {
      this.compositePoLine = compositePoLine;
      this.pieces = pieces;
    }

    public CompositePoLine getCompositePoLine() {
      return compositePoLine;
    }

    public List<Piece> getPieces() {
      return pieces;
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
          Optional<org.folio.rest.jaxrs.model.inventory.Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code)).findFirst();
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
    threadConnections.setPiecesConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private Connection lineItemNoteConnection;

    private Connection purchaseOrderLinesConnection;

    private Connection piecesConnection;

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

    public Connection getPiecesConnection() {
      return piecesConnection;
    }

    public void setPiecesConnection(Connection piecesConnection) {
      this.piecesConnection = piecesConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        lineItemNoteConnection.close();
        purchaseOrderLinesConnection.close();
        piecesConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
