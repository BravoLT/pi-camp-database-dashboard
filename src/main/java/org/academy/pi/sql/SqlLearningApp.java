package org.academy.pi.sql;

import java.sql.*;
import java.util.Scanner;

public class SqlLearningApp {

  private final SqlController sqlController;

  public SqlLearningApp() {
    this.sqlController = new SqlController();
  }

  /**
   * Execute a SQL query and display results
   */
  public void executeQuery(String sql) {
    try (
        Connection conn = sqlController.getConnection();
        Statement stmt = conn.createStatement()
    ) {
      if (sql.trim().toUpperCase().startsWith("SELECT")) {
        // Execute SELECT query
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Print column headers
        System.out.println("\n" + "=".repeat(80));
        for (int i = 1; i <= columnCount; i++) {
          System.out.printf("%-15s", metaData.getColumnName(i));
        }
        System.out.println("\n" + "-".repeat(80));

        // Print rows
        while (rs.next()) {
          for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-15s", rs.getString(i));
          }
          System.out.println();
        }
        System.out.println("=".repeat(80));
      } else {
        // Execute UPDATE, INSERT, DELETE, etc.
        int rowsAffected = stmt.executeUpdate(sql);
        System.out.println("✓ Query executed successfully! Rows affected: " + rowsAffected);
      }
    } catch (SQLException e) {
      System.err.println("SQL Error: " + e.getMessage());
    }
  }

  /**
   * Display helpful SQL examples for students
   */
  public void showExamples() {
    System.out.println("\n📚 SQL Examples to Try:");
    System.out.println("=".repeat(50));

    System.out.println("\n1. Basic SELECT:");
    System.out.println("   SELECT * FROM students;");
    System.out.println("   SELECT name, age FROM students;");

    System.out.println("\n2. WHERE clause:");
    System.out.println("   SELECT * FROM students WHERE age > 14;");
    System.out.println("   SELECT * FROM books WHERE price < 20;");

    System.out.println("\n3. ORDER BY:");
    System.out.println("   SELECT * FROM students ORDER BY age;");
    System.out.println("   SELECT * FROM books ORDER BY price DESC;");

    System.out.println("\n4. COUNT and GROUP BY:");
    System.out.println("   SELECT grade, COUNT(*) FROM students GROUP BY grade;");
    System.out.println("   SELECT genre, AVG(price) FROM books GROUP BY genre;");

    System.out.println("\n5. JOINs:");
    System.out.println("   SELECT s.name, b.title FROM students s");
    System.out.println("   JOIN orders o ON s.id = o.student_id");
    System.out.println("   JOIN books b ON o.book_id = b.id;");

    System.out.println("\n6. INSERT new data:");
    System.out.println("   INSERT INTO students (name, age, grade, email)");
    System.out.println("   VALUES ('Your Name', 15, 10, 'you@school.edu');");

    System.out.println("\n7. UPDATE data:");
    System.out.println("   UPDATE books SET price = 19.99 WHERE id = 1;");

    System.out.println("=".repeat(50));
  }

  /**
   * Interactive SQL console for students
   */
  public void startInteractiveMode() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("\n🎓 Welcome to SQL Learning Console!");
    System.out.println("Type 'help' for examples, 'quit' to exit");
    System.out.println("-".repeat(50));

    while (true) {
      System.out.print("\nSQL> ");
      String input = scanner.nextLine().trim();

      if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
        System.out.println("👋 Happy learning! Goodbye!");
        break;
      } else if (input.equalsIgnoreCase("help")) {
        showExamples();
      } else if (!input.isEmpty()) {
        executeQuery(input);
      }
    }

    scanner.close();
  }

  /**
   * Main method - entry point
   */
  public static void main(String[] args) {
    System.out.println("🚀 Starting SQL Learning Application...");

    SqlLearningApp app = new SqlLearningApp();
    try {
      app.sqlController.start();
    } catch (Exception e) {
      System.err.println("Could not start API server: " + e.getMessage());
    }

    // Start interactive mode
    app.startInteractiveMode();

    // Cleanup
    if (app.sqlController != null) {
      app.sqlController.stop();
    }
  }
}