package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Loantype;
import org.folio.rest.jaxrs.model.Loantypes;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Locations;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.ItemRecord;
import org.folio.rest.migration.model.request.ItemContext;
import org.folio.rest.migration.model.request.ItemDefaults;
import org.folio.rest.migration.model.request.ItemJob;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import io.vertx.core.json.JsonObject;

public class ItemMigration extends AbstractMigration<ItemContext> {

  private static final String HRID_PREFIX = "HRID_PREFIX";
  private static final String HRID_START_NUMBER = "HRID_START_NUMBER";

  private static final String LOAN_TYPES_MAP = "LOAN_TYPES_MAP";
  private static final String LOCATIONS_MAP = "LOCATIONS_MAP";

  private static final String ITEM_ID = "ITEM_ID";
  private static final String PERM_ITEM_TYPE_ID = "ITEM_TYPE_ID";
  private static final String PERM_LOCATION_ID = "PERM_LOCATION";
  private static final String PIECES = "PIECES";
  private static final String TEMP_LOCATION_ID = "TEMP_LOCATION";
  private static final String TEMP_TYPE_ID = "TEMP_ITEM_TYPE_ID";

  private static final String CHRON = "CHRON";
  private static final String ITEM_ENUM = "ITEM_ENUM";

  private static final String ITEM_BARCODE = "ITEM_BARCODE";

  private static final String ITEM_TYPE_ID = "ITEM_TYPE_ID";
  private static final String ITEM_TYPE_CODE = "ITEM_TYPE_CODE";

  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";

  private static final String ITEM_REFERENCE_ID = "itemTypeId";
  private static final String ITEM_TO_HOLDING_REFERENCE_ID = "itemToHoldingTypeId";

  // (id,jsonb,creation_date,created_by,holdingsrecordid,permanentloantypeid,temporaryloantypeid,materialtypeid,permanentlocationid,temporarylocationid,effectivelocationid)
  private static String ITEM_COPY_SQL = "COPY %s_mod_inventory_storage.item (id,jsonb,creation_date,created_by,holdingsrecordid,permanentloantypeid,temporaryloantypeid,materialtypeid,permanentlocationid,temporarylocationid,effectivelocationid) FROM STDIN WITH NULL AS 'null'";

  private ItemMigration(ItemContext context, String tenant) {
    super(context, tenant);
  }

  @Override
  public CompletableFuture<Boolean> run(MigrationService migrationService) {
    log.info("tenant: {}", tenant);

    log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

    log.info("available processors: {}", Runtime.getRuntime().availableProcessors());

    String token = migrationService.okapiService.getToken(tenant);

    JsonObject hridSettings = migrationService.okapiService.fetchHridSettings(tenant, token);

    Loantypes loanTypes = migrationService.okapiService.fetchLoanTypes(tenant, token);
    Locations locations = migrationService.okapiService.fetchLocations(tenant, token);

    Database voyagerSettings = context.getExtraction().getDatabase();
    Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

    preActions(folioSettings, context.getPreActions());

    taskQueue = new PartitionTaskQueue<ItemContext>(context, new TaskCallback() {

      @Override
      public void complete() {
        postActions(folioSettings, context.getPostActions());
      }

    });

    Map<String, Object> countContext = new HashMap<>();
    countContext.put(SQL, context.getExtraction().getCountSql());

    JsonObject holdingsHridSettings = hridSettings.getJsonObject("items");
    String hridPrefix = holdingsHridSettings.getString("prefix");

    int originalHridStartNumber = holdingsHridSettings.getInteger("startNumber");
    int hridStartNumber = originalHridStartNumber;

    int index = 0;

    for (ItemJob job : context.getJobs()) {

      countContext.put(SCHEMA, job.getSchema());

      Map<String, String> loanTypesMap = getLoanTypesMap(loanTypes, job.getSchema());
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
        partitionContext.put(OFFSET, offset);
        partitionContext.put(LIMIT, limit);
        partitionContext.put(INDEX, index);
        partitionContext.put(TOKEN, token);
        partitionContext.put(HRID_PREFIX, hridPrefix);
        partitionContext.put(HRID_START_NUMBER, hridStartNumber);
        partitionContext.put(JOB, job);
        partitionContext.put(LOAN_TYPES_MAP, loanTypesMap);
        partitionContext.put(LOCATIONS_MAP, locationsMap);
        log.info("submitting task schema {}, offset {}, limit {}", job.getSchema(), offset, limit);
        taskQueue.submit(new ItemPartitionTask(migrationService, partitionContext));
        offset += limit;
        index++;
        if (i < partitions) {
          hridStartNumber += limit;
        } else {
          hridStartNumber = originalHridStartNumber + count;
        }
      }
    }

