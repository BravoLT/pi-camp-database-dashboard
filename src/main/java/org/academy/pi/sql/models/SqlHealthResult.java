package org.academy.pi.sql.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class SqlHealthResult {

  private boolean connected;
  private String message;
  private List<SqlNamedQuery> sampleQueries;
  private List<String> tableNames;
}