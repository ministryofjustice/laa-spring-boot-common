package uk.gov.laa.springboot.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds information about an error to send to a client.
 *
 * @deprecated Prefer {@code laa-spring-boot-starter-oauth2} for new API integrations. See its
 *     README and migration guide for setup guidance.
 */
@Deprecated
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
