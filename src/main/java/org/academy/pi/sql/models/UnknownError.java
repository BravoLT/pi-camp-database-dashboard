package org.academy.pi.sql.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnknownError {

  private String errorMessage;
}
