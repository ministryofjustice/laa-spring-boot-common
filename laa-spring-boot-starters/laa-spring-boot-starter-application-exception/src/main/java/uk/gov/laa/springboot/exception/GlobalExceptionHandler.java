package uk.gov.laa.springboot.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller advice class responsible for handling exceptions globally and providing appropriate
 * error responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles the {@link uk.gov.laa.springboot.exception.ApplicationException} by logging the error and returning an
   * appropriate error response.
   *
   * @param e the ApplicationException
   * @return the response entity with the status code and error response body.
   */
  @ExceptionHandler(value = {ApplicationException.class})
  public ResponseEntity<Object> handleApplicationException(final ApplicationException e) {
    log.error("Application exception occurred", e);

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("error_message", e.getErrorMessage());
    responseBody.put("http_status", e.getHttpStatus().value());

    return ResponseEntity.status(e.getHttpStatus()).body(responseBody);
  }
}