package org.folio.rest.migration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.model.request.AbstractContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMigration<C extends AbstractContext> implements Migration {

  final Logger log = LoggerFactory.getLogger(this.getClass());

  static DateTimeFormatter DATE_TIME_FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXX");

  static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  static String SQL = "SQL";
  static String SCHEMA = "SCHEMA";
  static String OFFSET = "OFFSET";
  static String LIMIT = "LIMIT";

  static String TOKEN = "TOKEN";

  static String INDEX = "INDEX";

  static String TOTAL = "TOTAL";

  PartitionTaskQueue<C> taskQueue;

  public AbstractMigration() {

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
    try (

        Connection connection = getConnection(settings);
        Statement statement = connection.createStatement();
        ResultSet resultSet = getResultSet(statement, countContext);

    ) {
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

}
