package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.CirculationNote;
import org.folio.rest.jaxrs.model.CirculationNote.NoteType;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Loantype;
import org.folio.rest.jaxrs.model.Loantypes;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Locations;
import org.folio.rest.jaxrs.model.Materialtype;
import org.folio.rest.jaxrs.model.Materialtypes;
import org.folio.rest.jaxrs.model.Note__1;
import org.folio.rest.jaxrs.model.Statisticalcodes;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.ItemRecord;
import org.folio.rest.migration.model.ItemStatusRecord;
import org.folio.rest.migration.model.request.item.ItemContext;
import org.folio.rest.migration.model.request.item.ItemDefaults;
import org.folio.rest.migration.model.request.item.ItemJob;
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
  private static final String STATISTICAL_CODES = "STATISTICAL_CODES";
  private static final String MATERIAL_TYPES = "MATERIAL_TYPES";

  private static final String ITEM_ID = "ITEM_ID";
  private static final String PERM_ITEM_TYPE_ID = "ITEM_TYPE_ID";
  private static final String PERM_LOCATION_ID = "PERM_LOCATION";
  private static final String PIECES = "PIECES";
  private static final String TEMP_LOCATION_ID = "TEMP_LOCATION";
  private static final String TEMP_TYPE_ID = "TEMP_ITEM_TYPE_ID";

  private static final String CAPTION = "CAPTION";
  private static final String CHRON = "CHRON";
  private static final String ITEM_ENUM = "ITEM_ENUM";
  private static final String FREETEXT = "FREETEXT";
  private static final String YEAR = "YEAR";

  private static final String ITEM_BARCODE = "ITEM_BARCODE";
  private static final String SPINE_LABEL = "SPINE_LABEL";

  private static final String ITEM_TYPE_ID = "ITEM_TYPE_ID";
  private static final String ITEM_TYPE_CODE = "ITEM_TYPE_CODE";

  private static final String LOCATION_ID = "LOCATION_ID";
  private static final String LOCATION_CODE = "LOCATION_CODE";

  private static final String ITEM_REFERENCE_ID = "itemTypeId";
  private static final String ITEM_TO_HOLDING_REFERENCE_ID = "itemToHoldingTypeId";

  private static final String ITEM_STATUS = "ITEM_STATUS";
  private static final String ITEM_STATUS_DATE = "ITEM_STATUS_DATE";
  private static final String CIRCTRANS = "CIRCTRANS";
  private static final String ITEM_STATUS_DESC = "ITEM_STATUS_DESC";

  private static final String ITEM_NOTE = "ITEM_NOTE";
  private static final String ITEM_NOTE_TYPE = "ITEM_NOTE_TYPE";

  private static final String MTYPE_CODE = "MTYPE_CODE";

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
    Statisticalcodes statisticalcodes = migrationService.okapiService.fetchStatisticalCodes(tenant, token);
    Materialtypes materialTypes = migrationService.okapiService.fetchMaterialtypes(tenant, token);

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
        partitionContext.put(STATISTICAL_CODES, statisticalcodes);
        partitionContext.put(MATERIAL_TYPES, materialTypes);
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

      ItemDefaults defaults = context.getDefaults();

      Map<String, String> loanTypesMap = (Map<String, String>) partitionContext.get(LOAN_TYPES_MAP);
      Map<String, String> locationsMap = (Map<String, String>) partitionContext.get(LOCATIONS_MAP);

      String schema = job.getSchema();

      int index = this.getIndex();

      Database voyagerSettings = context.getExtraction().getDatabase();

      Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

      JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

      Map<String, Object> mfhdContext = new HashMap<>();
      mfhdContext.put(SQL, context.getExtraction().getMfhdSql());
      mfhdContext.put(SCHEMA, schema);

      Map<String, Object> barcodeContext = new HashMap<>();
      barcodeContext.put(SQL, context.getExtraction().getBarcodeSql());
      barcodeContext.put(SCHEMA, schema);

      Map<String, Object> itemStatusContext = new HashMap<>();
      itemStatusContext.put(SQL, context.getExtraction().getItemStatusSql());
      itemStatusContext.put(SCHEMA, schema);

      Map<String, Object> noteContext = new HashMap<>();
      noteContext.put(SQL, context.getExtraction().getNoteSql());
      noteContext.put(SCHEMA, schema);

      Map<String, Object> materialTypeContext = new HashMap<>();
      materialTypeContext.put(SQL, context.getExtraction().getMaterialTypeSql());
      materialTypeContext.put(SCHEMA, schema);

      String itemRLTypeId = job.getReferences().get(ITEM_REFERENCE_ID);
      String itemToHoldingRLTypeId = job.getReferences().get(ITEM_TO_HOLDING_REFERENCE_ID);

      ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

      int count = 0;

      try (
          PrintWriter itemWriter = new PrintWriter(new PGCopyOutputStream(threadConnections.getItemConnection(), String.format(ITEM_COPY_SQL, tenant)), true);

          Statement pageStatement = threadConnections.getPageConnection().createStatement();
          Statement mfhdItemStatement = threadConnections.getMfhdConnection().createStatement();
          Statement barcodeStatement = threadConnections.getBarcodeConnection().createStatement();
          Statement itemStatusStatement = threadConnections.getItemStatusConnection().createStatement();
          Statement noteStatement = threadConnections.getNoteConnection().createStatement();
          Statement materialTypeStatement = threadConnections.getMaterialTypeConnection().createStatement();

          ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);
      ) {

        while (pageResultSet.next()) {
          String itemId = pageResultSet.getString(ITEM_ID);

          String voyagerPermTypeId = pageResultSet.getString(PERM_ITEM_TYPE_ID);
          String voyagerPermLocationId = pageResultSet.getString(PERM_LOCATION_ID);

          String voyagerTempTypeId = pageResultSet.getString(TEMP_TYPE_ID);
          String voyagerTempLocationId = pageResultSet.getString(TEMP_LOCATION_ID);

          int numberOfPieces = pageResultSet.getInt(PIECES);
          String spineLabel = pageResultSet.getString(SPINE_LABEL);

          String permLoanTypeId, permLocationId;

          if (loanTypesMap.containsKey(voyagerPermTypeId)) {
            permLoanTypeId = loanTypesMap.get(voyagerPermTypeId);
          } else {
            log.warn("using default permanent loan type for schema {} itemId {} type {}", schema, itemId, voyagerPermTypeId);
            permLoanTypeId = defaults.getPermanentLoanTypeId();
          }

          if (locationsMap.containsKey(voyagerPermLocationId)) {
            permLocationId = locationsMap.get(voyagerPermLocationId);
          } else {
            log.warn("using default permanent location for schema {} itemId {} location {}", schema, itemId, voyagerPermLocationId);
            permLocationId = defaults.getPermanentLocationId();
          }

          mfhdContext.put(ITEM_ID, itemId);
          barcodeContext.put(ITEM_ID, itemId);
          itemStatusContext.put(ITEM_ID, itemId);
          noteContext.put(ITEM_ID, itemId);
          materialTypeContext.put(ITEM_ID, itemId);

          try {
            MfhdItem mfhdItem = getMfhdItem(mfhdItemStatement, mfhdContext);
            String barcode = getItemBarcode(barcodeStatement, barcodeContext);
            List<ItemStatusRecord> itemStatuses = getItemStatuses(itemStatusStatement, itemStatusContext, context.getMaps().getItemStatus(), context.getMaps().getStatusName());
            ItemNoteWrapper noteWrapper = getNotes(noteStatement, noteContext, job.getItemNoteTypeId());
            Materialtypes materialtypes = (Materialtypes) partitionContext.get(MATERIAL_TYPES);
            String materialTypeId = getMaterialTypeId(materialTypeStatement, materialTypeContext, defaults.getMaterialTypeId(), materialtypes);

            ItemRecord itemRecord = new ItemRecord(itemId, barcode, mfhdItem.getCaption(), mfhdItem.getItemEnum(), mfhdItem.getChron(), mfhdItem.getFreetext(), mfhdItem.getYear(), numberOfPieces, spineLabel, materialTypeId, job.getItemNoteTypeId(), job.getItemDamagedStatusId(), itemStatuses, noteWrapper.getNotes(), noteWrapper.getCirculationNotes());

            itemRecord.setPermanentLoanTypeId(permLoanTypeId);
            itemRecord.setPermanentLocationId(permLocationId);

            if (loanTypesMap.containsKey(voyagerTempTypeId)) {
              itemRecord.setTemporaryLoanTypeId(loanTypesMap.get(voyagerTempTypeId));
            }

            if (locationsMap.containsKey(voyagerTempLocationId)) {
              itemRecord.setTemporaryLocationId(locationsMap.get(voyagerTempLocationId));
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

            Statisticalcodes statisticalcodes = (Statisticalcodes) partitionContext.get(STATISTICAL_CODES);

            Item item = itemRecord.toItem(hridPrefix, hrid, statisticalcodes, materialtypes);

            String iUtf8Json = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(item)));

            // (id,jsonb,creation_date,created_by,holdingsrecordid,permanentloantypeid,temporaryloantypeid,materialtypeid,permanentlocationid,temporarylocationid,effectivelocationid)
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
    threadConnections.setItemStatusConnection(getConnection(voyagerSettings));
    threadConnections.setMfhdConnection(getConnection(voyagerSettings));
    threadConnections.setNoteConnection(getConnection(voyagerSettings));
    threadConnections.setMaterialTypeConnection(getConnection(voyagerSettings));
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
    private Connection itemStatusConnection;
    private Connection noteConnection;
    private Connection materialTypeConnection;

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

    public Connection getItemStatusConnection() {
      return itemStatusConnection;
    }

    public void setItemStatusConnection(Connection itemStatusConnection) {
      this.itemStatusConnection = itemStatusConnection;
    }

    public Connection getNoteConnection() {
      return noteConnection;
    }

    public void setNoteConnection(Connection noteConnection) {
      this.noteConnection = noteConnection;
    }

    public Connection getMaterialTypeConnection() {
      return materialTypeConnection;
    }

    public void setMaterialTypeConnection(Connection materialTypeConnection) {
      this.materialTypeConnection = materialTypeConnection;
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
        itemStatusConnection.close();
        itemConnection.close();
        noteConnection.close();
        materialTypeConnection.close();
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
        String caption = resultSet.getString(CAPTION);
        String chron = resultSet.getString(CHRON);
        String itemEnum = resultSet.getString(ITEM_ENUM);
        String freetext = resultSet.getString(FREETEXT);
        String year = resultSet.getString(YEAR);

        mfhdItem = new MfhdItem(caption, chron, itemEnum, freetext, year);
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
    try (
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
            idToUuid.put(id, loanType.get().getId());
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

  private List<ItemStatusRecord> getItemStatuses(Statement statement, Map<String, Object> context, Map<String, String> itemStatusMap, Map<String, String> statusNameMap) throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      List<ItemStatusRecord> statuses = new ArrayList<>();
      while (resultSet.next()) {
        String itemStatus = statusNameMap.get(resultSet.getString(ITEM_STATUS));
        String itemStatusDate = resultSet.getString(ITEM_STATUS_DATE);
        String circtrans = resultSet.getString(CIRCTRANS);
        String itemStatusDesc = itemStatusMap.get(resultSet.getString(ITEM_STATUS_DESC));
        statuses.add(new ItemStatusRecord(itemStatus, itemStatusDate, circtrans, itemStatusDesc));
      }
      Collections.sort(statuses, new Comparator<ItemStatusRecord>() {

        @Override
        public int compare(ItemStatusRecord is1, ItemStatusRecord is2) {
          return Integer.parseInt(is1.getItemStatusDesc()) - Integer.parseInt(is2.getItemStatusDesc());
        }

      });
      return statuses;
    }
  }

  private ItemNoteWrapper getNotes(Statement statement, Map<String, Object> context, String itemNoteTypeId) throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      List<Note__1> notes = new ArrayList<>();
      List<CirculationNote> circulationNotes = new ArrayList<>();
      while (resultSet.next()) {
        String itemNote = resultSet.getString(ITEM_NOTE);
        String itemNoteType = resultSet.getString(ITEM_NOTE_TYPE);
        itemNote = StringUtils.chomp(itemNote);
        itemNote = StringUtils.normalizeSpace(itemNote);
        itemNote.replaceAll("[^\\p{ASCII}]", "");
        itemNote = itemNote.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        itemNote = itemNote.replaceAll("\\p{C}", "");

        if (itemNoteType.equals("1")) {
          Note__1 note = new Note__1();
          note.setNote(itemNote);
          note.setItemNoteTypeId(itemNoteTypeId);
          note.setStaffOnly(true);
          notes.add(note);
        } else {
          CirculationNote circulationNote = new CirculationNote();
          circulationNote.setNote(itemNote);
          circulationNote.setStaffOnly(true);
          if (itemNoteType.equals("2")) {
            circulationNote.setNoteType(NoteType.CHECK_OUT);
            circulationNotes.add(circulationNote);
          } else if (itemNoteType.equals("3")) {
            circulationNote.setNoteType(NoteType.CHECK_IN);
            circulationNotes.add(circulationNote);
          }
        }

      }
      return new ItemNoteWrapper(notes, circulationNotes);
    }
  }

  private String getMaterialTypeId(Statement statement, Map<String, Object> context, String defaultMaterialTypeId, Materialtypes materialtypes) throws SQLException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      String materialType = defaultMaterialTypeId;
      while (resultSet.next()) {
        String materialTypeCode = resultSet.getString(MTYPE_CODE);
        Optional<Materialtype> potentialMaterialType = materialtypes.getMtypes().stream().filter(mt -> mt.getSource().equals(materialTypeCode)).findFirst();
        if (potentialMaterialType.isPresent()) {
          materialType = potentialMaterialType.get().getId();
        }
      }
      return materialType;
    }
  }

  private class MfhdItem {

    private final String caption;
    private final String chron;
    private final String itemEnum;
    private final String freetext;
    private final String year;

    public MfhdItem(String caption, String chron, String itemEnum, String freetext, String year) {
      this.caption = caption;
      this.chron = chron;
      this.itemEnum = itemEnum;
      this.freetext = freetext;
      this.year = year;
    }

    public String getCaption() {
      return caption;
    }

    public String getChron() {
      return chron;
    }

    public String getItemEnum() {
      return itemEnum;
    }

    public String getFreetext() {
      return freetext;
    }

    public String getYear() {
      return year;
    }

  }

  private class ItemNoteWrapper {

    private final List<Note__1> notes;
    private final List<CirculationNote> circulationNotes;

    public ItemNoteWrapper(List<Note__1> notes, List<CirculationNote> circulationNotes) {
      this.notes = notes;
      this.circulationNotes = circulationNotes;
    }

    public List<CirculationNote> getCirculationNotes() {
      return circulationNotes;
    }

    public List<Note__1> getNotes() {
      return notes;
    }
  }

}