    return CompletableFuture.completedFuture(true);
  }

  public static ItemMigration with(ItemContext context, String tenant) {
    return new ItemMigration(context, tenant);
  }

  public class ItemPartitionTask implements PartitionTask<ItemContext> {

    private final MigrationService migrationService;

    private final Map<String, Object> partitionContext;

    private int hrid;

    public ItemPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext) {
      this.migrationService = migrationService;
      this.partitionContext = partitionContext;
      this.hrid = (int) partitionContext.get(HRID_START_NUMBER);
    }

    public int getIndex() {
      return (int) partitionContext.get(INDEX);
    }

    @Override
    public PartitionTask<ItemContext> execute(ItemContext context) {
      long startTime = System.nanoTime();

      String hridPrefix = (String) partitionContext.get(HRID_PREFIX);

      ItemJob job = (ItemJob) partitionContext.get(JOB);

      Map<String, String> loanTypesMap = (Map<String, String>) partitionContext.get(LOAN_TYPES_MAP);
      Map<String, String> locationsMap = (Map<String, String>) partitionContext.get(LOCATIONS_MAP);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      ItemDefaults itemDefaults = context.getDefaults();

      Map<String, Object> mfhdContext = new HashMap<>();
      mfhdContext.put(SQL, context.getExtraction().getMfhdSql());
      mfhdContext.put(SCHEMA, schema);

      Map<String, Object> barcodeContext = new HashMap<>();
      barcodeContext.put(SQL, context.getExtraction().getBarcodeSql());
      barcodeContext.put(SCHEMA, schema);

      String itemRLTypeId = job.getReferences().get(ITEM_REFERENCE_ID);
      String itemToHoldingRLTypeId = job.getReferences().get(ITEM_TO_HOLDING_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try (
        PrintWriter itemWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getItemConnection(), String.format(ITEM_COPY_SQL, tenant)), true);
        Statement pageStatement = threadConnections.getPageConnection().createStatement();
        Statement mfhdItemStatement = threadConnections.getMfhdConnection().createStatement();
        Statement barcodeStatement = threadConnections.getBarcodeConnection().createStatement();
        ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String itemId = pageResultSet.getString(ITEM_ID);

          String voyagerPermTypeId = pageResultSet.getString(PERM_ITEM_TYPE_ID);
          String voyagerPermLocationId = pageResultSet.getString(PERM_LOCATION_ID);

          String voyagerTempTypeId = pageResultSet.getString(TEMP_TYPE_ID);
          String voyagerTempLocationId = pageResultSet.getString(TEMP_LOCATION_ID);

          int numberOfPieces = pageResultSet.getInt(PIECES);

          String permLoanTypeId, permLocationId;

          String voyagerPermTypeIdKey = String.format(KEY_TEMPLATE, schema, voyagerPermTypeId);
          if (loanTypesMap.containsKey(voyagerPermTypeIdKey)) {
            permLoanTypeId = loanTypesMap.get(voyagerPermTypeIdKey);
          } else {
            log.warn("using default permanent loan type for schema {} itemId {} type {}", schema, itemId, voyagerPermTypeId);
            permLoanTypeId = itemDefaults.getPermanentLoanTypeId();
          }

          String voyagerPermLocationIdKey = String.format(KEY_TEMPLATE, schema, voyagerPermLocationId);
          if (locationsMap.containsKey(voyagerPermLocationIdKey)) {
            permLocationId = locationsMap.get(voyagerPermLocationIdKey);
          } else {
            log.warn("using default permanent location for schema {} itemId {} location {}", schema, itemId, voyagerPermLocationId);
            permLocationId = itemDefaults.getPermanentLocationId();
          }

          mfhdContext.put(ITEM_ID, itemId);
          barcodeContext.put(ITEM_ID, itemId);

          try {
            MfhdItem mfhdItem = getMfhdItem(mfhdItemStatement, mfhdContext);
            String barcode = getItemBarcode(barcodeStatement, barcodeContext);

            ItemRecord itemRecord = new ItemRecord(itemId, barcode, mfhdItem.getChron(), mfhdItem.getItemEnum(), numberOfPieces, job.getMaterialTypeId());

            itemRecord.setPermanentLoanTypeId(permLoanTypeId);
            itemRecord.setPermanentLocationId(permLocationId);

            String voyagerTempTypeIdKey = String.format(KEY_TEMPLATE, schema, voyagerTempTypeId);
            if (loanTypesMap.containsKey(voyagerTempTypeIdKey)) {
              itemRecord.setTemporaryLoanTypeId(loanTypesMap.get(voyagerTempTypeIdKey));
            }

            String voyagerTempLocationIdKey = String.format(KEY_TEMPLATE, schema, voyagerTempLocationId);
            if (locationsMap.containsKey(voyagerTempLocationIdKey)) {
              itemRecord.setTemporaryLocationId(locationsMap.get(voyagerTempLocationIdKey));
            }

            String id = null, holdingId = null;

            Optional<ReferenceLink> itemRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(itemRLTypeId, itemId);

            if (itemRL.isPresent()) {

              id = itemRL.get().getFolioReference();

              Optional<ReferenceLink> itemToHoldingRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(itemToHoldingRLTypeId, itemRL.get().getId());

              if (itemToHoldingRL.isPresent()) {
                Optional<ReferenceLink> holdingRL = migrationService.referenceLinkRepo.findById(itemToHoldingRL.get().getFolioReference());

                if (holdingRL.isPresent()) {
                  holdingId = holdingRL.get().getFolioReference();
                }
              }
            }

            if (Objects.isNull(id)) {
              log.error("{} no item record id found for item id {}", schema, itemId);
              continue;
            }

            if (Objects.isNull(holdingId)) {
              log.error("{} no holdings record id found for item id {}", schema, itemId);
              continue;
            }

            itemRecord.setId(id);
            itemRecord.setHoldingId(holdingId);

            Date createdDate = new Date();
            itemRecord.setCreatedByUserId(job.getUserId());
            itemRecord.setCreatedDate(createdDate);

            String createdAt = DATE_TIME_FOMATTER.format(createdDate.toInstant().atOffset(ZoneOffset.UTC));
            String createdByUserId = job.getUserId();

            Item item = itemRecord.toItem(hridPrefix, hrid);

            String iUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(item)));

            // // (id,jsonb,creation_date,created_by,holdingsrecordid,permanentloantypeid,temporaryloantypeid,materialtypeid,permanentlocationid,temporarylocationid,effectivelocationid)
            itemWriter.println(String.join("\t",
              item.getId(),
              iUtf8Json,
              createdAt,
              createdByUserId,
              item.getHoldingsRecordId(),
              item.getPermanentLoanTypeId(),
              Objects.nonNull(item.getTemporaryLoanTypeId()) ? item.getTemporaryLoanTypeId() : NULL,
              item.getMaterialTypeId(),
              item.getPermanentLocationId(),
              Objects.nonNull(item.getTemporaryLocationId()) ? item.getTemporaryLocationId() : NULL,
              Objects.nonNull(item.getEffectiveLocationId()) ? item.getEffectiveLocationId() : NULL
            ));

            hrid++;
            count++;

          } catch (JsonProcessingException e) {
            log.error("{} item id {} error serializing item", schema, itemId);
            log.debug(e.getMessage());
          }

        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        threadConnections.closeAll();
      }

      log.info("{} {} item finished {}-{} in {} milliseconds", schema, index, hrid - count, hrid, TimingUtility.getDeltaInMilliseconds(startTime));

      return this;
    }

  }

  private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
    ThreadConnections threadConnections = new ThreadConnections();
    threadConnections.setPageConnection(getConnection(voyagerSettings));
    threadConnections.setBarcodeConnection(getConnection(voyagerSettings));
    threadConnections.setMfhdConnection(getConnection(voyagerSettings));
    try {
      threadConnections.setItemConnection(getConnection(folioSettings).unwrap(BaseConnection.class));
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return threadConnections;
  }

  private class ThreadConnections {
    private Connection pageConnection;
    private Connection mfhdConnection;
    private Connection barcodeConnection;

    private BaseConnection itemConnection;

    public ThreadConnections() {

    }

    public Connection getPageConnection() {
      return pageConnection;
    }

    public void setPageConnection(Connection pageConnection) {
      this.pageConnection = pageConnection;
    }

    public Connection getMfhdConnection() {
      return mfhdConnection;
    }

    public void setMfhdConnection(Connection mfhdConnection) {
      this.mfhdConnection = mfhdConnection;
    }

    public Connection getBarcodeConnection() {
      return barcodeConnection;
    }

    public void setBarcodeConnection(Connection barcodeConnection) {
      this.barcodeConnection = barcodeConnection;
    }

    public BaseConnection getItemConnection() {
      return itemConnection;
    }

    public void setItemConnection(BaseConnection itemConnection) {
      this.itemConnection = itemConnection;
    }

    public void closeAll() {
      try {
        pageConnection.close();
        mfhdConnection.close();
        barcodeConnection.close();
        itemConnection.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private MfhdItem getMfhdItem(Statement statement, Map<String, Object> context) throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      MfhdItem mfhdItem = null;
      while (resultSet.next()) {
        String chron = resultSet.getString(CHRON);
        String itemEnum = resultSet.getString(ITEM_ENUM);

        mfhdItem = new MfhdItem(chron, itemEnum);
      }
      return mfhdItem;
    }
  }

  private String getItemBarcode(Statement statement, Map<String, Object> context) throws SQLException {
    String itemBarcode = null;
    try (ResultSet resultSet = getResultSet(statement, context)) {
      while (resultSet.next()) {
        itemBarcode = resultSet.getString(ITEM_BARCODE);
      }
    }
    return itemBarcode;
  }

  private Map<String, String> getLoanTypesMap(Loantypes loanTypes, String schema) {
    Map<String, String> idToUuid = new HashMap<>();
    Map<String, Object> itemTypeContext = new HashMap<>();
    itemTypeContext.put(SQL, context.getExtraction().getItemTypeSql());
    itemTypeContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<String, String> ltConv = context.getMaps().getLoanType();
    try(
      Connection voyagerConnection = getConnection(voyagerSettings);
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, itemTypeContext);
    ) {
      while (rs.next()) {
        String id = rs.getString(ITEM_TYPE_ID);
        if (Objects.nonNull(id)) {
          String originalCode = rs.getString(ITEM_TYPE_CODE);
          String code = ltConv.containsKey(originalCode) ? ltConv.get(originalCode) : rs.getString(ITEM_TYPE_CODE);
          Optional<Loantype> loanType = loanTypes.getLoantypes().stream().filter(lt -> lt.getName().equals(code)).findFirst();
          if (loanType.isPresent()) {
            String key = String.format(KEY_TEMPLATE, schema, id);
            idToUuid.put(key, loanType.get().getId());
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToUuid;
  }

  private Map<String, String> getLocationsMap(Locations locations, String schema) {
    Map<String, String> idToUuid = new HashMap<>();
    Map<String, Object> locationContext = new HashMap<>();
    locationContext.put(SQL, context.getExtraction().getLocationSql());
    locationContext.put(SCHEMA, schema);
    Database voyagerSettings = context.getExtraction().getDatabase();
    Map<String, String> locConv = context.getMaps().getLocation();
    try(
      Connection voyagerConnection = getConnection(voyagerSettings);
      Statement st = voyagerConnection.createStatement();
      ResultSet rs = getResultSet(st, locationContext);
    ) {
      while (rs.next()) {
        String id = rs.getString(LOCATION_ID);
        if (Objects.nonNull(id)) {
          String key = String.format(KEY_TEMPLATE, schema, id);
          String code = locConv.containsKey(key) ? locConv.get(key) : rs.getString(LOCATION_CODE);
          Optional<Location> location = locations.getLocations().stream().filter(loc -> loc.getCode().equals(code)).findFirst();
          if (location.isPresent()) {
            idToUuid.put(key, location.get().getId());
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return idToUuid;
  }

  public class MfhdItem {

    private final String chron;
    private final String itemEnum;

    public MfhdItem(String chron, String itemEnum) {
        this.chron = chron;
        this.itemEnum = itemEnum;
    }

    public String getChron() {
        return chron;
    }

    public String getItemEnum() {
        return itemEnum;
    }

  }

}
