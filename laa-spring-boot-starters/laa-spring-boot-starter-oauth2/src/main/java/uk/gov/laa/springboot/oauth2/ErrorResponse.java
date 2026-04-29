package uk.gov.laa.springboot.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds information about an error to send to a client.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

  /** The HTTP status code. **/
  private int code;

  /** The HTTP status description. */
  private String status;

  /** A message providing further information about the error. */
  private String message;
}
