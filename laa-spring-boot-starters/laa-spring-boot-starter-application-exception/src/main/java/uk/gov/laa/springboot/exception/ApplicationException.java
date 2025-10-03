package uk.gov.laa.springboot.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown in the api for application exceptions.
 */
public class ApplicationException extends RuntimeException {

  private final String errorMessage;

  private final HttpStatus httpStatus;

  /**
   * The exception thrown for specific api exceptions.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   * @param httpStatus the HTTP status to return for the exception.
   */
  public ApplicationException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
    this.errorMessage = message;
  }

  /**
   * Retrieves the error message associated with this exception.
   *
   * @return the error message as a String.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Retrieves the HTTP status associated with this exception.
   *
   * @return the HTTP status as an HttpStatus object.
   */
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}