package org.academy.pi.sql;

import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * REST API Controller for SQL Learning App Provides HTTP endpoints to access H2 database data Runs
 * on port 8080 alongside the main application
 */
public class SqlController {

  private static final String DB_URL = "jdbc:h2:~/sqllearning;AUTO_SERVER=TRUE";
  private static final String DB_USER = "student";
  private static final String DB_PASSWORD = "learn123";
  private static final int API_PORT = 8080;

  private final ObjectMapper objectMapper;
  private HttpServer server;

  public SqlController() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  /**
   * Data Transfer Objects (DTOs)
   */
  public static class Student {

    public int id;
    public String name;
    public int age;
    public int grade;
    public String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate enrollmentDate;

    public Student() {
    }

    public Student(int id, String name, int age, int grade, String email,
        LocalDate enrollmentDate) {
      this.id = id;
      this.name = name;
      this.age = age;
      this.grade = grade;
      this.email = email;
      this.enrollmentDate = enrollmentDate;
    }
  }

  public static class Book {

    public int id;
    public String title;
    public String author;
    public String genre;
    public double price;
    public int publicationYear;
    public boolean available;

    public Book() {
    }

    public Book(int id, String title, String author, String genre, double price,
        int publicationYear, boolean available) {
      this.id = id;
      this.title = title;
      this.author = author;
      this.genre = genre;
      this.price = price;
      this.publicationYear = publicationYear;
      this.available = available;
    }
  }

  public static class Order {

    public int id;
    public int studentId;
    public int bookId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate orderDate;
    public int quantity;

    // Additional fields for joined data
    public String studentName;
    public String bookTitle;
    public double bookPrice;

    public Order() {
    }

    public Order(int id, int studentId, int bookId, LocalDate orderDate, int quantity) {
      this.id = id;
      this.studentId = studentId;
      this.bookId = bookId;
      this.orderDate = orderDate;
      this.quantity = quantity;
    }
  }

  public static class QueryResult {

    public List<String> columns;
    public List<List<Object>> rows;
    public int totalRows;
    public String executionTime;

    public QueryResult(List<String> columns, List<List<Object>> rows, String executionTime) {
      this.columns = columns;
      this.rows = rows;
      this.totalRows = rows.size();
      this.executionTime = executionTime;
    }
  }

  public static class ApiResponse<T> {

    public boolean success;
    public T data;
    public String message;
    public String timestamp;

    public ApiResponse(boolean success, T data, String message) {
      this.success = success;
      this.data = data;
      this.message = message;
      this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static <T> ApiResponse<T> success(T data) {
      return new ApiResponse<>(true, data, "Success");
    }

    public static <T> ApiResponse<T> error(String message) {
      return new ApiResponse<>(false, null, message);
    }
  }

  /**
   * Start the HTTP server
   */
  public void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress(API_PORT), 0);

    // Setup CORS for all endpoints
    server.createContext("/", this::handleCors);

    // API endpoints
    server.createContext("/api/students", this::handleStudents);
    server.createContext("/api/books", this::handleBooks);
    server.createContext("/api/orders", this::handleOrders);
    server.createContext("/api/query", this::handleCustomQuery);
    server.createContext("/api/stats", this::handleStats);
    server.createContext("/api/health", this::handleHealth);

    server.setExecutor(null);
    server.start();

