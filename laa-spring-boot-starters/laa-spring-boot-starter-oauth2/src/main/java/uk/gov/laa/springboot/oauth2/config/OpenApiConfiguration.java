package uk.gov.laa.springboot.oauth2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

  private static final String SECURITY_SCHEME_NAME = "BearerAuth";
  private static final String BEARER_SCHEME = "bearer";
  private static final String BEARER_FORMAT = "JWT";

  /**
   * Configures the OpenAPI spec with a bearer security scheme for all endpoints.
   *
   * @return configured OpenAPI object
   */
  @Bean("bearerOpenApi")
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI bearerOpenApi() {
    OpenAPI openApiSpec =
        new OpenAPI()
            .components(
                new Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme(BEARER_SCHEME)
                            .bearerFormat(BEARER_FORMAT)))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    log.info("OpenAPI Security Scheme '{}' added for all endpoints.", SECURITY_SCHEME_NAME);
    return openApiSpec;
  }
}
