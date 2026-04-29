package uk.gov.laa.springboot.oauth2.testsupport;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Simple token fixture for tests that need a mock auth service/JWT decoder.
 *
 * @param token bearer token value
 * @param subject token subject
 * @param roles role names without prefix
 * @param scopes scope names without prefix
 * @param additionalClaims extra claims to include
 */
public record StubJwtToken(
    String token,
    String subject,
    String[] roles,
    String[] scopes,
    Map<String, Object> additionalClaims) {

  /**
   * Builds a Spring Security JWT from this fixture.
   *
   * @return JWT containing roles/scopes and extra claims
   */
  public Jwt toJwt() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", subject);
    claims.put("roles", roles);
    if (scopes != null && scopes.length > 0) {
      claims.put("scope", String.join(" ", scopes));
    }
    if (additionalClaims != null) {
      claims.putAll(additionalClaims);
    }
    Instant issuedAt = Instant.now();
    return new Jwt(token, issuedAt, issuedAt.plusSeconds(3600), Map.of("alg", "none"), claims);
  }
}
