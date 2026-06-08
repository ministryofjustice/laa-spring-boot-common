package uk.gov.laa.springboot.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ClassUtils;
import tools.jackson.databind.ObjectMapper;


/**
 * Configuration of security filter chains to determine authentication behavior per endpoint
 * (group). See <a
 * href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#jc-httpsecurity">HTTP
 * Security Configuration</a>.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(AuthenticationProperties.class)
public class SecurityFilterChainAutoConfiguration {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String OAUTH2_SECURITY_CONFIGURATION_CLASS =
      "uk.gov.laa.springboot.oauth2.SecurityFilterChainAutoConfiguration";

  @Bean
  public TokenDetailsManager tokenDetailsManager(AuthenticationProperties properties) {
    return new TokenDetailsManager(properties);
  }

  @Bean
  public ApiAuthenticationProvider apiAuthenticationProvider(
      TokenDetailsManager tokenDetailsManager) {
    return new ApiAuthenticationProvider(tokenDetailsManager);
  }

  /**
   * First security filter chain to allow requests to unprotected URLs regardless of whether
   * authentication credentials have been provided.
   *
   * @param httpSecurity web based security configuration customizer
   * @return The {@link SecurityFilterChain} to continue with successive security filters.
   * @throws Exception -
   */
  @Bean("apiKeySecurityFilterChain")
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  @ConditionalOnMissingBean(name = "apiKeySecurityFilterChain")
  public SecurityFilterChain apiKeySecurityFilterChain(
      HttpSecurity httpSecurity,
      TokenDetailsManager tokenDetailsManager,
      ObjectMapper objectMapper,
      ApiAuthenticationProvider authenticationProvider)
      throws Exception {

    if (isOauth2StarterPresent()) {
      httpSecurity.securityMatcher(apiKeyRequestMatcher(tokenDetailsManager));
    }

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> {
          // Permit access to unprotected URIs
          auth.requestMatchers(tokenDetailsManager.getUnprotectedUris()).permitAll();

          // Apply role-based access for protected URIs
          auth.anyRequest()
              .access((authentication, context) -> {
                Authentication authenticated = authentication.get();
                boolean authorized = authenticated != null
                    && tokenDetailsManager.isRequestAuthorized(
                    authenticated.getAuthorities(), context.getRequest());
                return new AuthorizationDecision(authorized);
              });
        })
        .with(new ApiTokenConfigurer(objectMapper, tokenDetailsManager), Customizer.withDefaults())
        .authenticationProvider(authenticationProvider)
        .exceptionHandling(exceptionHandling ->
            exceptionHandling.accessDeniedHandler(new ApiAccessDeniedHandler(objectMapper)));

    return httpSecurity.build();
  }

  private RequestMatcher apiKeyRequestMatcher(TokenDetailsManager tokenDetailsManager) {
    return request -> {
      String authorizationHeader = request.getHeader(tokenDetailsManager.getAuthenticationHeader());
      return authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX);
    };
  }

  private boolean isOauth2StarterPresent() {
    return ClassUtils.isPresent(
        OAUTH2_SECURITY_CONFIGURATION_CLASS, getClass().getClassLoader());
  }
}
