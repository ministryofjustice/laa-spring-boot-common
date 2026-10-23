package uk.gov.laa.springboot.oauth2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.laa.springboot.oauth2.utils.JwtValidation;

/**
 * Spring configuration for the X-Authorization header JWT validation.
 *
 * <p>This configuration class — and all beans it declares — only exist in the Spring context when
 * {@code feature.disable-security=false}. When the flag is off, neither the decoder nor the filter
 * bean is created, and the filter is never registered in the security chain.
 */
@Configuration
public class XAuthorizationConfig {

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String issuerUri;

  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  /**
   * A second {@link NimbusJwtDecoder} for the X-Authorization token.
   *
   * <p>Uses the same JWK set URI and issuer URI as the primary decoder (same Entra tenant). No
   * audience validator — the access token may be issued against any of several client
   * registrations.
   *
   * @return a configured JwtDecoder bean for X-Authorization tokens
   */
  @Bean("xAuthorizationJwtDecoder")
  public JwtDecoder xAuthorizationJwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    decoder.setJwtValidator(JwtValidation.requiringExpiryWithIssuer(issuerUri));
    return decoder;
  }

  /**
   * Filter bean that validates the X-Authorization header and merges roles into the existing {@link
   * org.springframework.security.core.context.SecurityContext}.
   *
   * @param secondaryAuthorizationJwtDecoder the decoder for the X-Authorization JWT
   * @return a configured {@link XAuthorizationFilter}
   */
  @Bean("xAuthorizationFilter")
  public OncePerRequestFilter xAuthorizationFilter(
      @Qualifier("xAuthorizationJwtDecoder") JwtDecoder secondaryAuthorizationJwtDecoder) {
    return new XAuthorizationFilter(secondaryAuthorizationJwtDecoder);
  }

  @Bean("xAuthorizationFilterRegistration")
  FilterRegistrationBean<OncePerRequestFilter> xAuthorizationFilterRegistration(
      @Qualifier("xAuthorizationFilter") OncePerRequestFilter xAuthorizationFilter) {

    FilterRegistrationBean<OncePerRequestFilter> registration =
        new FilterRegistrationBean<>(xAuthorizationFilter);
    registration.setEnabled(false);
    return registration;
  }
}
