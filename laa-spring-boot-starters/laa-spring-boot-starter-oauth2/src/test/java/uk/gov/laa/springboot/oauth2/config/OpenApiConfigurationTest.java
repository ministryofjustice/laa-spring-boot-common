package uk.gov.laa.springboot.oauth2.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenApiConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withBean(OpenApiConfiguration.class);

  private static final String OPEN_API_CONFIGURATION_BEAN = "openApiConfiguration";
  private static final String OPEN_API_BEAN = "bearerOpenApi";
  private static final String SECURITY_SCHEME_NAME = "BearerAuth";

  @Test
  void openApiBeanCreatedWhenPropertyOmitted() {
    contextRunner.run(context -> {
      assertThat(context).hasBean(OPEN_API_CONFIGURATION_BEAN);
      assertSecuritySchemeApplied(context);
    });
  }

  @Test
  void openApiBeanCreatedWhenPropertyEnabled() {
    contextRunner
        .withPropertyValues("laa.springboot.starter.open-api.security-scheme.enabled=true")
        .run(context -> {
          assertThat(context).hasBean(OPEN_API_CONFIGURATION_BEAN);
          assertSecuritySchemeApplied(context);
        });
  }

  @Test
  void noOpenApiBeanWhenPropertyDisabled() {
    contextRunner
        .withPropertyValues("laa.springboot.starter.open-api.security-scheme.enabled=false")
        .run(context -> {
          assertThat(context).doesNotHaveBean(OPEN_API_CONFIGURATION_BEAN);
          assertThat(context).doesNotHaveBean(OPEN_API_BEAN);
        });
  }

  @Test
  void openApiBeanBacksOffWhenOneAlreadyExists() {
    OpenAPI existingOpenApi = new OpenAPI();

    contextRunner.withBean("existingOpenApi", OpenAPI.class, () -> existingOpenApi)
        .run(context -> {
          assertThat(context).hasSingleBean(OpenAPI.class);
          assertThat(context).doesNotHaveBean(OPEN_API_BEAN);
          assertThat(context.getBean(OpenAPI.class)).isSameAs(existingOpenApi);
        });
  }

  private void assertSecuritySchemeApplied(AssertableApplicationContext context) {
    OpenAPI openApiSpec = context.getBean(OPEN_API_BEAN, OpenAPI.class);
    assertThat(openApiSpec.getComponents().getSecuritySchemes()).isEqualTo(
        Map.of(SECURITY_SCHEME_NAME, expectedSecurityScheme()));
  }

  private SecurityScheme expectedSecurityScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT");
  }
}
