package uk.gov.laa.springboot.oauth2.testsupport;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.laa.springboot.oauth2.Oauth2AuthorizationProperties;

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

  private static final String SUBJECT_CLAIM = "sub";
  private static final String ALGORITHM_HEADER = "alg";
  private static final String NONE_ALGORITHM = "none";
  private static final long TOKEN_LIFETIME_SECONDS = 3600;

  /**
   * Builds a Spring Security JWT from this fixture.
   *
   * @return JWT containing roles/scopes and extra claims
   */
  public Jwt toJwt() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(SUBJECT_CLAIM, subject);
    claims.put(Oauth2AuthorizationProperties.DEFAULT_ROLES_CLAIM_PATH, roles);
    if (scopes != null && scopes.length > 0) {
      claims.put(
          Oauth2AuthorizationProperties.DEFAULT_SCOPES_CLAIM_PATH,
          String.join(" ", scopes));
    }
    if (additionalClaims != null) {
      claims.putAll(additionalClaims);
    }
    Instant issuedAt = Instant.now();
    return new Jwt(
        token,
        issuedAt,
        issuedAt.plusSeconds(TOKEN_LIFETIME_SECONDS),
        Map.of(ALGORITHM_HEADER, NONE_ALGORITHM),
        claims);
  }
}
