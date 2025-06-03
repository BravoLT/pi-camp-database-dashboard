package org.academy.pi;

import java.sql.*;
import java.util.Scanner;
import org.academy.pi.sql.SqlController;

public class DbLearningApp {

  // H2 Database configuration
  private static final String DB_URL = "jdbc:h2:~/sqllearning;AUTO_SERVER=TRUE";
  private static final String DB_USER = "student";
  private static final String DB_PASSWORD = "learn123";

  private Connection connection;

  public DbLearningApp() {
    initializeDatabase();
    createSampleTables();
    insertSampleData();
  }

  /**
   * Initialize the H2 database connection
   */
  private void initializeDatabase() {
    try {
      // Load H2 driver
      Class.forName("org.h2.Driver");

      // Create connection
      connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
      System.out.println("✓ Database connected successfully!");
      System.out.println("✓ H2 Console available at: http://localhost:8082");
      System.out.println("  - JDBC URL: " + DB_URL);
      System.out.println("  - Username: " + DB_USER);
      System.out.println("  - Password: " + DB_PASSWORD);
    } catch (Exception e) {
      System.err.println("Error connecting to database: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Create sample tables for students to practice with
   */
  private void createSampleTables() {
    try {
      Statement stmt = connection.createStatement();

      // Drop tables if they exist (for fresh start)
      stmt.execute("DROP TABLE IF EXISTS orders");
      stmt.execute("DROP TABLE IF EXISTS students");
      stmt.execute("DROP TABLE IF EXISTS books");

      // Create Students table
      stmt.execute("""
              CREATE TABLE students (
                  id INT PRIMARY KEY AUTO_INCREMENT,
                  name VARCHAR(100) NOT NULL,
                  age INT NOT NULL,
                  grade INT NOT NULL,
                  email VARCHAR(100),
                  enrollment_date DATE DEFAULT CURRENT_DATE
              )
          """);

      // Create Books table
      stmt.execute("""
              CREATE TABLE books (
                  id INT PRIMARY KEY AUTO_INCREMENT,
                  title VARCHAR(200) NOT NULL,
                  author VARCHAR(100) NOT NULL,
                  genre VARCHAR(50),
                  price DECIMAL(10,2),
                  publication_year INT,
                  available BOOLEAN DEFAULT TRUE
              )
          """);

      // Create Orders table (relates students to books)
      stmt.execute("""
              CREATE TABLE orders (
                  id INT PRIMARY KEY AUTO_INCREMENT,
                  student_id INT,
                  book_id INT,
                  order_date DATE DEFAULT CURRENT_DATE,
                  quantity INT DEFAULT 1,
                  FOREIGN KEY (student_id) REFERENCES students(id),
                  FOREIGN KEY (book_id) REFERENCES books(id)
              )
          """);

      System.out.println("✓ Sample tables created successfully!");
    } catch (SQLException e) {
      System.err.println("Error creating tables: " + e.getMessage());
    }
  }

  /**
   * Insert sample data for students to practice queries
   */
  private void insertSampleData() {
    try {
      Statement stmt = connection.createStatement();

      // Insert sample students
      stmt.execute("""
              INSERT INTO students (name, age, grade, email) VALUES
              ('Alice Johnson', 13, 8, 'alice.j@school.edu'),
              ('Bob Smith', 15, 10, 'bob.s@school.edu'),
              ('Charlie Brown', 12, 7, 'charlie.b@school.edu'),
              ('Diana Prince', 16, 11, 'diana.p@school.edu'),
              ('Eve Wilson', 14, 9, 'eve.w@school.edu'),
              ('Frank Miller', 13, 8, 'frank.m@school.edu'),
              ('Grace Lee', 17, 12, 'grace.l@school.edu'),
              ('Henry Davis', 15, 10, 'henry.d@school.edu')
          """);

      // Insert sample books
      stmt.execute("""
              INSERT INTO books (title, author, genre, price, publication_year) VALUES
              ('The Great Adventure', 'Jane Author', 'Fiction', 12.99, 2020),
              ('Math Made Easy', 'Prof. Numbers', 'Education', 24.50, 2021),
              ('Science Wonders', 'Dr. Lab', 'Science', 18.75, 2019),
              ('History Heroes', 'Time Keeper', 'History', 15.99, 2022),
              ('Art and Creativity', 'Brush Master', 'Art', 22.00, 2020),
              ('Coding for Kids', 'Tech Guru', 'Technology', 29.99, 2023),
              ('Mystery Island', 'Secret Writer', 'Mystery', 13.50, 2021),
              ('Space Explorers', 'Astro Naut', 'Science Fiction', 16.25, 2022)
          """);

      // Insert sample orders
      stmt.execute("""
              INSERT INTO orders (student_id, book_id, quantity) VALUES
              (1, 1, 1), (1, 3, 1),
              (2, 2, 1), (2, 6, 1),
              (3, 1, 2), (3, 7, 1),
              (4, 4, 1), (4, 5, 1),
              (5, 2, 1), (5, 8, 1),
              (6, 6, 1), (7, 3, 1),
              (8, 4, 1), (8, 7, 1)
          """);

      System.out.println("✓ Sample data inserted successfully!");
    } catch (SQLException e) {
      System.err.println("Error inserting sample data: " + e.getMessage());
    }
  }

  /**
   * Execute a SQL query and display results
   */
  public void executeQuery(String sql) {
    try {
      Statement stmt = connection.createStatement();

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
   * Close database connection
   */
  public void close() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.println("✓ Database connection closed.");
      }
    } catch (SQLException e) {
      System.err.println("Error closing connection: " + e.getMessage());
    }
  }

  /**
   * Main method - entry point
   */
  public static void main(String[] args) {
    System.out.println("🚀 Starting SQL Learning Application...");

    DbLearningApp app = new DbLearningApp();

    // Start H2 Console Server
    try {
      org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
      System.out.println("✓ H2 Web Console started on port 8082");
    } catch (SQLException e) {
      System.err.println("Could not start H2 Console: " + e.getMessage());
    }

    SqlController apiController = null;
    try {
      apiController = new SqlController();
      apiController.start();

    } catch (Exception e) {
      System.err.println("Could not start API server: " + e.getMessage());
    }

    // Start interactive mode
    app.startInteractiveMode();

    // Cleanup
    if (apiController != null) {
      apiController.stop();
    }
    app.close();
  }
}