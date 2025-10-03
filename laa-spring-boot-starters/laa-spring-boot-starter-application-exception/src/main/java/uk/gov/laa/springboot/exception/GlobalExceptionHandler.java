package uk.gov.laa.springboot.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Controller advice class responsible for handling exceptions globally and providing appropriate
 * error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handles the {@link uk.gov.laa.springboot.exception.ApplicationException} by logging the error
   * and returning an appropriate error response.
   *
   * @param e the ApplicationException
   * @return the response entity with the status code and error response body.
   */
  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ApplicationException> handleApplicationException(ApplicationException e) {
    log.error("Application exception occurred", e);
    return ResponseEntity.status(e.getHttpStatus()).body(e);
  }
}