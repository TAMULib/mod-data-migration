package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.Loantype;
import org.folio.rest.jaxrs.model.Loantypes;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Locations;
import org.folio.rest.jaxrs.model.Status.Name;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.Item;
import org.folio.rest.migration.model.request.ItemContext;
import org.folio.rest.migration.model.request.ItemJob;
import org.folio.rest.migration.model.request.MfhdItem;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.utility.TimingUtility;
import org.folio.rest.model.ReferenceLink;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class ItemMigration extends AbstractMigration<ItemContext> {

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

    // (id,jsonb,creation_date,created_by,holdingsrecordid,permanentloantypeid,temporaryloantypeid,meterialtypeid,permanentlocationid,temporarylocationid,effectivelocationid)
    private static String ITEM_COPY_SQL = "COPY %s_mod_inventory_storage.item (id,jsonb,creation_date,created_by) FROM STDIN";

    private ItemMigration(ItemContext context, String tenant) {
        super(context, tenant);
    }

    @Override
    public CompletableFuture<Boolean> run(MigrationService migrationService) {
        log.info("tenant: {}", tenant);

        log.info("context:\n{}", migrationService.objectMapper.convertValue(context, JsonNode.class).toPrettyString());

        String token = migrationService.okapiService.getToken(tenant);

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

        int index = 0;

        for (ItemJob job : context.getJobs()) {

            countContext.put(SCHEMA, job.getSchema());

            int count = getCount(voyagerSettings, countContext);

            log.info("{} count: {}", job.getSchema(), count);

            int partitions = job.getPartitions();
            int limit = count / partitions;
            if (limit * partitions < count) {
                limit++;
            }
            int offset = 0;
            for (int i = 0; i < partitions; i++) {
                Map<String, Object> partitionContext = new HashMap<String, Object>();
                partitionContext.put(SQL, context.getExtraction().getPageSql());
                partitionContext.put(SCHEMA, job.getSchema());
                partitionContext.put(OFFSET, offset);
                partitionContext.put(LIMIT, limit);
                partitionContext.put(INDEX, index);
                partitionContext.put(TOKEN, token);
                taskQueue.submit(new ItemPartitionTask(migrationService, partitionContext, job));
                offset += limit;
                index++;
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

        private final ItemJob job;

        public ItemPartitionTask(MigrationService migrationService, Map<String, Object> partitionContext, ItemJob job) {
            this.migrationService = migrationService;
            this.partitionContext = partitionContext;
            this.job = job;
        }

        public int getIndex() {
            return (int) partitionContext.get(INDEX);
        }

        public String getSchema() {
            return job.getSchema();
        }

        @Override
        public PartitionTask<ItemContext> execute(ItemContext context) {
            long startTime = System.nanoTime();

            String schema = this.getSchema();

            int index = this.getIndex();

            String token = (String) partitionContext.get(TOKEN);

            Database voyagerSettings = context.getExtraction().getDatabase();

            Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

            JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

            Map<String, Object> mfhdContext = new HashMap<>();
            mfhdContext.put(SQL, context.getExtraction().getMfhdSql());
            mfhdContext.put(SCHEMA, schema);

            Map<String, Object> barcodeContext = new HashMap<>();
            barcodeContext.put(SQL, context.getExtraction().getBarcodeSql());
            barcodeContext.put(SCHEMA, schema);

            Map<String, Object> itemTypeContext = new HashMap<>();
            itemTypeContext.put(SQL, context.getExtraction().getItemTypeSql());
            itemTypeContext.put(SCHEMA, schema);

            Map<String, Object> locationContext = new HashMap<>();
            locationContext.put(SQL, context.getExtraction().getLocationSql());
            locationContext.put(SCHEMA, schema);

            ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

            int count = 0;

            try {
                PGCopyOutputStream itemOutput = new PGCopyOutputStream(threadConnections.getItemConnection(),
                        String.format(ITEM_COPY_SQL, tenant));
                PrintWriter itemWriter = new PrintWriter(itemOutput, true);

                Statement pageStatement = threadConnections.getPageConnection().createStatement();
                Statement mfhdItemStatement = threadConnections.getMfhdConnection().createStatement();
                Statement barcodeStatement = threadConnections.getBarcodeConnection().createStatement();
                Statement locationStatement = threadConnections.getLocationConnection().createStatement();
                Statement itemTypeStatement = threadConnections.getItemTypeConnection().createStatement();

                ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

                Map<String, String> voyagerItemTypes = buildVoyagerItemTypeMap(itemTypeStatement, itemTypeContext);
                Map<String, String> voyagerLocations = buildVoyagerLocationMap(locationStatement, locationContext);

                Map<String, Loantype> folioLoantypes = buildLoanTypeMap(
                        this.migrationService.okapiService.fetchLoanTypes(tenant, token));
                Map<String, Location> folioLocations = buildLocationMap(
                        this.migrationService.okapiService.fetchLocations(tenant, token));

                Map<String, Loantype> loanTypeMap = voyagerItemTypes.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> folioLoantypes.get(e.getValue())));
                Map<String, Location> locationMap = voyagerLocations.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> folioLocations.get(e.getValue())));

                while (pageResultSet.next()) {

                    String permTypeId = pageResultSet.getString(PERM_ITEM_TYPE_ID);
                    String permLocationId = pageResultSet.getString(PERM_LOCATION_ID);
                    String tempLocationId = pageResultSet.getString(TEMP_LOCATION_ID);
                    String tempTypeId = pageResultSet.getString(TEMP_TYPE_ID);

                    String itemId = pageResultSet.getString(ITEM_ID);
                    Loantype permLoanType = loanTypeMap.get(permTypeId);
                    Location permLocation = locationMap.get(permLocationId);
                    int pieces = pageResultSet.getInt(PIECES);
                    Location tempLocation = locationMap.get(tempLocationId);
                    Loantype tempLoantype = loanTypeMap.get(tempTypeId);

                    mfhdContext.put(ITEM_ID, itemId);
                    barcodeContext.put(ITEM_ID, itemId);

                    try {
                        MfhdItem mfhdItem = getMfhdItem(mfhdItemStatement, mfhdContext);
                        String barcode = getItemBarcode(barcodeStatement, barcodeContext);

                        String createdAt = DATE_TIME_FOMATTER.format(OffsetDateTime.now());
                        String createdByUserId = job.getUserId();

                        String itemRLTypeId = job.getItemRLTypeId();
                        Optional<ReferenceLink> itemRL = migrationService.referenceLinkRepo.findByTypeIdAndExternalReference(itemRLTypeId, itemId);
                        String id = itemRL.isPresent() ? itemRL.get().getFolioReference() : UUID.randomUUID().toString();

                        Item item = new Item(id, barcode, mfhdItem, permLocation.getId(), itemId, pieces,
                                job.getMaterialTypeId(), Name.AVAILABLE, permLoanType.getId(), tempLoantype.getId(),
                                tempLocation.getId());

                        String itemJson = new String(jsonStringEncoder.quoteAsUTF8(migrationService.objectMapper.writeValueAsString(item)));

                        itemWriter.println(String.join("\t", item.getId(), itemJson, createdAt, createdByUserId));

                        count++;

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException jsonError) {
                        jsonError.printStackTrace();
                    }

                }

                itemWriter.close();

                pageStatement.close();
                mfhdItemStatement.close();
                barcodeStatement.close();
                locationStatement.close();
                itemTypeStatement.close();

                pageResultSet.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            threadConnections.closeAll();

            log.info("{} {} finished {} in {} miliseconds", schema, index, count, TimingUtility.getDeltaInMilliseconds(startTime));

            return this;
        }

    }

    private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
        ThreadConnections threadConnections = new ThreadConnections();
        threadConnections.setPageConnection(getConnection(voyagerSettings));
        threadConnections.setBarcodeConnection(getConnection(voyagerSettings));
        threadConnections.setMfhdConnection(getConnection(voyagerSettings));
        threadConnections.setItemTypeConnection(getConnection(voyagerSettings));
        threadConnections.setLocationConnection(getConnection(voyagerSettings));
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
        private Connection locationConnection;
        private Connection itemTypeConnection;

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

        public Connection getLocationConnection() {
            return locationConnection;
        }

        public void setLocationConnection(Connection locationConnection) {
            this.locationConnection = locationConnection;
        }

        public Connection getItemTypeConnection() {
            return itemTypeConnection;
        }

        public void setItemTypeConnection(Connection itemTypeConnection) {
            this.itemTypeConnection = itemTypeConnection;
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
                locationConnection.close();
                itemTypeConnection.close();
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

    private Map<String, Loantype> buildLoanTypeMap(Loantypes loanTypes) {
        return loanTypes.getLoantypes().stream()
            .collect(Collectors.toMap(Loantype::getName, Function.identity()));
    }

    private Map<String, Location> buildLocationMap(Locations locations) {
        return locations.getLocations().stream()
            .collect(Collectors.toMap(Location::getCode, Function.identity()));
    }

    private Map<String, String> buildVoyagerItemTypeMap(Statement statement, Map<String, Object> context) {
        Map<String, String> vgrTypeMap = new HashMap<>();
        try (ResultSet resultSet = getResultSet(statement, context)) {
            while (resultSet.next()) {
                String id = resultSet.getString(ITEM_TYPE_ID);
                String code = resultSet.getString(ITEM_TYPE_CODE);
                vgrTypeMap.put(id, code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vgrTypeMap;
    }

    private Map<String, String> buildVoyagerLocationMap(Statement statement, Map<String, Object> context) {
        Map<String, String> vgrLocationMap = new HashMap<>();
        try (ResultSet resultSet = getResultSet(statement, context)) {
            while (resultSet.next()) {
                String id = resultSet.getString(LOCATION_ID);
                String code = resultSet.getString(LOCATION_CODE);
                vgrLocationMap.put(id, code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vgrLocationMap;
    }
}
