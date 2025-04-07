package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                 TokenDetailsManager tokenDetailsManager,
                                                 ObjectMapper objectMapper,
                                                 ApiAuthenticationProvider authenticationProvider)
      throws Exception {

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
          tokenDetailsManager.getAuthorizedRoles().forEach(role ->
              auth.requestMatchers(role.uris()).hasRole(role.name())
          );

          // Deny all other requests not matching the above rules
          auth.anyRequest().denyAll();
        })
        .with(new ApiTokenConfigurer(objectMapper, tokenDetailsManager), Customizer.withDefaults())
        .authenticationProvider(authenticationProvider)
        .exceptionHandling(exceptionHandling ->
            exceptionHandling.accessDeniedHandler(new ApiAccessDeniedHandler(objectMapper)));

    return httpSecurity.build();
  }
}
