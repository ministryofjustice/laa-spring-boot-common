package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;


/**
 * Configuration of security filter chains to determine authentication behavior per endpoint (group).
 * See <a href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#jc-httpsecurity">HTTP Security Configuration</a>.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ComponentScan
@EnableConfigurationProperties(AuthenticationProperties.class)
public class SecurityFilterChainAutoConfiguration {

    private final AuthenticationProperties authenticationProperties;

    private final ApiAuthenticationService apiAuthenticationService;

    private Set<AuthorizedRole> authorizedRoles;

    private final ObjectMapper objectMapper;

    @Autowired
    public SecurityFilterChainAutoConfiguration(AuthenticationProperties authenticationProperties,
                                                ApiAuthenticationService apiAuthenticationService,
                                                ObjectMapper objectMapper) {
        this.authenticationProperties = authenticationProperties;
        this.apiAuthenticationService = apiAuthenticationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Initialise a set of {@link AuthorizedRole} from those configured as a JSON string in the application properties.
     */
    @PostConstruct
    private void initialise() {

        try {
            authorizedRoles = new ObjectMapper().readValue(authenticationProperties.getAuthorizedRoles()
                    , new TypeReference<Set<AuthorizedRole>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (authorizedRoles.isEmpty()) throw new InvalidPropertyException(AuthenticationProperties.class,
                "authorizedRoles", "At least one authorized role must be provided.");

        for (AuthorizedRole authorizedRole : authorizedRoles) {
            log.info("Authorized Role Registered: '{}'", authorizedRole.name());
        }
    }


    /**
     * First security filter chain to allow requests to unprotected URLs regardless of whether authentication
     * credentials have been provided.
     *
     * @param httpSecurity web based security configuration customizer
     * @return The {@link SecurityFilterChain} to continue with successive security filters.
     * @throws Exception -
     */
    @Bean
    @Order(1)
    public SecurityFilterChain filterUnprotectedURIs(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.securityMatcher(authenticationProperties.getUnprotectedURIs())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return httpSecurity.build();

    }

    /**
     * Second security filter chain to authenticate against endpoints based on roles configured in application
     * properties.
     *
     * @param httpSecurity web based security configuration customizer
     * @return The {@link SecurityFilterChain} to continue with successive security filters.
     * @throws Exception -
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterProtectedURIs(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(customizer -> {
                        for (AuthorizedRole authorizedRole : authorizedRoles) {
                            customizer.requestMatchers(authorizedRole.URIs()).hasRole(authorizedRole.name());
                        }
                        // Deny requests to any other endpoint by default
                        customizer.anyRequest().denyAll();
                    }
                );

        ApiAuthenticationFilter apiAuthenticationFilter = new ApiAuthenticationFilter(apiAuthenticationService,
                objectMapper);

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(apiAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> {
                exceptionHandling.accessDeniedHandler(new ApiAccessDeniedHandler(objectMapper));
            });

        return httpSecurity.build();

    }

}
