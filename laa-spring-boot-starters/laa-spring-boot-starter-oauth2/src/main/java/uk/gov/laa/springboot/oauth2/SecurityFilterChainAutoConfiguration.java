package uk.gov.laa.springboot.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

/**
 * Security configuration for OAuth2 JWT authentication and role/scope endpoint authorization.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(Oauth2AuthorizationProperties.class)
public class SecurityFilterChainAutoConfiguration {

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
   * @return configured filter chain
   * @throws Exception when security chain cannot be built
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                 EndpointAccessManager endpointAccessManager,
                                                 JwtAuthenticationConverter jwtConverter,
                                                 ObjectMapper objectMapper)
      throws Exception {
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
            exceptionHandling.authenticationEntryPoint(
                new Oauth2AuthenticationEntryPoint(objectMapper)))
        .oauth2ResourceServer(oauth2 ->
            oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

    return httpSecurity.build();
  }

  private boolean isAuthorized(Authentication authentication,
                               EndpointAccessManager endpointAccessManager,
                               jakarta.servlet.http.HttpServletRequest request) {
    return authentication != null
        && authentication.isAuthenticated()
        && endpointAccessManager.isRequestAuthorized(authentication.getAuthorities(), request);
  }
}
