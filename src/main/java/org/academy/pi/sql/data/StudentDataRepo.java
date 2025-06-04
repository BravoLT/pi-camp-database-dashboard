package org.academy.pi.sql.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.academy.pi.sql.models.Student;

public class StudentDataRepo {

  private static final String CREATE_TABLE = """
      CREATE TABLE students (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        age INT NOT NULL,
        grade INT NOT NULL,
        updated_date DATE DEFAULT CURRENT_DATE
      )
      """;

  private static final String INSERT_INITIAL_DATA = """
      INSERT INTO students (name, age, grade) VALUES
        ('Alice Johnson', 13, 8),
        ('Bob Smith', 15, 10),
        ('Charlie Brown', 12, 7),
        ('Diana Prince', 16, 11),
        ('Eve Wilson', 14, 9),
        ('Frank Miller', 13, 8),
        ('Grace Lee', 17, 12),
        ('Henry Davis', 15, 10)
      """;

  private static final String SELECT_ALL = """
      SELECT
        id, name, age, grade, updated_date
      FROM students ORDER BY name
      """;

  private static final String INSERT_ONE = """
      INSERT INTO students (name, age, grade) VALUES (?, ?, ?)
      """;

  private final RootDataRepo rootDataRepo;

  public StudentDataRepo(final RootDataRepo rootDataRepo) {
    this.rootDataRepo = rootDataRepo;
    initializeTable();
  }

  public List<Student> getAllStudents() throws SQLException {
    List<Student> students = new ArrayList<>();

    try (Connection conn = rootDataRepo.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

      while (rs.next()) {
        students.add(
            Student.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .age(rs.getInt("age"))
                .grade(rs.getInt("grade"))
                .updatedDate(rs.getDate("updated_date").toLocalDate())
                .build()
        );
      }
    }
    return students;
  }

  public boolean addStudent(Student student) throws SQLException {
    try (Connection conn = rootDataRepo.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(INSERT_ONE)) {

      pstmt.setString(1, student.getName());
      pstmt.setInt(2, student.getAge());
      pstmt.setInt(3, student.getGrade());

      return pstmt.executeUpdate() > 0;
    }
  }

  private void initializeTable() {
    try (Connection conn = rootDataRepo.getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute("DROP TABLE IF EXISTS students");
      stmt.execute(CREATE_TABLE);
      stmt.execute(INSERT_INITIAL_DATA);

      System.out.println("✓ Student table created successfully!");
    } catch (SQLException e) {
      System.err.println("Error creating student table: " + e.getMessage());
    }
  }
}