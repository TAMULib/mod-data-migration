package org.folio.rest.migration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.AbstractContext;
import org.marc4j.MarcException;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMigration<C extends AbstractContext> implements Migration {

  private static final String RECORD_SEGMENT = "RECORD_SEGMENT";
  private static final String SEQNUM = "SEQNUM";

  static final DateTimeFormatter DATE_TIME_FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXX");

  static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  static final String SQL = "SQL";
  static final String JOB = "JOB";
  static final String SCHEMA = "SCHEMA";
  static final String OFFSET = "OFFSET";
  static final String LIMIT = "LIMIT";

  static final String TENANT = "TENANT";
  static final String TOKEN = "TOKEN";
  static final String INDEX = "INDEX";
  static final String TOTAL = "TOTAL";

  static final String PREFIX = "prefix";
  static final String START_NUMBER = "startNumber";

  static final String NULL = "null";

  static final String HRID_TEMPLATE = "%s%011d";

  static final String IN_PROGRESS_RESPONSE_MESSAGE = "Migration in progress";

  final Logger log = LoggerFactory.getLogger(this.getClass());

  final C context;

  final String tenant;

  PartitionTaskQueue<C> taskQueue;

  public AbstractMigration(C context, String tenant) {
    this.context = context;
    this.tenant = tenant;
  }

  void preActions(Database settings, List<String> preActions) {
    preActions.stream().forEach(actionSqlTemplate -> action(settings, actionSqlTemplate));
  }

  void postActions(Database settings, List<String> postActions) {
    postActions.stream().forEach(actionSqlTemplate -> action(settings, actionSqlTemplate));
  }

  void action(Database settings, String actionSqlTemplate) {
    Map<String, Object> actionContext = new HashMap<>();
    actionContext.put(TENANT, tenant);
    try (Connection connection = getConnection(settings); Statement statement = connection.createStatement();) {
      String actionSql = templateSql(actionSqlTemplate, actionContext);
      log.info(actionSql);
      statement.execute(actionSql);
    } catch (SQLException e) {
      log.error(e.getMessage());
    }
  }

  Connection getConnection(Database settings) {
    try {
      Class.forName(settings.getDriverClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    try {
      return DriverManager.getConnection(settings.getUrl(), settings.getUsername(), settings.getPassword());
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  int getCount(Database settings, Map<String, Object> countContext) {
    try (Connection connection = getConnection(settings);
        Statement statement = connection.createStatement();
        ResultSet resultSet = getResultSet(statement, countContext);) {
      return resultSet.next() ? Integer.parseInt(resultSet.getBigDecimal(TOTAL).toString()) : 0;
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  ResultSet getResultSet(Statement statement, Map<String, Object> context) throws SQLException {
    String sql = templateSql((String) context.get(SQL), context);
    return statement.executeQuery(sql);
  }

  String templateSql(String template, Map<String, Object> context) {
    StringSubstitutor sub = new StringSubstitutor(context);
    return sub.replace(template);
  }

  String getMarc(Statement statement, Map<String, Object> context) throws SQLException, IOException {
    try (ResultSet resultSet = getResultSet(statement, context)) {
      List<SequencedMarc> marcSequence = new ArrayList<>();
      while (resultSet.next()) {
        InputStream recordSegment = resultSet.getBinaryStream(RECORD_SEGMENT);
        int seqnum = resultSet.getInt(SEQNUM);
        marcSequence.add(new SequencedMarc(seqnum, recordSegment));
      }
      List<InputStream> asciiStreams = marcSequence.stream()
          .sorted((sm1, sm2) -> sm1.getSeqnum().compareTo(sm2.getSeqnum())).map(sm -> sm.getRecordSegment())
          .collect(Collectors.toList());
      SequenceInputStream sequenceInputStream = new SequenceInputStream(Collections.enumeration(asciiStreams));
      return IOUtils.toString(sequenceInputStream, DEFAULT_CHARSET);
    }
  }

  Optional<Record> rawMarcToRecord(String rawMarc) throws IOException, MarcException {
    StringBuilder marc = new StringBuilder(rawMarc);
    // leader/22 must be 0 to avoid missing field terminator at end of directory
    marc.setCharAt(22, '0');
    try (InputStream in = new ByteArrayInputStream(marc.toString().getBytes(DEFAULT_CHARSET))) {
      MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    }
    return Optional.empty();
  }

  private class SequencedMarc {
    private Integer seqnum;
    private InputStream recordSegment;

    public SequencedMarc(Integer seqnum, InputStream recordSegment) {
      this.seqnum = seqnum;
      this.recordSegment = recordSegment;
    }

    public Integer getSeqnum() {
      return seqnum;
    }

    public InputStream getRecordSegment() {
      return recordSegment;
    }

  }

}
