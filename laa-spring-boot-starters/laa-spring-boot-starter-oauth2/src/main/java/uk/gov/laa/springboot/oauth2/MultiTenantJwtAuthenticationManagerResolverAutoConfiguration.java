package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.util.Assert;

/** Auto-configuration for multi-tenant OAuth2 resource-server JWT authentication. */
@AutoConfiguration
@EnableConfigurationProperties(Oauth2AuthorizationProperties.class)
public class MultiTenantJwtAuthenticationManagerResolverAutoConfiguration {

  /**
   * Creates a JWT authentication manager resolver that trusts each configured tenant issuer and
   * audience.
   *
   * @param properties configured OAuth2 starter properties
   * @param jwtAuthenticationConverter converter for JWT authorities
   * @return multi-tenant JWT authentication manager resolver
   */
  @Bean
  @ConditionalOnMissingBean({AuthenticationManagerResolver.class, JwtDecoder.class})
  public AuthenticationManagerResolver<HttpServletRequest> jwtAuthenticationManagerResolver(
      Oauth2AuthorizationProperties properties,
      JwtAuthenticationConverter jwtAuthenticationConverter) {
    Map<String, AuthenticationManager> authenticationManagers =
        new ConcurrentHashMap<>();
    Map<String, List<String>> audiencesByIssuer =
        tenants(properties).stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    MultiTenantJwtProperties.Tenant::getIssuerUri,
                    tenant -> List.copyOf(tenant.getAudiences())));

    return new JwtIssuerAuthenticationManagerResolver(
        issuer -> {
          List<String> audiences = audiencesByIssuer.get(issuer);
          if (audiences == null) {
            return null;
          }

          return authenticationManagers.computeIfAbsent(
              issuer, trustedIssuer -> authenticationManager(
                  trustedIssuer, audiences, jwtAuthenticationConverter));
        });
  }

  private List<MultiTenantJwtProperties.Tenant> tenants(Oauth2AuthorizationProperties properties) {
    List<MultiTenantJwtProperties.Tenant> tenants =
        properties.getResourceserver().getJwt().getTenants();
    Assert.notEmpty(tenants, "At least one trusted JWT tenant must be configured");

    tenants.forEach(
        tenant -> {
          Assert.hasText(tenant.getIssuerUri(), "JWT tenant issuer-uri is required");
          Assert.notEmpty(tenant.getAudiences(), "JWT tenant audiences are required");
        });
    return tenants;
  }

  AuthenticationManager authenticationManager(
      String issuer,
      List<String> audiences,
      JwtAuthenticationConverter jwtAuthenticationConverter) {
    JwtAuthenticationProvider authenticationProvider =
        new JwtAuthenticationProvider(jwtDecoderForTenant(issuer, audiences));
    authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
    return new ProviderManager(authenticationProvider);
  }

  private JwtDecoder jwtDecoderForTenant(String issuer, List<String> audiences) {
    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer), audienceValidator(audiences)));
    return decoder;
  }

  OAuth2TokenValidator<Jwt> audienceValidator(List<String> allowedAudiences) {
    return jwt -> {
      if (jwt.getAudience().stream().anyMatch(allowedAudiences::contains)) {
        return OAuth2TokenValidatorResult.success();
      }

      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "JWT audience is not trusted", null));
    };
  }
}
