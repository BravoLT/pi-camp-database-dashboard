package org.academy.pi.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.academy.pi.sql.data.RootDataRepo;
import org.academy.pi.sql.data.StudentDataRepo;
import org.academy.pi.sql.models.ApiResponse;
import org.academy.pi.sql.models.QueryResult;
import org.academy.pi.sql.models.Student;

/**
 * REST API Controller for SQL Learning App Provides HTTP endpoints to access H2 database data Runs
 * on port 8080 alongside the main application
 */
public class SqlController {

  private static final int API_PORT = 8080;

  private final ObjectMapper objectMapper;
  private final RootDataRepo rootDataRepo;
  private final StudentDataRepo studentDataRepo;

  private HttpServer server;

  public SqlController() {
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    this.rootDataRepo = new RootDataRepo();
    this.studentDataRepo = new StudentDataRepo(rootDataRepo);
  }

  protected Connection getConnection() throws SQLException {
    return rootDataRepo.getConnection();
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
    server.createContext("/api/query", this::handleCustomQuery);

    server.setExecutor(null);
    server.start();

    System.out.println("🌐 SQL Learning API started on http://localhost:" + API_PORT);
    System.out.println("📋 Available endpoints:");
    System.out.println("   GET  /api/students - Get all students");
    System.out.println("   POST /api/students - Add new student");
    System.out.println("   POST /api/query - Execute custom SQL query");
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
        List<Student> students = studentDataRepo.getAllStudents();
        sendJsonResponse(exchange, 200, ApiResponse.success(students));
      } else if ("POST".equals(exchange.getRequestMethod())) {
        Student newStudent = readRequestBody(exchange, Student.class);
        boolean success = studentDataRepo.addStudent(newStudent);
        if (success) {
          sendJsonResponse(exchange, 201, ApiResponse.success("Student added successfully"));
        } else {
          sendJsonResponse(exchange, 400, ApiResponse.error("Failed to add student"));
        }
      } else {
        sendJsonResponseFor405(exchange);
      }
    } catch (Exception e) {
      sendJsonResponseFor500(exchange, e);
    }
  }

  /**
   * Handle /api/query endpoints - Execute custom SQL
   */
  @SuppressWarnings("unchecked")
  private void handleCustomQuery(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    try {
      if ("POST".equals(exchange.getRequestMethod())) {
        var request = (Map<String, String>) readRequestBody(exchange, Map.class);
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
          sendJsonResponse(exchange, 400, ApiResponse.error("SQL query is required"));
          return;
        }

        QueryResult result = rootDataRepo.executeQuery(sql);
        sendJsonResponse(exchange, 200, ApiResponse.success(result));
      } else {
        sendJsonResponseFor405(exchange);
      }
    } catch (Exception e) {
      sendJsonResponseFor500(exchange, e);
    }
  }

  private void addCorsHeaders(HttpExchange exchange) {
    Headers headers = exchange.getResponseHeaders();
    headers.add("Access-Control-Allow-Origin", "*");
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    headers.add("Content-Type", "application/json");
  }

  private void sendJsonResponseFor405(HttpExchange exchange) throws IOException {
    sendJsonResponse(exchange, 405, ApiResponse.error("Method Not Allowed"));
  }

  private void sendJsonResponseFor500(HttpExchange exchange, Exception e) throws IOException {
    e.printStackTrace();
    sendJsonResponse(exchange, 500,
        ApiResponse.error("Internal Service Error: %s".formatted(e.getMessage())));
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

  private <T> T readRequestBody(HttpExchange exchange, final Class<T> clazz) throws IOException {
    try (InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(is, StandardCharsets.UTF_8))) {

      StringBuilder body = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
      return objectMapper.readValue(body.toString(), clazz);
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