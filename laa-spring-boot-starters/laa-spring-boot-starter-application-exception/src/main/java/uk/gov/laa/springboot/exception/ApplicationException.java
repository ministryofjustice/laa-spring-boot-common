package uk.gov.laa.springboot.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Exception thrown in the api for application exceptions.
 */
public class ApplicationException extends RuntimeException {

  @Getter
  @JsonProperty("errorMessage")
  private final String errorMessage;

  @Getter
  private final HttpStatusCode httpStatus;

  /**
   * The exception thrown for specific api exceptions.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   * @param httpStatus the HTTP status to return for the exception.
   */
  public ApplicationException(String message, HttpStatusCode httpStatus) {
    super(message);
    this.errorMessage = message;
    this.httpStatus = httpStatus;
  }

  /**
   * Constructor used for deserialization from JSON.
   *
   * @param message the detail message.
   * @param httpStatusValue the HTTP status code as an integer.
   */
  @JsonCreator
  public ApplicationException(
      @JsonProperty("errorMessage") String message,
      @JsonProperty("httpStatus") int httpStatusValue
  ) {
    super(message);
    this.errorMessage = message;
    this.httpStatus = HttpStatusCode.valueOf(httpStatusValue);
  }

  /**
   * Retrieves the HTTP status code associated with this exception.
   *
   * @return the HTTP status code as an integer.
   */
  @JsonProperty("httpStatus")
  public int getHttpStatusValue() {
    return httpStatus.value();
  }

}