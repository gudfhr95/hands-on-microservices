package com.example.util.http;

import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;

public class HttpErrorInfo {

  private final ZonedDateTime timestamp;
  private final HttpStatus httpStatus;
  private final String path;
  private final String message;

  public HttpErrorInfo() {
    timestamp = null;
    httpStatus = null;
    path = null;
    message = null;
  }

  public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
    timestamp = ZonedDateTime.now();
    this.httpStatus = httpStatus;
    this.path = path;
    this.message = message;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getPath() {
    return path;
  }

  public String getMessage() {
    return message;
  }
}
