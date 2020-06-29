package org.folio.rest.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.dto.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.dto.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.dto.JobExecution;
import org.folio.rest.jaxrs.model.dto.InitJobExecutionsRqDto.SourceType;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.ItemContext;
import org.folio.rest.migration.model.request.ItemJob;
import org.folio.rest.migration.service.MigrationService;
import org.h2.expression.function.ToChar.Capitalization;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

public class ItemMigration extends AbstractMigration<ItemContext> {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String COPY_NUMBER = "COPY_NUMBER";
    private static final String PERM_ITEM_TYPE_ID = "ITEM_TYPE_ID";
    private static final String MEDIA_TYPE_ID = "MEDIA_TYPE_ID";
    private static final String PERM_LOCATION_ID = "PERM_LOCATION";
    private static final String PIECES = "PIECES";
    private static final String PRICE = "PRICE";
    private static final String SPINE_LABEL = "SPINE_LABEL";
    private static final String TEMP_LOCATION_ID = "TEMP_LOCATION";
    private static final String TEMP_TYPE_ID = "TEMP_ITEM_TYPE_ID";
    private static final String MAGNETIC_MEDIA = "MAGNETIC_MEDIA";
    private static final String SENSITIZE = "SENSITIZE";

    private static final String CAPTION = "CAPTION";
    private static final String CHRON = "CHRON";
    private static final String ITEM_ENUM = "ITEM_ENUM";
    private static final String FREETEXT = "FREETEXT";
    private static final String YEAR = "YEAR";

    private static final String ITEM_BARCODE = "ITEM_BARCODE";

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

            InitJobExecutionsRqDto jobExecutionRqDto = new InitJobExecutionsRqDto();
            jobExecutionRqDto.setSourceType(SourceType.ONLINE);
            jobExecutionRqDto.setJobProfileInfo(job.getProfileInfo());
            jobExecutionRqDto.setUserId(job.getUserId());

            InitJobExecutionsRsDto JobExecutionRsDto = migrationService.okapiService.createJobExecution(tenant, token, jobExecutionRqDto);
            JobExecution jobExecution = JobExecutionRsDto.getJobExecutions().get(0);

            String jobExecutionId = jobExecution.getId();

            Database voyagerSettings = context.getExtraction().getDatabase();

            Database folioSettings = migrationService.okapiService.okapi.getModules().getDatabase();

            JsonStringEncoder jsonStringEncoder = new JsonStringEncoder();

            Map<String, Object> mfhdContext = new HashMap<>();
            mfhdContext.put(SQL, context.getExtraction().getMfhdSql());
            mfhdContext.put(SCHEMA, schema);

            Map<String, Object> barcodeContext = new HashMap<>();
            barcodeContext.put(SQL, context.getExtraction().getBarcodeSql());
            barcodeContext.put(SCHEMA, schema);

            ThreadConnections threadConnections = getThreadConnections(voyagerSettings, folioSettings);

            int count = 0;

            try {
                PGCopyOutputStream rawRecordOutput = new PGCopyOutputStream(threadConnections.getItemConnection(), String.format(ITEM_COPY_SQL, tenant));
                PrintWriter rawRecordWriter = new PrintWriter(rawRecordOutput, true);

                Statement pageStatement = threadConnections.getPageConnection().createStatement();
                Statement mfhdItemStatement = threadConnections.getMfhdConnection().createStatement();
                Statement barcodeStatement = threadConnections.getBarcodeConnection().createStatement();

                ResultSet pageResultSet = getResultSet(pageStatement, partitionContext);

                while (pageResultSet.next()) {

                    String itemId = pageResultSet.getString(ITEM_ID);
                    String copyNumber = pageResultSet.getString(COPY_NUMBER);
                    String permTypeId = pageResultSet.getString(PERM_ITEM_TYPE_ID);
                    String mediaTypeId = pageResultSet.getString(MEDIA_TYPE_ID);
                    String permLocation = pageResultSet.getString(PERM_LOCATION_ID);
                    int pieces = pageResultSet.getInt(PIECES);
                    int price = pageResultSet.getInt(PRICE);
                    String spineLabel = pageResultSet.getString(SPINE_LABEL);
                    String tempLocation = pageResultSet.getString(TEMP_LOCATION_ID);
                    String tempTypeId = pageResultSet.getString(TEMP_TYPE_ID);
                    String magneticMedia = pageResultSet.getString(MAGNETIC_MEDIA);
                    String sensitize = pageResultSet.getString(SENSITIZE);

                    mfhdContext.put(ITEM_ID, itemId);
                    barcodeContext.put(ITEM_ID, itemId);

                    // TODO: Reference links

                    try {
                        MfhdItem mfhdItem = getMfhdItem(mfhdItemStatement, mfhdContext);
                        String barcode = getItemBarcode(barcodeStatement, barcodeContext);
                    } catch (SQLException e) {

                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return this;
        }

    }

    private ThreadConnections getThreadConnections(Database voyagerSettings, Database folioSettings) {
        ThreadConnections threadConnections = new ThreadConnections();
        threadConnections.setPageConnection(getConnection(voyagerSettings));
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
    }

    private class MfhdItem {
        private String caption;
        private String chron;
        private String itemEnum;
        private String freetext;
        private String year;

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
}