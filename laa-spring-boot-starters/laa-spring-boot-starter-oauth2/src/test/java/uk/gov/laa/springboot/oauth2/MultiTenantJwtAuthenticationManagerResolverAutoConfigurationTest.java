package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class MultiTenantJwtAuthenticationManagerResolverAutoConfigurationTest {

  private static final String TRUSTED_ISSUER = "https://login.microsoftonline.com/tenant-one/v2.0";
  private static final String UNTRUSTED_ISSUER = "https://login.microsoftonline.com/tenant-two/v2.0";
  private static final String AUDIENCE = "api://laa-data";

  /**
   * A trusted issuer should not trigger tenant-specific decoder construction during application
   * startup. The resolver should inspect the bearer token issuer first, then create the matching
   * authentication manager only when the first request for that issuer is authenticated.
   */
  @Test
  void createsAuthenticationManagersLazilyForTrustedIssuers() {
    TestAutoConfiguration configuration = new TestAutoConfiguration();

    AuthenticationManagerResolver<HttpServletRequest> resolver =
        configuration.jwtAuthenticationManagerResolver(properties(), new JwtAuthenticationConverter());

    assertThat(configuration.authenticationManagerCreations).hasValue(0);

    resolver
        .resolve(new MockHttpServletRequest())
        .authenticate(new BearerTokenAuthenticationToken(tokenForIssuer(TRUSTED_ISSUER)));

    assertThat(configuration.authenticationManagerCreations).hasValue(1);
  }

  /**
   * A token whose issuer is not present in the starter's trusted tenant list must fail at resolver
   * level. This protects the application from accepting tokens issued by another tenant, and avoids
   * constructing any tenant authentication manager for an issuer we do not trust.
   */
  @Test
  void rejectsUntrustedIssuers() {
    TestAutoConfiguration configuration = new TestAutoConfiguration();
    AuthenticationManagerResolver<HttpServletRequest> resolver =
        configuration.jwtAuthenticationManagerResolver(properties(), new JwtAuthenticationConverter());

    assertThatThrownBy(
            () ->
                resolver
                    .resolve(new MockHttpServletRequest())
                    .authenticate(new BearerTokenAuthenticationToken(tokenForIssuer(UNTRUSTED_ISSUER))))
        .isInstanceOf(InvalidBearerTokenException.class);

    assertThat(configuration.authenticationManagerCreations).hasValue(0);
  }

  /**
   * The same untrusted-issuer rule must apply even when the bearer token is a signed JWS rather
   * than an unsigned/plain JWT. A valid-looking signature should not matter unless the token issuer
   * is one of the configured trusted tenant issuers.
   */
  @Test
  void rejectsSignedTokensFromUntrustedIssuers() throws Exception {
    TestAutoConfiguration configuration = new TestAutoConfiguration();
    AuthenticationManagerResolver<HttpServletRequest> resolver =
        configuration.jwtAuthenticationManagerResolver(properties(), new JwtAuthenticationConverter());

    assertThatThrownBy(
            () ->
                resolver
                    .resolve(new MockHttpServletRequest())
                    .authenticate(
                        new BearerTokenAuthenticationToken(signedTokenForIssuer(UNTRUSTED_ISSUER))))
        .isInstanceOf(InvalidBearerTokenException.class);

    assertThat(configuration.authenticationManagerCreations).hasValue(0);
  }

  private static Oauth2AuthorizationProperties properties() {
    MultiTenantJwtProperties.Tenant tenant = new MultiTenantJwtProperties.Tenant();
    tenant.setIssuerUri(TRUSTED_ISSUER);
    tenant.setAudiences(List.of(AUDIENCE));

    Oauth2AuthorizationProperties properties = new Oauth2AuthorizationProperties();
    properties.getResourceserver().getJwt().setTenants(List.of(tenant));
    return properties;
  }

  private static String tokenForIssuer(String issuer) {
    return new PlainJWT(new JWTClaimsSet.Builder().issuer(issuer).build()).serialize();
  }

  private static String signedTokenForIssuer(String issuer) throws Exception {
    SignedJWT jwt =
        new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            new JWTClaimsSet.Builder().issuer(issuer).build());
    jwt.sign(new MACSigner("01234567890123456789012345678901"));
    return jwt.serialize();
  }

  private static class TestAutoConfiguration
      extends MultiTenantJwtAuthenticationManagerResolverAutoConfiguration {
    private final AtomicInteger authenticationManagerCreations = new AtomicInteger();

    @Override
    AuthenticationManager authenticationManager(
        String issuer, List<String> audiences, JwtAuthenticationConverter jwtAuthenticationConverter) {
      authenticationManagerCreations.incrementAndGet();
      return authentication -> authentication;
    }
  }
}
