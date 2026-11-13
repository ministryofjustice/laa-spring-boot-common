package uk.gov.laa.springboot.oauth2.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Retrieves effective identity and entitlement claims for the current request. */
public final class SecurityHelper {

  public static final String LAA_ACCOUNTS_CLAIM = "LAA_ACCOUNTS";
  public static final String LAA_APP_ROLES_CLAIM = "LAA_APP_ROLES";
  public static final String PROFILE_ID_CLAIM = "PROFILE_ID";
  public static final String FIRM_CODE_CLAIM = "FIRM_CODE";
  public static final String ENTRA_OID_CLAIM = "oid";
  public static final String ENTRA_SCOPES_CLAIM = "scp";

  private SecurityHelper() {}

  /**
   * Gets the validated {@code LAA_ACCOUNTS} claim from the current JWT authentication.
   *
   * @return an immutable list of account codes, or an empty list when unavailable
   */
  public static List<String> getLaaAccounts() {
    return getStringListClaim(LAA_ACCOUNTS_CLAIM);
  }

  /**
   * Gets the validated {@code LAA_APP_ROLES} claim from the current JWT authentication.
   *
   * @return an immutable list of application roles, or an empty list when unavailable
   */
  public static List<String> getLaaAppRoles() {
    return getStringListClaim(LAA_APP_ROLES_CLAIM);
  }

  /**
   * Gets the validated profile identifier from the current JWT authentication.
   *
   * @return the profile identifier, or {@code null} when unavailable
   */
  public static String getProfileId() {
    return getStringClaim(PROFILE_ID_CLAIM);
  }

  /**
   * Gets the validated firm code from the current JWT authentication.
   *
   * @return the firm code, or {@code null} when unavailable
   */
  public static String getFirmCode() {
    return getStringClaim(FIRM_CODE_CLAIM);
  }

  /**
   * Gets the validated Entra delegated application scopes from the current JWT authentication.
   *
   * @return an immutable list of scopes, or an empty list when unavailable
   */
  public static List<String> getScopes() {
    return getStringListClaim(ENTRA_SCOPES_CLAIM);
  }

  /**
   * Gets the Entra object identifier from the JWT for the current request.
   *
   * @return the Entra OID, or empty when the request is not JWT-authenticated or the claim is
   *     absent
   */
  public static Optional<String> getEntraOid() {
    return getJwtAuthentication().flatMap(authentication -> getEntraOid(authentication.getToken()));
  }

  /**
   * Gets the Entra object identifier from a JWT.
   *
   * @param jwt the JWT to inspect
   * @return the Entra OID, or empty when the claim is absent or blank
   */
  public static Optional<String> getEntraOid(Jwt jwt) {
    return Optional.ofNullable(jwt.getClaimAsString(ENTRA_OID_CLAIM)).filter(oid -> !oid.isBlank());
  }

  private static List<String> getStringListClaim(String claimName) {
    return getJwtAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(jwt -> parseClaimValues(jwt.getClaimAsString(claimName)))
        .orElse(List.of());
  }

  private static List<String> parseClaimValues(String claimValue) {
    if (claimValue == null) {
      return List.of();
    }
    return Arrays.stream(claimValue.split("[,\\s]+"))
        .map(String::trim)
        .filter(v -> !v.isBlank())
        .toList();
  }

  private static String getStringClaim(String claimName) {
    return getJwtAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(jwt -> jwt.getClaimAsString(claimName))
        .filter(value -> !value.isBlank())
        .map(String::trim)
        .orElse(null);
  }

  private static Optional<JwtAuthenticationToken> getJwtAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication instanceof JwtAuthenticationToken jwtAuthentication
        ? Optional.of(jwtAuthentication)
        : Optional.empty();
  }
}
