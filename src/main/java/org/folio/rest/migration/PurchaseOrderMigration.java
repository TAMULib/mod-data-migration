package org.folio.rest.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.inventory.Entry;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Locations;
import org.folio.rest.jaxrs.model.inventory.Note;
import org.folio.rest.jaxrs.model.inventory.ReceivingHistory;
import org.folio.rest.jaxrs.model.orders.acq_models.common.schemas.ReferenceNumberItem;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePoLine;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePoLine.ReceiptStatus;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePurchaseOrder;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Cost;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Details;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Eresource;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.FundDistribution;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Location;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Ongoing;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.Physical;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.ProductIdentifier;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.VendorDetail;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.purchaseorder.PurchaseOrderContext;
import org.folio.rest.migration.model.request.purchaseorder.PurchaseOrderDefaults;
import org.folio.rest.migration.model.request.purchaseorder.PurchaseOrderJob;
import org.folio.rest.migration.model.request.purchaseorder.PurchaseOrderMaps;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;

public class PurchaseOrderMigration extends AbstractMigration<PurchaseOrderContext> {

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
  private static final String ISSN = "ISSN";
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
  // private static final String RECEIVED_DATE = "RECEIVED_DATE";
  private static final String MFHD_ID = "MFHD_ID";

  private static final String VENDOR_REFERENCE_ID = "vendorTypeId";
  private static final String INSTANCE_REFERENCE_ID = "instanceTypeId";
  private static final String HOLDING_REFERENCE_ID = "holdingTypeId";

