package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.laa.springboot.oauth2.utils.SecurityHelper;

/**
 * Filter that validates the {@code X-Authorization} header JWT and merges its {@code LAA_APP_ROLES}
 * claim into the existing {@link Authentication}.
 *
 * <p>Logic:
 *
 * <ol>
 *   <li>Read the {@code X-Authorization} header; absent → continue with OBO authentication.
 *   <li>Decode and validate the JWT via the injected {@code xAuthorizationJwtDecoder}; invalid →
 *       401.
 *   <li>Compare the Entra {@code oid} claim against the existing OBO JWT; mismatch → 401.
 *   <li>Merge the allow-listed {@code LAA_APP_ROLES} and {@code LAA_ACCOUNTS} claims into the OBO
 *       JWT.
 *   <li>Merge configured role-prefix authorities into a JWT-backed {@link Authentication} and
 *       update the {@link SecurityContextHolder}.
 * </ol>
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class XAuthorizationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(XAuthorizationFilter.class);

  private static final String X_AUTHORIZATION_HEADER = "X-Authorization";
  private static final String LAA_APP_ROLES_CLAIM = "LAA_APP_ROLES";
  private static final String LAA_ACCOUNTS_CLAIM = "LAA_ACCOUNTS";
  private final JwtDecoder secondaryAuthorizationJwtDecoder;
  private final String roleAuthorityPrefix;

  public XAuthorizationFilter(
      JwtDecoder secondaryAuthorizationJwtDecoder, String roleAuthorityPrefix) {
    this.secondaryAuthorizationJwtDecoder = secondaryAuthorizationJwtDecoder;
    this.roleAuthorityPrefix = roleAuthorityPrefix;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String token = request.getHeader(X_AUTHORIZATION_HEADER);
    if (token == null) {
      // x-auth header does not exist, let chain continue without merging any entitlements
      chain.doFilter(request, response);
      return;
    }

    Jwt authorizationJwt;
    try {
      authorizationJwt = secondaryAuthorizationJwtDecoder.decode(token);
    } catch (JwtException e) {
      log.warn("X-Authorization token invalid: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if (!(existingAuth instanceof JwtAuthenticationToken oboAuthentication)) {
      log.warn("Expected JWT authentication before X-Authorization processing");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    Optional<String> authorizationOid = SecurityHelper.getEntraOid(authorizationJwt);
    Optional<String> oboOid = SecurityHelper.getEntraOid(oboAuthentication.getToken());
    if (authorizationOid.isEmpty() || oboOid.isEmpty() || !authorizationOid.equals(oboOid)) {
      log.warn("X-Authorization token OID does not match the OBO token OID");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    List<String> appRoles = claimAsNonBlankDistinctStrings(authorizationJwt, LAA_APP_ROLES_CLAIM);
    List<String> laaAccounts = claimAsNonBlankDistinctStrings(authorizationJwt, LAA_ACCOUNTS_CLAIM);
    if (appRoles.isEmpty() && laaAccounts.isEmpty()) {
      // Authenticated but no entitlements — let method security determine access.
      chain.doFilter(request, response);
      return;
    }

    Set<GrantedAuthority> mergedAuthorities = new HashSet<>(existingAuth.getAuthorities());
    appRoles.forEach(
        role -> mergedAuthorities.add(new SimpleGrantedAuthority(roleAuthorityPrefix + role)));

    JwtAuthenticationToken merged =
        new JwtAuthenticationToken(
            mergeEntitlementClaims(oboAuthentication.getToken(), appRoles, laaAccounts),
            mergedAuthorities,
            existingAuth.getName());
    merged.setDetails(existingAuth.getDetails());
    SecurityContextHolder.getContext().setAuthentication(merged);

    chain.doFilter(request, response);
  }

  private Jwt mergeEntitlementClaims(Jwt oboJwt, List<String> appRoles, List<String> laaAccounts) {
    Map<String, Object> claims = new LinkedHashMap<>(oboJwt.getClaims());
    claims.put(LAA_APP_ROLES_CLAIM, appRoles);
    claims.put(LAA_ACCOUNTS_CLAIM, laaAccounts);

    Jwt.Builder builder =
        Jwt.withTokenValue(oboJwt.getTokenValue())
            .headers(headers -> headers.putAll(oboJwt.getHeaders()))
            .claims(mergedClaims -> mergedClaims.putAll(claims));
    if (oboJwt.getIssuedAt() != null) {
      builder.issuedAt(oboJwt.getIssuedAt());
    }
    builder.expiresAt(
        Objects.requireNonNull(oboJwt.getExpiresAt(), "Validated OBO JWT must contain an expiry"));
    return builder.build();
  }

  private List<String> claimAsNonBlankDistinctStrings(Jwt jwt, String claimName) {
    List<String> values = jwt.getClaimAsStringList(claimName);
    return values == null
        ? List.of()
        : values.stream()
        .filter(Objects::nonNull)
        .filter(value -> !value.isBlank())
        .distinct()
        .toList();
  }
}