package org.academy.pi.sql.data;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.academy.pi.sql.models.SqlNamedQuery;

@UtilityClass
public class QueryRepo {

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
          .title("Find Unique Students by Grade")
          .query("SELECT DISTINCT name, age FROM students WHERE grade = 7;")
          .build(),
      SqlNamedQuery.builder()
          .title("Count Students")
          .query("SELECT COUNT(*) as total_students FROM students;")
          .build(),
      SqlNamedQuery.builder()
          .title("Group Students by Grade")
          .query("""
          SELECT
            grade, COUNT(*) as grade_students
          FROM students
          GROUP BY grade
          ORDER BY grade;
          """)
          .build(),
      SqlNamedQuery.builder()
          .title("Order Students by Age")
          .query("SELECT * FROM students ORDER BY age DESC;")
          .build(),
      SqlNamedQuery.builder()
          .title("Join Favorites")
          .query("""
           SELECT
            s.name, s.age, s.grade,
            f.fav_key, f.fav_val
           FROM students s
           JOIN favorites f ON s.id = f.student_id
           WHERE s.id = 1;
           """)
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

  public static List<String> getTableNames() {
    return List.of("favorites", "students");
  }

  public static List<SqlNamedQuery> getSampleQueries() {
    return SAMPLE_QUERIES;
  }
}