package org.academy.pi.sql.models;

import lombok.Data;

@Data
public class ApiResponse<T> {

  private ApiResponseType type;
  private T data;

  private ApiResponse(ApiResponseType type, T data) {
    this.type = type;
    this.data = data;
  }

  public static <T> ApiResponse<T> success(ApiResponseType type, T data) {
    return new ApiResponse<>(type, data);
  }

  public static ApiResponse<UnknownError> error(String message) {
    return new ApiResponse<>(
        ApiResponseType.ERROR,
        UnknownError.builder().errorMessage(message).build()
    );
  }
}
