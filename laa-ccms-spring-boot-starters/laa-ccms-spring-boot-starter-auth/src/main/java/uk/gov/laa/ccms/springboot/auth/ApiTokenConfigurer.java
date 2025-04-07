package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Custom Api token configurer.
 */
public class ApiTokenConfigurer extends AbstractHttpConfigurer<ApiTokenConfigurer, HttpSecurity> {

  private final ObjectMapper objectMapper;

  private final TokenDetailsManager tokenDetailsManager;

  public ApiTokenConfigurer(ObjectMapper objectMapper,
                            TokenDetailsManager tokenDetailsManager) {
    this.objectMapper = objectMapper;
    this.tokenDetailsManager = tokenDetailsManager;
  }

  @Override
  public void configure(HttpSecurity http) {
    var authManager = http.getSharedObject(AuthenticationManager.class);
    var filter =
        new ApiAuthenticationFilter(authManager, objectMapper, tokenDetailsManager);
    http.addFilterBefore(filter, AuthorizationFilter.class);
  }
}
