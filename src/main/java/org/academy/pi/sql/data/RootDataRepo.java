package org.academy.pi.sql.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

public class RootDataRepo {

  private static final String DB_URL = "jdbc:h2:~/sqllearning;AUTO_SERVER=TRUE";
  private static final String DB_USER = "student";
  private static final String DB_PASSWORD = "learn123";

  private final StudentDataRepo studentDataRepo;

  public RootDataRepo() {
    initializeDatabase();
    studentDataRepo = new StudentDataRepo(this);
  }

  public SqlHealthResult health() {
    try (Connection _conn = getConnection()) {
      return SqlHealthResult.builder()
          .connected(true)
          .message("Green means go!")
          .sampleQueries(studentDataRepo.getSampleQueries())
          .tableNames(studentDataRepo.getTableNames())
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
              if("DATE".equals(metaData.getColumnTypeName(i))){
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

  protected String loadSqlFromFile(String resourcePath) throws IOException {
    try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + resourcePath);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Initialize the H2 database connection
   */
  private void initializeDatabase() {
    try {
      // test connection
      getConnection();

      System.out.println("✓ Database connected successfully!");
      System.out.println("✓ H2 Console available at: http://localhost:8082");
      System.out.println("  - JDBC URL: " + DB_URL);
      System.out.println("  - Username: " + DB_USER);
      System.out.println("  - Password: " + DB_PASSWORD);
    } catch (Exception e) {
      System.err.println("Error connecting to database: " + e.getMessage());
      e.printStackTrace();
    }

    // Start H2 Console Server
    try {
      org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
      System.out.println("✓ H2 Web Console started on port 8082");
    } catch (SQLException e) {
      System.err.println("Could not start H2 Console: " + e.getMessage());
    }
  }
}