package org.academy.pi.sql.data;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.academy.pi.sql.models.SqlNamedQuery;

@UtilityClass
public class QueryRepo {

  private static final List<SqlNamedQuery> SAMPLE_QUERIES = List.of(
      SqlNamedQuery.builder()
          .title("Find All")
          .query("SELECT * FROM students;")
          .build(),
      SqlNamedQuery.builder()
          .title("Find By Name")
          .query("""
          SELECT USERNAME, FIRST_NAME, LAST_NAME FROM STUDENTS
          WHERE FIRST_NAME = 'Nanny';
          """)
          .query("SELECT USERNAME, FIRST_NAME, LAST_NAME FROM STUDENTS WHERE FIRST_NAME = 'Nanny';")
          .build(),
      SqlNamedQuery.builder()
          .title("Find Like Name")
          .query("""
          SELECT USERNAME, FIRST_NAME, LAST_NAME, EMAIL FROM STUDENTS
          WHERE FIRST_NAME LIKE 'Nan%';
          """)
          .build(),
      SqlNamedQuery.builder()
          .title("Find Distinct Values")
          .query("SELECT DISTINCT IS_GOOD FROM STUDENTS;")
          .build(),
      SqlNamedQuery.builder()
          .title("Count All")
          .query("SELECT COUNT(*) AS TOTAL_STUDENTS FROM STUDENTS;")
          .build(),
      SqlNamedQuery.builder()
          .title("Count With Shirt Size")
          .query("""
          SELECT COUNT(*) AS STUDENTS_WITH_SHIRT_SIZE FROM STUDENTS
          WHERE SHIRT_SIZE IS NOT NULL;
          """)
          .build(),
      SqlNamedQuery.builder()
          .title("Count By Group")
          .query("""
          SELECT
            IS_GOOD, COUNT(*) AS GOOD_STUDENTS
          FROM STUDENTS
          GROUP BY IS_GOOD
          ORDER BY GOOD_STUDENTS DESC;
          """)
          .build(),
      SqlNamedQuery.builder()
          .title("Count By 2 Groups")
          .query("""
          SELECT
            SHIRT_SIZE, IS_GOOD, COUNT(*) AS GOOD_STUDENTS_WITH_SHIRT_SIZE
          FROM STUDENTS
          WHERE SHIRT_SIZE IS NOT NULL
          GROUP BY SHIRT_SIZE, IS_GOOD
          ORDER BY SHIRT_SIZE ASC, IS_GOOD DESC;
          """)
          .build(),
      SqlNamedQuery.builder()
          .title("Join Favorites Filter Name")
          .query("""
           SELECT
             S.ID, S.FIRST_NAME, F.CATEGORY, F.FAVORITE
           FROM STUDENTS S
           JOIN FAVORITES F ON S.ID = F.STUDENT_ID
           WHERE S.FIRST_NAME = 'Thor';
           """)
          .build(),
      SqlNamedQuery.builder()
          .title("Join Favorites Filter Color w/ Limit")
          .query("""
           SELECT
             S.ID, S.FIRST_NAME, F.CATEGORY, F.FAVORITE
           FROM STUDENTS S
           JOIN FAVORITES F ON S.ID = F.STUDENT_ID
           WHERE F.CATEGORY = 'COLOR'
           AND F.FAVORITE = 'Fuscia'
           ORDER BY S.FIRST_NAME
           LIMIT 10;
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