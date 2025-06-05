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
public class SqlQueryResult {

  private List<String> columns;
  private List<List<Object>> rows;
  private int count;
  private long execTimeMs;
}