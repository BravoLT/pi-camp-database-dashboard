package org.academy.pi.sql.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Student {

  private Integer id;
  private String name;
  private Integer age;
  private Integer grade;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate updatedDate;
}
