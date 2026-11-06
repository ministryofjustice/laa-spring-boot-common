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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.laa.springboot.oauth2.utils.SecurityHelper;

/**
 * Filter that validates a supplementary JWT header and merges configured claims into the existing
 * {@link Authentication}.
 *
 * <p>Logic:
 *
 * <ol>
 *   <li>Read the configured supplementary-token header; absent → continue with OBO authentication.
 *   <li>Decode and validate the JWT via the injected {@code xAuthorizationJwtDecoder}; invalid →
 *       401.
 *   <li>Compare the Entra {@code oid} claim against the existing OBO JWT; mismatch → 401.
 *   <li>Merge the configured supported claims into the OBO JWT.
 *   <li>Merge configured role-prefix authorities into a JWT-backed {@link Authentication} and
 *       update the {@link SecurityContextHolder}.
 * </ol>
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class XAuthorizationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(XAuthorizationFilter.class);

  public static final String DEFAULT_HEADER = "X-Authorization";
  private static final Set<String> SUPPORTED_CLAIMS = Set.of(
      SecurityHelper.LAA_APP_ROLES_CLAIM,
      SecurityHelper.LAA_ACCOUNTS_CLAIM,
      SecurityHelper.PROFILE_ID_CLAIM,
      SecurityHelper.FIRM_CODE_CLAIM);
  private final JwtDecoder secondaryAuthorizationJwtDecoder;
  private final String roleAuthorityPrefix;
  private final String authorizationHeader;
  private final Set<String> claimsToMerge;

  /**
   * Creates a filter using the default supplementary-token header and entitlement claims.
   *
   * @param secondaryAuthorizationJwtDecoder decoder for supplementary tokens
   * @param roleAuthorityPrefix prefix for role authorities
   */
  public XAuthorizationFilter(
      JwtDecoder secondaryAuthorizationJwtDecoder, String roleAuthorityPrefix) {
    this(
        secondaryAuthorizationJwtDecoder,
        roleAuthorityPrefix,
        DEFAULT_HEADER,
        List.of(SecurityHelper.LAA_APP_ROLES_CLAIM, SecurityHelper.LAA_ACCOUNTS_CLAIM));
  }

  /**
   * Creates a filter using the supplied supplementary-token header and claim allow-list.
   *
   * @param secondaryAuthorizationJwtDecoder decoder for supplementary tokens
   * @param roleAuthorityPrefix prefix for role authorities
   * @param authorizationHeader request header carrying the supplementary token
   * @param claimsToMerge supported claims to merge from the supplementary token
   */
  public XAuthorizationFilter(
      JwtDecoder secondaryAuthorizationJwtDecoder,
      String roleAuthorityPrefix,
      String authorizationHeader,
      List<String> claimsToMerge) {
    this.secondaryAuthorizationJwtDecoder = secondaryAuthorizationJwtDecoder;
    this.roleAuthorityPrefix = roleAuthorityPrefix;
    if (!StringUtils.hasText(authorizationHeader)) {
      throw new IllegalArgumentException("X-Authorization header must not be blank");
    }
    this.authorizationHeader = authorizationHeader.trim();
    this.claimsToMerge = validateClaims(claimsToMerge);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String token = request.getHeader(authorizationHeader);
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

    if (claimsToMerge.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }

    List<String> appRoles = claimsToMerge.contains(SecurityHelper.LAA_APP_ROLES_CLAIM)
        ? claimAsNonBlankDistinctStrings(authorizationJwt, SecurityHelper.LAA_APP_ROLES_CLAIM)
        : List.of();
    List<String> laaAccounts = claimsToMerge.contains(SecurityHelper.LAA_ACCOUNTS_CLAIM)
        ? claimAsNonBlankDistinctStrings(authorizationJwt, SecurityHelper.LAA_ACCOUNTS_CLAIM)
        : List.of();
    if (containsOnlyEntitlementClaims() && appRoles.isEmpty() && laaAccounts.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }
    Set<GrantedAuthority> mergedAuthorities = new HashSet<>(existingAuth.getAuthorities());
    appRoles.forEach(
        role -> mergedAuthorities.add(new SimpleGrantedAuthority(roleAuthorityPrefix + role)));

    JwtAuthenticationToken merged =
        new JwtAuthenticationToken(
            mergeEntitlementClaims(
                oboAuthentication.getToken(), authorizationJwt, appRoles, laaAccounts),
            mergedAuthorities,
            existingAuth.getName());
    merged.setDetails(existingAuth.getDetails());
    SecurityContextHolder.getContext().setAuthentication(merged);

    chain.doFilter(request, response);
  }

  private Jwt mergeEntitlementClaims(
      Jwt oboJwt, Jwt authorizationJwt, List<String> appRoles, List<String> laaAccounts) {
    Map<String, Object> claims = new LinkedHashMap<>(oboJwt.getClaims());
    claimsToMerge.forEach(claimName -> mergeClaim(
        claims, authorizationJwt, claimName, appRoles, laaAccounts));

    Jwt.Builder builder =
        Jwt.withTokenValue(oboJwt.getTokenValue())
            .headers(headers -> headers.putAll(oboJwt.getHeaders()))
            .claims(mergedClaims -> mergedClaims.putAll(claims));

    if (oboJwt.getIssuedAt() != null) {
      builder.issuedAt(oboJwt.getIssuedAt());
    }

    if (oboJwt.getExpiresAt() != null) {
      builder.expiresAt(oboJwt.getExpiresAt());
    }
    return builder.build();
  }

  private void mergeClaim(
      Map<String, Object> claims,
      Jwt authorizationJwt,
      String claimName,
      List<String> appRoles,
      List<String> laaAccounts) {
    claims.remove(claimName);
    if (SecurityHelper.LAA_APP_ROLES_CLAIM.equals(claimName)) {
      claims.put(claimName, appRoles);
    } else if (SecurityHelper.LAA_ACCOUNTS_CLAIM.equals(claimName)) {
      claims.put(claimName, laaAccounts);
    } else {
      String value = claimAsNonBlankString(authorizationJwt, claimName);
      if (value != null) {
        claims.put(claimName, value);
      }
    }
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

  private String claimAsNonBlankString(Jwt jwt, String claimName) {
    String value = jwt.getClaimAsString(claimName);
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private boolean containsOnlyEntitlementClaims() {
    return claimsToMerge.stream().allMatch(claimName ->
        SecurityHelper.LAA_APP_ROLES_CLAIM.equals(claimName)
            || SecurityHelper.LAA_ACCOUNTS_CLAIM.equals(claimName));
  }

  private Set<String> validateClaims(List<String> configuredClaims) {
    if (configuredClaims == null) {
      throw new IllegalArgumentException("X-Authorization claims must not be null");
    }
    Set<String> claims = new HashSet<>(configuredClaims);
    if (claims.stream().anyMatch(
        claim -> !StringUtils.hasText(claim) || !SUPPORTED_CLAIMS.contains(claim))) {
      throw new IllegalArgumentException(
          "X-Authorization claims must be selected from " + SUPPORTED_CLAIMS);
    }
    return Set.copyOf(claims);
  }
}