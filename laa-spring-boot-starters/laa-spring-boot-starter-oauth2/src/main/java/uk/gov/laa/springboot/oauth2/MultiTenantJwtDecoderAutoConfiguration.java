package uk.gov.laa.springboot.oauth2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;

/** Auto-configuration for multi-tenant OAuth2 resource-server JWT decoding. */
@AutoConfiguration
@EnableConfigurationProperties(Oauth2AuthorizationProperties.class)
public class MultiTenantJwtDecoderAutoConfiguration {

  /**
   * Creates a JWT decoder that trusts each configured tenant issuer and audience.
   *
   * @param properties configured OAuth2 starter properties
   * @return multi-tenant JWT decoder
   */
  @Bean
  @ConditionalOnMissingBean(JwtDecoder.class)
  public JwtDecoder jwtDecoder(Oauth2AuthorizationProperties properties) {
    Map<String, List<String>> audiencesByIssuer = audiencesByIssuer(properties);

    return new MultiTenantJwtDecoder(
        List.copyOf(audiencesByIssuer.keySet()),
        issuer -> jwtDecoderForIssuer(issuer, audiencesByIssuer.get(issuer)));
  }

  private Map<String, List<String>> audiencesByIssuer(Oauth2AuthorizationProperties properties) {
    List<MultiTenantJwtProperties.Tenant> tenants =
        properties.getResourceserver().getJwt().getTenants();
    Assert.notEmpty(tenants, "At least one trusted JWT tenant must be configured");

    return tenants.stream()
        .peek(tenant -> Assert.hasText(tenant.getIssuerUri(), "JWT tenant issuer-uri is required"))
        .peek(tenant -> Assert.notEmpty(tenant.getAudiences(), "JWT tenant audiences are required"))
        .collect(
            Collectors.toUnmodifiableMap(
                MultiTenantJwtProperties.Tenant::getIssuerUri,
                tenant -> List.copyOf(tenant.getAudiences())));
  }

  private JwtDecoder jwtDecoderForIssuer(String issuer, List<String> audiences) {
    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer), audienceValidator(audiences)));
    return decoder;
  }

  private OAuth2TokenValidator<Jwt> audienceValidator(List<String> allowedAudiences) {
    return jwt -> {
      if (jwt.getAudience().stream().anyMatch(allowedAudiences::contains)) {
        return OAuth2TokenValidatorResult.success();
      }

      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "JWT audience is not trusted", null));
    };
  }
}
