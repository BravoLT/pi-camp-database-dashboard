package org.academy.pi.sql.models;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApiResponse<T> {

  private T data;
  private String message;
  private String timestamp;

  private ApiResponse(T data, String message) {
    this.data = data;
    this.message = message;
    this.timestamp = LocalDateTime.now().toString();
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, "Success");
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(null, message);
  }
}
