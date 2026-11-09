package uk.gov.laa.springboot.auth;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when no credentials have been provided, but are required.
 *
 * @deprecated Prefer {@code laa-spring-boot-starter-oauth2} for new API integrations. See its
 *     README and migration guide for setup guidance.
 */
@Deprecated
public class MissingCredentialsException extends AuthenticationException {

  /**
   * Creates an instance of {@code MissingCredentialsException} with a message and original cause.
   *
   * @param msg a message providing information about the exception
   * @param cause the original cause of this exception
   */
  public MissingCredentialsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Creates an instance of {@code MissingCredentialsException} with a message.
   *
   * @param msg a message providing information about the exception
   */
  public MissingCredentialsException(String msg) {
    super(msg);
  }
}
