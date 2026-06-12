package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ClassUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * Security configuration for OAuth2 JWT authentication and role/scope endpoint authorization.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(Oauth2AuthorizationProperties.class)
public class SecurityFilterChainAutoConfiguration {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String API_KEY_SECURITY_CONFIGURATION_CLASS =
      "uk.gov.laa.springboot.auth.SecurityFilterChainAutoConfiguration";

  /**
   * Creates the endpoint access manager used for role/scope URI checks.
   *
   * @param properties configured starter properties
   * @param objectMapper object mapper used for JSON parsing
   * @return endpoint access manager
   */
  @Bean
  public EndpointAccessManager endpointAccessManager(Oauth2AuthorizationProperties properties,
                                                      ObjectMapper objectMapper) {
    return new EndpointAccessManager(properties, objectMapper);
  }

  /**
   * Creates a JWT authentication converter with configurable role/scope claim mappings.
   *
   * @param properties configured starter properties
   * @return JWT authentication converter
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      Oauth2AuthorizationProperties properties) {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter(properties));
    return converter;
  }

  /**
   * Creates the primary security filter chain for OAuth2 resource server authentication.
   *
   * @param httpSecurity HTTP security builder
   * @param endpointAccessManager endpoint role/scope access manager
   * @param jwtConverter JWT converter for authorities
   * @param authenticationManagerResolver optional JWT authentication manager resolver
   * @return configured filter chain
   * @throws Exception when security chain cannot be built
   */
  @Bean("oauth2SecurityFilterChain")
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @ConditionalOnMissingBean(name = "oauth2SecurityFilterChain")
  public SecurityFilterChain oauth2SecurityFilterChain(
      HttpSecurity httpSecurity,
      EndpointAccessManager endpointAccessManager,
      JwtAuthenticationConverter jwtConverter,
      ObjectProvider<AuthenticationManagerResolver<HttpServletRequest>>
          authenticationManagerResolver,
      ObjectMapper objectMapper)
      throws Exception {
    if (isApiKeyStarterPresent()) {
      httpSecurity.securityMatcher(bearerRequestMatcher());
    }

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(endpointAccessManager.getUnprotectedUris()).permitAll();
          auth.anyRequest().access((authentication, context) ->
              new AuthorizationDecision(
                  isAuthorized(authentication.get(), endpointAccessManager, context.getRequest())));
        })
        .exceptionHandling(exceptionHandling ->
            exceptionHandling
                .authenticationEntryPoint(new Oauth2AuthenticationEntryPoint(objectMapper))
                .accessDeniedHandler(new Oauth2AccessDeniedHandler(objectMapper)))
        .oauth2ResourceServer(resourceServerConfigurer(
            authenticationManagerResolver.getIfAvailable(), jwtConverter));

    return httpSecurity.build();
  }

  private Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> resourceServerConfigurer(
      AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver,
      JwtAuthenticationConverter jwtConverter) {
    return oauth2 -> {
      if (authenticationManagerResolver != null) {
        oauth2.authenticationManagerResolver(authenticationManagerResolver);
      } else {
        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter));
      }
    };
  }

  private boolean isAuthorized(Authentication authentication,
                               EndpointAccessManager endpointAccessManager,
                               jakarta.servlet.http.HttpServletRequest request) {
    return authentication != null
        && authentication.isAuthenticated()
        && endpointAccessManager.isRequestAuthorized(authentication.getAuthorities(), request);
  }

  private RequestMatcher bearerRequestMatcher() {
    return request -> {
      String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      return authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX);
    };
  }

  private boolean isApiKeyStarterPresent() {
    return ClassUtils.isPresent(
        API_KEY_SECURITY_CONFIGURATION_CLASS, getClass().getClassLoader());
  }
}
