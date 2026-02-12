package uk.gov.laa.springboot.export.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.laa.springboot.export.ExportValidationException;

/**
 * Maps export exceptions to HTTP responses.
 */
@RestControllerAdvice
public class ExportExceptionHandler {

  /**
   * Maps export validation failures to a 400 response.
   */
  @ExceptionHandler(ExportValidationException.class)
  public ProblemDetail handleExportValidationException(ExportValidationException exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    problemDetail.setTitle("Invalid export request");
    return problemDetail;
  }
}
