package com.example.util.http;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(force = true)
@Getter
public class HttpErrorInfo {

  private final ZonedDateTime timestamp;
  private final HttpStatus httpStatus;
  private final String path;
  private final String message;

  public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
    timestamp = ZonedDateTime.now();
    this.httpStatus = httpStatus;
    this.path = path;
    this.message = message;
  }
}
