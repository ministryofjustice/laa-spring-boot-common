package uk.gov.laa.springboot.oauth2.utils;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;

/** Provides the JWT validation policy used by the API's resource-server decoders. */
public final class JwtValidation {

  private JwtValidation() {}

  public static OAuth2TokenValidator<Jwt> requiringExpiryWithIssuer(String issuer) {
    JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
    timestampValidator.setAllowEmptyExpiryClaim(false);
    return new DelegatingOAuth2TokenValidator<>(
        timestampValidator, JwtValidators.createDefaultWithIssuer(issuer));
  }
}

