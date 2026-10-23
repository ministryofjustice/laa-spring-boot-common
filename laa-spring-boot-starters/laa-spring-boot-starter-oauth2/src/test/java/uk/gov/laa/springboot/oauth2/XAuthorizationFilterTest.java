package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class XAuthorizationFilterTest {

  private static final String OID = "user-1";

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void mergesXAuthorizationRolesAndAccountsIntoOboAuthentication() throws Exception {
    Jwt oboJwt = jwt("obo-token", Map.of("oid", OID));
    Jwt authorizationJwt = jwt(
        "x-authorization-token",
        Map.of(
            "oid", OID,
            "LAA_APP_ROLES", List.of("CASEWORKER", "CASEWORKER", " "),
            "LAA_ACCOUNTS", List.of("account-1", "account-1")));
    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(
            oboJwt, List.of(new SimpleGrantedAuthority("SCOPE_read")), "caller"));
    XAuthorizationFilter filter = new XAuthorizationFilter(token -> authorizationJwt, "ROLE_");
    AtomicBoolean chainCalled = new AtomicBoolean();

    filter.doFilter(
        requestWithXAuthorization("x-authorization-token"),
        new MockHttpServletResponse(),
        calledFilterChain(chainCalled));

    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    assertThat(chainCalled).isTrue();
    assertThat(authentication.getAuthorities())
        .extracting(authority -> authority.getAuthority())
        .containsExactlyInAnyOrder("SCOPE_read", "ROLE_CASEWORKER");
    assertThat(authentication.getToken().getClaimAsStringList("LAA_APP_ROLES"))
        .containsExactly("CASEWORKER");
    assertThat(authentication.getToken().getClaimAsStringList("LAA_ACCOUNTS"))
        .containsExactly("account-1");
  }

  @Test
  void continuesWithoutXAuthorizationHeader() throws Exception {
    JwtAuthenticationToken authentication = authentication(jwt("obo-token", Map.of("oid", OID)));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    XAuthorizationFilter filter = new XAuthorizationFilter(token -> jwt(token, Map.of()), "ROLE_");
    AtomicBoolean chainCalled = new AtomicBoolean();

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), calledFilterChain(chainCalled));

    assertThat(chainCalled).isTrue();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
  }

  @Test
  void rejectsInvalidXAuthorizationToken() throws Exception {
    JwtDecoder decoder = token -> {
      throw new BadJwtException("invalid");
    };
    XAuthorizationFilter filter = new XAuthorizationFilter(decoder, "ROLE_");
    SecurityContextHolder.getContext().setAuthentication(authentication(jwt("obo-token", Map.of("oid", OID))));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(requestWithXAuthorization("invalid"), response, calledFilterChain(new AtomicBoolean()));

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void rejectsXAuthorizationTokenWhenExistingAuthenticationIsNotJwtBacked() throws Exception {
    XAuthorizationFilter filter = new XAuthorizationFilter(
        token -> jwt(token, Map.of("oid", OID)), "ROLE_");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("caller", "password"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(requestWithXAuthorization("x-token"), response, calledFilterChain(new AtomicBoolean()));

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void rejectsXAuthorizationTokenForAnotherUser() throws Exception {
    XAuthorizationFilter filter = new XAuthorizationFilter(
        token -> jwt(token, Map.of("oid", "another-user")), "ROLE_");
    SecurityContextHolder.getContext().setAuthentication(authentication(jwt("obo-token", Map.of("oid", OID))));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(requestWithXAuthorization("x-token"), response, calledFilterChain(new AtomicBoolean()));

    assertThat(response.getStatus()).isEqualTo(401);
  }

  private JwtAuthenticationToken authentication(Jwt jwt) {
    return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_EXISTING")));
  }

  private MockHttpServletRequest requestWithXAuthorization(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Authorization", token);
    return request;
  }

  private FilterChain calledFilterChain(AtomicBoolean chainCalled) {
    return (request, response) -> chainCalled.set(true);
  }

  private Jwt jwt(String token, Map<String, Object> claims) {
    return Jwt.withTokenValue(token)
        .header("alg", "none")
        .claim("sub", "caller")
        .claims(jwtClaims -> jwtClaims.putAll(claims))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }
}