    System.out.println("🌐 SQL Learning API started on http://localhost:" + API_PORT);
    System.out.println("📋 Available endpoints:");
    System.out.println("   GET  /api/students - Get all students");
    System.out.println("   POST /api/students - Add new student");
    System.out.println("   GET  /api/books - Get all books");
    System.out.println("   GET  /api/orders - Get all orders with details");
    System.out.println("   POST /api/query - Execute custom SQL query");
    System.out.println("   GET  /api/stats - Get database statistics");
    System.out.println("   GET  /api/health - Health check");
  }

  /**
   * Stop the HTTP server
   */
  public void stop() {
    if (server != null) {
      server.stop(0);
      System.out.println("🛑 API server stopped");
    }
  }

  /**
   * Handle CORS preflight requests
   */
  private void handleCors(HttpExchange exchange) throws IOException {
    Headers headers = exchange.getResponseHeaders();
    headers.add("Access-Control-Allow-Origin", "*");
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

    if ("OPTIONS".equals(exchange.getRequestMethod())) {
      exchange.sendResponseHeaders(200, 0);
      exchange.close();
    }
  }

  /**
   * Handle /api/students endpoints
   */
  private void handleStudents(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("GET".equals(exchange.getRequestMethod())) {
        List<Student> students = getAllStudents();
        sendJsonResponse(exchange, 200, ApiResponse.success(students));
      } else if ("POST".equals(exchange.getRequestMethod())) {
        String body = readRequestBody(exchange);
        Student student = objectMapper.readValue(body, Student.class);
        boolean success = addStudent(student);

        if (success) {
          sendJsonResponse(exchange, 201, ApiResponse.success("Student added successfully"));
        } else {
          sendJsonResponse(exchange, 400, ApiResponse.error("Failed to add student"));
        }
      } else {
        sendJsonResponse(exchange, 405, ApiResponse.error("Method not allowed"));
      }
    } catch (Exception e) {
      sendJsonResponse(exchange, 500,
          ApiResponse.error("Internal server error: " + e.getMessage()));
    }
  }

  /**
   * Handle /api/books endpoints
   */
  private void handleBooks(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("GET".equals(exchange.getRequestMethod())) {
        List<Book> books = getAllBooks();
        sendJsonResponse(exchange, 200, ApiResponse.success(books));
      } else {
        sendJsonResponse(exchange, 405, ApiResponse.error("Method not allowed"));
      }
    } catch (Exception e) {
      sendJsonResponse(exchange, 500,
          ApiResponse.error("Internal server error: " + e.getMessage()));
    }
  }

  /**
   * Handle /api/orders endpoints
   */
  private void handleOrders(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("GET".equals(exchange.getRequestMethod())) {
        List<Order> orders = getAllOrdersWithDetails();
        sendJsonResponse(exchange, 200, ApiResponse.success(orders));
      } else {
        sendJsonResponse(exchange, 405, ApiResponse.error("Method not allowed"));
      }
    } catch (Exception e) {
      sendJsonResponse(exchange, 500,
          ApiResponse.error("Internal server error: " + e.getMessage()));
    }
  }

  /**
   * Handle /api/query endpoints - Execute custom SQL
   */
  private void handleCustomQuery(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("POST".equals(exchange.getRequestMethod())) {
        String body = readRequestBody(exchange);
        Map<String, String> request = objectMapper.readValue(body, Map.class);
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
          sendJsonResponse(exchange, 400, ApiResponse.error("SQL query is required"));
          return;
        }

        QueryResult result = executeQuery(sql);
        sendJsonResponse(exchange, 200, ApiResponse.success(result));
      } else {
        sendJsonResponse(exchange, 405, ApiResponse.error("Method not allowed"));
      }
    } catch (Exception e) {
      sendJsonResponse(exchange, 500, ApiResponse.error("Query error: " + e.getMessage()));
    }
  }

  /**
   * Handle /api/stats endpoints
   */
  private void handleStats(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("GET".equals(exchange.getRequestMethod())) {
        Map<String, Object> stats = getDatabaseStats();
        sendJsonResponse(exchange, 200, ApiResponse.success(stats));
      } else {
        sendJsonResponse(exchange, 405, ApiResponse.error("Method not allowed"));
      }
    } catch (Exception e) {
      sendJsonResponse(exchange, 500,
          ApiResponse.error("Internal server error: " + e.getMessage()));
    }
  }

  /**
   * Handle /api/health endpoints
   */
  private void handleHealth(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      Map<String, Object> health = new HashMap<>();
      health.put("status", "healthy");
      health.put("database", testDatabaseConnection() ? "connected" : "disconnected");
      health.put("timestamp", java.time.LocalDateTime.now().toString());

      sendJsonResponse(exchange, 200, ApiResponse.success(health));
    } catch (Exception e) {
      sendJsonResponse(exchange, 500, ApiResponse.error("Health check failed: " + e.getMessage()));
    }
  }

  /**
   * Database operations
   */
  private List<Student> getAllStudents() throws SQLException {
    List<Student> students = new ArrayList<>();

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY name")) {

      while (rs.next()) {
        students.add(new Student(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getInt("grade"),
            rs.getString("email"),
            rs.getDate("enrollment_date").toLocalDate()
        ));
      }
    }

    return students;
  }

  private List<Book> getAllBooks() throws SQLException {
    List<Book> books = new ArrayList<>();

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM books ORDER BY title")) {

      while (rs.next()) {
        books.add(new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("genre"),
            rs.getDouble("price"),
            rs.getInt("publication_year"),
            rs.getBoolean("available")
        ));
      }
    }

    return books;
  }

  private List<Order> getAllOrdersWithDetails() throws SQLException {
    List<Order> orders = new ArrayList<>();

    String sql = """
            SELECT o.id, o.student_id, o.book_id, o.order_date, o.quantity,
                   s.name as student_name, b.title as book_title, b.price as book_price
            FROM orders o
            JOIN students s ON o.student_id = s.id
            JOIN books b ON o.book_id = b.id
            ORDER BY o.order_date DESC
        """;

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        Order order = new Order(
            rs.getInt("id"),
            rs.getInt("student_id"),
            rs.getInt("book_id"),
            rs.getDate("order_date").toLocalDate(),
            rs.getInt("quantity")
        );
        order.studentName = rs.getString("student_name");
        order.bookTitle = rs.getString("book_title");
        order.bookPrice = rs.getDouble("book_price");
        orders.add(order);
      }
    }

    return orders;
  }

  private boolean addStudent(Student student) throws SQLException {
    String sql = "INSERT INTO students (name, age, grade, email) VALUES (?, ?, ?, ?)";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, student.name);
      pstmt.setInt(2, student.age);
      pstmt.setInt(3, student.grade);
      pstmt.setString(4, student.email);

      return pstmt.executeUpdate() > 0;
    }
  }

  private QueryResult executeQuery(String sql) throws SQLException {
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
              row.add(rs.getObject(i));
            }
            rows.add(row);
          }

          long executionTime = System.currentTimeMillis() - startTime;
          return new QueryResult(columns, rows, executionTime + "ms");
        }
      } else {
        int rowsAffected = stmt.executeUpdate(sql);
        long executionTime = System.currentTimeMillis() - startTime;

        List<String> columns = Arrays.asList("rows_affected");
        List<List<Object>> rows = Arrays.asList(Arrays.asList((Object) rowsAffected));
        return new QueryResult(columns, rows, executionTime + "ms");
      }
    }
  }

  private Map<String, Object> getDatabaseStats() throws SQLException {
    Map<String, Object> stats = new HashMap<>();

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      // Count records in each table
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
      rs.next();
      stats.put("totalStudents", rs.getInt(1));

      rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
      rs.next();
      stats.put("totalBooks", rs.getInt(1));

      rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
      rs.next();
      stats.put("totalOrders", rs.getInt(1));

      // Additional stats
      rs = stmt.executeQuery("SELECT AVG(age) FROM students");
      rs.next();
      stats.put("averageStudentAge", Math.round(rs.getDouble(1) * 100.0) / 100.0);

      rs = stmt.executeQuery("SELECT AVG(price) FROM books");
      rs.next();
      stats.put("averageBookPrice", Math.round(rs.getDouble(1) * 100.0) / 100.0);

      rs = stmt.executeQuery(
          "SELECT genre, COUNT(*) as count FROM books GROUP BY genre ORDER BY count DESC");
      Map<String, Integer> genreStats = new HashMap<>();
      while (rs.next()) {
        genreStats.put(rs.getString("genre"), rs.getInt("count"));
      }
      stats.put("booksByGenre", genreStats);
    }

    return stats;
  }

  /**
   * Utility methods
   */
  private Connection getConnection() throws SQLException {
    try {
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    } catch (ClassNotFoundException e) {
      throw new SQLException("H2 Driver not found", e);
    }
  }

  private boolean testDatabaseConnection() {
    try (Connection conn = getConnection()) {
      return conn != null && !conn.isClosed();
    } catch (SQLException e) {
      return false;
    }
  }

  private void addCorsHeaders(HttpExchange exchange) {
    Headers headers = exchange.getResponseHeaders();
    headers.add("Access-Control-Allow-Origin", "*");
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    headers.add("Content-Type", "application/json");
  }

  private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response)
      throws IOException {
    String jsonResponse = objectMapper.writeValueAsString(response);
    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }

  private String readRequestBody(HttpExchange exchange) throws IOException {
    try (InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(is, StandardCharsets.UTF_8))) {

      StringBuilder body = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
      return body.toString();
    }
  }

  /**
   * Main method for testing the API standalone
   */
  public static void main(String[] args) {
    SqlController controller = new SqlController();

    try {
      controller.start();

      // Keep the server running
      System.out.println("Press Enter to stop the server...");
      System.in.read();
    } catch (IOException e) {
      System.err.println("Failed to start API server: " + e.getMessage());
    } finally {
      controller.stop();
    }
  }
}