  private PurchaseOrderMigration(PurchaseOrderContext context, String tenant) {
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

    taskQueue = new PartitionTaskQueue<PurchaseOrderContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
        migrationService.complete();
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    int index = 0;

    for (PurchaseOrderJob job : context.getJobs()) {

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

  public static PurchaseOrderMigration with(PurchaseOrderContext context, String tenant) {
    return new PurchaseOrderMigration(context, tenant);
  }

  public class OrderPartitionTask implements PartitionTask<PurchaseOrderContext> {

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

    public OrderPartitionTask execute(PurchaseOrderContext context) {
      long startTime = System.nanoTime();

      String token = (String) partitionContext.get(TOKEN);

      PurchaseOrderJob job = (PurchaseOrderJob) partitionContext.get(JOB);

      PurchaseOrderMaps maps = (PurchaseOrderMaps) partitionContext.get(MAPS);

      PurchaseOrderDefaults defaults = (PurchaseOrderDefaults) partitionContext.get(DEFAULTS);

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

      Map<String, Object> receivingHistoryContext = new HashMap<>();
      receivingHistoryContext.put(SQL, context.getExtraction().getReceivingHistorySql());
      receivingHistoryContext.put(SCHEMA, schema);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings);

      String vendorRLTypeId = job.getReferences().get(VENDOR_REFERENCE_ID);

      try (
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement lineItemNoteStatement = threadConnections.getLineItemNoteConnection().createStatement();
        Statement poLinesStatement = threadConnections.getPurchaseOrderLinesConnection().createStatement();
        Statement receivingHistoryStatement = threadConnections.getReceivingHistoryConnection().createStatement();
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

          CompletableFuture.allOf(
            getLineItemNotes(lineItemNoteStatement, lineItemNoteContext)
              .thenAccept((notes) -> compositePurchaseOrder.setNotes(notes)),
            getPurchaseOrderLines(poLinesStatement, poLinesContext, receivingHistoryStatement, receivingHistoryContext, job, maps, defaults, locationsMap, fundsMap, vendorReferenceId, token)
              .thenAccept((poLines) -> compositePurchaseOrder.setCompositePoLines(poLines))
          ).get();

          try {
            migrationService.okapiService.postCompositePurchaseOrder(tenant, token, compositePurchaseOrder);
          } catch (Exception e) {
            log.error("Failed to post composite purchase order {}\n{}", compositePurchaseOrder, e.getMessage());
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

    private CompletableFuture<List<CompositePoLine>> getPurchaseOrderLines(Statement poLinesStatement, Map<String, Object> poLinesContext, Statement receivingHistoryStatement, Map<String, Object> receivingHistoryContext, PurchaseOrderJob job, PurchaseOrderMaps maps, PurchaseOrderDefaults defaults, Map<String, String> locationsMap, Map<String, String> fundsMap, String vendorId, String token) {
      CompletableFuture<List<CompositePoLine>> future = new CompletableFuture<>();
      additionalExecutor.submit(() -> {

        String poId = (String) poLinesContext.get(PO_ID);
        String instanceRLTypeId = job.getReferences().get(INSTANCE_REFERENCE_ID);
        String holdingRLTypeId = job.getReferences().get(HOLDING_REFERENCE_ID);

        Map<String, String> expenseClasses = maps.getExpenseClasses().get(job.getSchema());
        Map<String, String> fundCodes = maps.getFundCodes().get(job.getSchema());

        List<CompositePoLine> poLines = new ArrayList<>();
        try (ResultSet poLinesResultSet = getResultSet(poLinesStatement, poLinesContext)) {
          while (poLinesResultSet.next()) {
            final String bibId = poLinesResultSet.getString(BIB_ID);
            final String lineItemId = poLinesResultSet.getString(LINE_ITEM_ID);
            // NOTE: ignored
            // final String linePrice = poLinesResultSet.getString(LINE_PRICE);
            final String poType = poLinesResultSet.getString(PO_TYPE);
            final String locationCode = poLinesResultSet.getString(LOCATION_CODE);
            final String locationId = poLinesResultSet.getString(LOCATION_ID);
            final String title = poLinesResultSet.getString(TITLE);
            final String issn = poLinesResultSet.getString(ISSN);
            final String lineItemStatus = poLinesResultSet.getString(LINE_ITEM_STATUS);
            final String requester = poLinesResultSet.getString(REQUESTER);
            final String vendorTitleNumber = poLinesResultSet.getString(VENDOR_TITLE_NUM);
            final String vendorRefQual = poLinesResultSet.getString(VENDOR_REF_QUAL);
            final String vendorRefNumber = poLinesResultSet.getString(VENDOR_REF_NUM);
            final String accountName = poLinesResultSet.getString(ACCOUNT_NAME);
            final String fundCode = poLinesResultSet.getString(FUND_CODE);
            // NOTE: ignored
            // final String note = poLinesResultSet.getString(NOTE);

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
                log.warn("{} mapped location id {} not found for po id {}", job.getSchema(), locationId, poId);
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

            String fCode = fundCode;

            if (StringUtils.isNotEmpty(fCode)) {

              fCode = fCode.toLowerCase();

              List<FundDistribution> fundDistributions = new ArrayList<>();
              FundDistribution fundDistribution = new FundDistribution();
              fundDistribution.setDistributionType(FundDistribution.DistributionType.PERCENTAGE);
              fundDistribution.setValue(100.0);

              // NOTE: conditioning on schema :(
              if (job.getSchema().equals("AMDB")) {

                if (fCode.startsWith("msv")) {
                  fCode = fCode.substring(3);
                }

                switch (fCode) {
                  case "seri":
                  case "serial":
                    fCode = "serials";
                    break;
                  case "qatar":
                    fCode = "etxtqatar";
                    break;
                  case "btetext":
                  case "btetxt":
                    fCode = "etxt";
                    break;
                  case "e-72997":
                    fCode = "barclay";
                    break;
                  case "chargeback":
                  case "access":
                  case "galveston":
                    fundDistribution.setExpenseClassId(expenseClasses.get(fCode));
                    fCode = "etxt";
                    break;
                  case "costshare":
                    // delete po ref by po id
                  break;
                  default:
                  break;
                }

                if (fCode.equals("etxt") && Objects.isNull(fundDistribution.getExpenseClassId())) {
                  fundDistribution.setExpenseClassId(expenseClasses.get("etxtnorm"));
                }

                if (fundsMap.containsKey(fCode)) {
                  fundDistribution.setFundId(fundsMap.get(fCode));
                } else {
                  log.warn("{} fund code {} not found for po {}", job.getSchema(), fCode, poId);
                  continue;
                }

              } else if (job.getSchema().equals("MSDB")) {

                String fundCodePrefix = fCode.substring(0, 2);
                if (fundCodes.containsKey(fundCodePrefix)) {
                  String mappedFundCode = fundCodes.get(fundCodePrefix);
                  if (fundsMap.containsKey(mappedFundCode)) {
                    fundDistribution.setFundId(fundsMap.get(mappedFundCode));
                  } else {
                    log.warn("{} fund code {} not found for po {}", job.getSchema(), mappedFundCode, poId);
                  }
                } else {
                  log.warn("{} fund code {} as {} not mapped", job.getSchema(), fCode, fundCodePrefix);
                }

                if (expenseClasses.containsKey(fCode)) {
                  fundDistribution.setExpenseClassId(expenseClasses.get(fCode));
                } else {
                  log.warn("{} expense class not mapped from {}", job.getSchema(), fCode);
                }

              }

              fundDistributions.add(fundDistribution);
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
                ReferenceNumberItem refNumItem = new ReferenceNumberItem();
                refNumItem.setRefNumberType(ReferenceNumberItem.RefNumberType.fromValue(maps.getVendorRefQual().get(vendorRefQual)));
                refNumItem.setRefNumber(vendorRefNumber);
                vendorDetail.getReferenceNumbers().add(refNumItem);
              } else if (StringUtils.isNotEmpty(vendorTitleNumber)) {
                ReferenceNumberItem refNumItem = new ReferenceNumberItem();
                refNumItem.setRefNumberType(ReferenceNumberItem.RefNumberType.fromValue(maps.getVendorRefQual().get(defaults.getVendorRefQual())));
                refNumItem.setRefNumber(vendorTitleNumber);
                vendorDetail.getReferenceNumbers().add(refNumItem);
              }

              compositePoLine.setVendorDetail(vendorDetail);
            }

            if (StringUtils.isNotEmpty(lineItemStatus)) {
              compositePoLine.setReceiptStatus(ReceiptStatus.fromValue(maps.getPoLineReceiptStatus().get(lineItemStatus)));
            }
            if (StringUtils.isNotEmpty(requester)) {
              compositePoLine.setRequester(requester);
            }

            receivingHistoryContext.put(LINE_ITEM_ID, lineItemId);

            Pattern pattern = Pattern.compile("^(.*?)(\\(.*)$");
            String mfhdId = null;
            String note = StringUtils.EMPTY;
            List<Entry> recievingHistoryEntries = new ArrayList<>();
            try (ResultSet receivingHistoryResultSet = getResultSet(receivingHistoryStatement, receivingHistoryContext)) {
              while (receivingHistoryResultSet.next()) {
                // NOTE: ignored
                // String predict = receivingHistoryResultSet.getString(PREDICT);
                // String opacSuppressed = receivingHistoryResultSet.getString(OPAC_SUPPRESSED);
                String receivingNote = receivingHistoryResultSet.getString(RECEIVING_NOTE);
                String enumchron = receivingHistoryResultSet.getString(ENUMCHRON);
                // String receivedDate = receivingHistoryResultSet.getString(RECEIVED_DATE);
                mfhdId = receivingHistoryResultSet.getString(MFHD_ID);

                Entry entry = new Entry();

                entry.setPublicDisplay(true);

                Matcher matcher = pattern.matcher(enumchron);

                if (matcher.matches()) {
                  if (matcher.groupCount() > 0) {
                    entry.setEnumeration(matcher.group(1));
                  }
                  if (matcher.groupCount() > 1) {
                    entry.setChronology(matcher.group(2));
                  }
                }

                recievingHistoryEntries.add(entry);

                if (StringUtils.isNotEmpty(receivingNote)) {
                  note = receivingNote
                    .replaceAll("[\\n]", StringUtils.EMPTY)
                    .replaceAll("\\s+", StringUtils.SPACE)
                    .replaceAll("(.*?)(MFHD<.*?>)(.*)", "$1$3");
                }

              }
            } catch (SQLException e) {
              e.printStackTrace();
            } finally {

              if (!recievingHistoryEntries.isEmpty() && Objects.nonNull(mfhdId)) {

                compositePoLine.setCheckinItems(true);

                Optional<ReferenceLink> holdingsRl = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(holdingRLTypeId, mfhdId);
                if (holdingsRl.isPresent()) {

                  String HoldingsRecordId = holdingsRl.get().getFolioReference();

                  Holdingsrecord holdingsRecord = migrationService.okapiService.fetchHoldingsRecordById(tenant, token, HoldingsRecordId);

                  ReceivingHistory receivingHistory = new ReceivingHistory();
                  receivingHistory.setEntries(recievingHistoryEntries);

                  holdingsRecord.setReceivingHistory(receivingHistory);

                  List<Note> holdingsRecordNotes = holdingsRecord.getNotes();

                  if (StringUtils.isNotEmpty(job.getHoldingsNoteToElide())) {
                    holdingsRecordNotes = holdingsRecordNotes.stream().filter(hn -> {
                      return !hn.getNote().toLowerCase().contains(job.getHoldingsNoteToElide());
                    }).collect(Collectors.toList());
                  }

                  for (Note an : job.getAdditionalHoldingsNotes()) {
                    Note holdingsNote = new Note();
                    holdingsNote.setStaffOnly(an.getStaffOnly());
                    holdingsNote.setNote(an.getNote());
                    holdingsNote.setHoldingsNoteTypeId(an.getHoldingsNoteTypeId());
                    holdingsRecordNotes.add(holdingsNote);
                  }

                  if (StringUtils.isNotEmpty(note)) {
                    Note holdingsNote = new Note();
                    holdingsNote.setStaffOnly(true);
                    holdingsNote.setNote(note);
                    holdingsNote.setHoldingsNoteTypeId(job.getHoldingsNoteTypeId());
                    holdingsRecordNotes.add(holdingsNote);
                  }

                  holdingsRecord.setNotes(holdingsRecordNotes);

                  try {
                    migrationService.okapiService.putHoldingsrecord(tenant, token, holdingsRecord);
                  } catch (Exception e) {
                    log.error("{} failed to update holdings record for mfhd id {}", job.getSchema(), mfhdId);
                  }

                } else {
                  log.error("{} no holdings record id found for mfhd id {}", job.getSchema(), mfhdId);
                }

              }

              Details details = new Details();

              if (StringUtils.isNotEmpty(issn)) {
                List<ProductIdentifier> productIds = details.getProductIds();
                ProductIdentifier productId = new ProductIdentifier();
                productId.setProductId(issn);
                productId.setProductIdType(job.getProductIdType());
                productIds.add(productId);
                details.setProductIds(productIds);
              }

              compositePoLine.setDetails(details);

              poLines.add(compositePoLine);
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
    threadConnections.setReceivingHistoryConnection(getConnection(voyagerSettings));
    return threadConnections;
  }

  private class ThreadConnections {

    private Connection pageConnection;

    private Connection lineItemNoteConnection;

    private Connection purchaseOrderLinesConnection;

    private Connection receivingHistoryConnection;

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

    public Connection getReceivingHistoryConnection() {
      return receivingHistoryConnection;
    }

    public void setReceivingHistoryConnection(Connection receivingHistoryConnection) {
      this.receivingHistoryConnection = receivingHistoryConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        lineItemNoteConnection.close();
        purchaseOrderLinesConnection.close();
        receivingHistoryConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

  }

}
