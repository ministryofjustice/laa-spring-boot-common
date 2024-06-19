package uk.gov.laa.ccms.springboot.auth;

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

  /** The HTTP Status code. **/
  private int code;

  /** The HTTP Status description. */
  private String status;

  /** A message providing further information about the error. */
  private String message;

}
