package uk.gov.laa.springboot.oauth2.utils;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;

/** Provides the JWT validation policy used by the API's resource-server decoders. */
public final class JwtValidation {

  private JwtValidation() {}

  /**
   * Creates a validator that requires a valid expiry and the supplied issuer.
   *
   * @param issuer JWT issuer that must be present in validated tokens
   * @return a combined timestamp and issuer validator
   */
  public static OAuth2TokenValidator<Jwt> requiringExpiryWithIssuer(String issuer) {
    JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
    timestampValidator.setAllowEmptyExpiryClaim(false);
    return new DelegatingOAuth2TokenValidator<>(
        timestampValidator, JwtValidators.createDefaultWithIssuer(issuer));
  }
}
