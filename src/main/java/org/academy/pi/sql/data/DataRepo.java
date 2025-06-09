package org.academy.pi.sql.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.academy.pi.sql.models.SqlHealthResult;
import org.academy.pi.sql.models.SqlQueryResult;
import org.h2.tools.RunScript;
import org.h2.tools.Server;

public class DataRepo {

  private static final String DB_URL = "jdbc:h2:mem:sqllearning;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
  private static final String DB_USER = "student";
  private static final String DB_PASSWORD = "learn123";

  public DataRepo() {
    initializeDatabase();
  }

  public SqlHealthResult health() {
    try (Connection conn = getConnection()) {
      return SqlHealthResult.builder()
          .connected(conn != null)
          .message("Green means go!")
          .sampleQueries(QueryRepo.getSampleQueries())
          .tableNames(QueryRepo.getTableNames())
          .build();
    } catch (Exception e) {
      return SqlHealthResult.builder()
          .connected(false)
          .message(e.getMessage())
          .sampleQueries(List.of())
          .tableNames(List.of())
          .build();
    }
  }

  public SqlQueryResult executeQuery(String sql) throws SQLException {
    long startTime = System.currentTimeMillis();

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      if (sql.trim().toUpperCase().startsWith("SELECT")) {
        try (ResultSet rs = stmt.executeQuery(sql)) {
          ResultSetMetaData metaData = rs.getMetaData();
          int columnCount = metaData.getColumnCount();

          // Get column names
          List<String> columns = new ArrayList<>();
          for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnName(i));
          }

          // Get rows
          List<List<Object>> rows = new ArrayList<>();
          while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
              if ("DATE".equals(metaData.getColumnTypeName(i))) {
                row.add(rs.getDate(i).toLocalDate().toString());
              } else {
                row.add(rs.getObject(i));
              }
            }
            rows.add(row);
          }

          long executionTime = System.currentTimeMillis() - startTime;
          return SqlQueryResult.builder()
              .columns(columns)
              .rows(rows)
              .count(rows.size())
              .execTimeMs(executionTime)
              .build();
        }
      } else {
        int rowsAffected = stmt.executeUpdate(sql);
        long executionTime = System.currentTimeMillis() - startTime;

        List<String> columns = List.of("rows_affected");
        List<List<Object>> rows = List.of(List.of(rowsAffected));
        return SqlQueryResult.builder()
            .count(rowsAffected)
            .columns(columns)
            .rows(rows)
            .execTimeMs(executionTime)
            .build();
      }
    }
  }

  public Connection getConnection() throws SQLException {
    try {
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    } catch (ClassNotFoundException e) {
      throw new SQLException("H2 Driver not found", e);
    }
  }

  private void initializeDatabase() {

    String initScript = "/sql/initialize-database.sql";

    try (InputStream is = getClass().getResourceAsStream(initScript)) {
      if (is == null) {
        return;
      }
      try (
          Connection conn = getConnection();
          InputStreamReader isr = new InputStreamReader(is)
      ) {
        RunScript.execute(conn, isr);
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        System.out.println("✓ Database initialize!");
        System.out.println("✓ H2 Web Console available at: http://localhost:8082");
        System.out.println("  - JDBC URL: " + DB_URL);
        System.out.println("  - Username: " + DB_USER);
        System.out.println("  - Password: " + DB_PASSWORD);
      }
    } catch (Exception e) {
      System.err.println("initializeDatabase Error! " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
}