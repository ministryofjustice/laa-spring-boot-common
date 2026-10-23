package uk.gov.laa.springboot.oauth2.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Retrieves effective identity and entitlement claims for the current request. */
public final class SecurityHelper {

  private static final String LAA_ACCOUNTS_CLAIM = "LAA_ACCOUNTS";
  private static final String LAA_APP_ROLES_CLAIM = "LAA_APP_ROLES";
  private static final String ENTRA_OID_CLAIM = "oid";
  private static final String ENTRA_SCOPES_CLAIM = "scp";

  private SecurityHelper() {}

  /**
   * Gets the validated {@code LAA_ACCOUNTS} claim from the current JWT authentication.
   *
   * @return an immutable list of account codes, or an empty list when unavailable
   */
  public static List<String> getLaaAccounts() {
    List<String> accounts =
        getJwtAuthentication()
            .map(JwtAuthenticationToken::getToken)
            .map(jwt -> jwt.getClaimAsStringList(LAA_ACCOUNTS_CLAIM))
            .orElse(null);
    return accounts == null ? List.of() : List.copyOf(accounts);
  }

  /**
   * Gets the validated {@code LAA_APP_ROLES} claim from the current JWT authentication.
   *
   * @return an immutable list of application roles, or an empty list when unavailable
   */
  public static List<String> getLaaAppRoles() {
    List<String> appRoles =
        getJwtAuthentication()
            .map(JwtAuthenticationToken::getToken)
            .map(jwt -> jwt.getClaimAsStringList(LAA_APP_ROLES_CLAIM))
            .orElse(null);
    return appRoles == null ? List.of() : List.copyOf(appRoles);
  }

  /**
   * Gets the validated Entra delegated application scopes from the current JWT authentication.
   *
   * @return an immutable set of scopes, or an empty set when unavailable
   */
  public static Set<String> getScopes() {
    return getJwtAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(jwt -> jwt.getClaimAsString(ENTRA_SCOPES_CLAIM))
        .stream()
        .flatMap(scopes -> Arrays.stream(scopes.split("\\s+")))
        .filter(scope -> !scope.isBlank())
        .collect(Collectors.toUnmodifiableSet());
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

  private static Optional<JwtAuthenticationToken> getJwtAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication instanceof JwtAuthenticationToken jwtAuthentication
        ? Optional.of(jwtAuthentication)
        : Optional.empty();
  }
}
