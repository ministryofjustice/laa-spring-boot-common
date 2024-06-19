package uk.gov.laa.ccms.springboot.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides configuration of the OpenAPI specification unless explicitly disabled.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    value = "laa.ccms.springboot.starter.open-api.security-scheme.enabled",
    matchIfMissing = true)
public class OpenApiConfiguration {

  @Value("${laa.ccms.springboot.starter.auth.authentication-header}")
  String authenticationHeader;

  /**
   * Configures the OpenAPI spec with a security scheme and applies it to all endpoints.
   *
   * @return The configured OpenAPI object.
   */
  @Bean
  public OpenAPI openApi() {
    String securitySchemeName = "ApiKeyAuth";
    OpenAPI openApiSpec =
        new OpenAPI()
            .components(
                new Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name(authenticationHeader)))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    log.info("OpenAPI Security Scheme '{}' added for all endpoints.", securitySchemeName);
    return openApiSpec;
  }
}
