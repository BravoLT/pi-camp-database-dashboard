package org.academy.pi.sql.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.academy.pi.sql.models.SqlNamedQuery;

public class StudentDataRepo {

  private static final List<SqlNamedQuery> SAMPLE_QUERIES = List.of(
      SqlNamedQuery.builder()
          .title("Find All Students")
          .query("SELECT * FROM students;")
          .build(),
      SqlNamedQuery.builder()
          .title("Find Students by Grade")
          .query("SELECT name, age FROM students WHERE grade = 7;")
          .build(),
      SqlNamedQuery.builder()
          .title("Count Students")
          .query("SELECT COUNT(*) as total_students FROM students;")
          .build(),
      SqlNamedQuery.builder()
          .title("Order Students by Age")
          .query("SELECT * FROM students ORDER BY age DESC;")
          .build(),
      SqlNamedQuery.builder()
          .title("Insert 1")
          .query("INSERT INTO students (name, age, grade) VALUES (?, ?, ?);")
          .build(),
      SqlNamedQuery.builder()
          .title("Insert 2")
          .query("""
          INSERT INTO students
            (name, age, grade)
          VALUES
            (?, ?, ?),
            (?, ?, ?);
          """)
          .build()
  );

  private final RootDataRepo rootDataRepo;

  public StudentDataRepo(final RootDataRepo rootDataRepo) {
    this.rootDataRepo = rootDataRepo;
    initializeTable();
  }

  public List<String> getTableNames() {
    return List.of("students");
  }

  public List<SqlNamedQuery> getSampleQueries() {
    return SAMPLE_QUERIES;
  }

  private void initializeTable() {
    try (Connection conn = rootDataRepo.getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute("DROP TABLE IF EXISTS students");

      String createTableSQL = rootDataRepo.loadSqlFromFile("/sql/students-create.sql");
      stmt.execute(createTableSQL);

      String insertRecordsSQL = rootDataRepo.loadSqlFromFile("/sql/students-data.sql");
      stmt.execute(insertRecordsSQL);

      System.out.println("✓ Student table created successfully!");
    } catch (IOException | SQLException e) {
      System.err.println("Error creating student table: " + e.getMessage());
      e.printStackTrace();
    }
  }
}