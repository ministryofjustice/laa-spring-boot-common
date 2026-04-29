package uk.gov.laa.springboot.oauth2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides default OpenAPI security scheme configuration unless disabled.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    value = "laa.springboot.starter.open-api.security-scheme.enabled",
    matchIfMissing = true)
public class OpenApiConfiguration {

  /**
   * Configures the OpenAPI spec with a bearer security scheme for all endpoints.
   *
   * @return configured OpenAPI object
   */
  @Bean
  public OpenAPI openApi() {
    String securitySchemeName = "BearerAuth";
    OpenAPI openApiSpec =
        new OpenAPI()
            .components(
                new Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    log.info("OpenAPI Security Scheme '{}' added for all endpoints.", securitySchemeName);
    return openApiSpec;
  }
}
