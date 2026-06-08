package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

class SecurityFilterChainAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(SecurityFilterChainAutoConfiguration.class))
      .withBean(ObjectMapper.class, ObjectMapper::new)
      .withBean("oauth2SecurityFilterChain", SecurityFilterChain.class,
          TestSecurityFilterChain::new)
      .withPropertyValues(
          "laa.springboot.starter.oauth2.authorized-roles=["
              + "{\"name\":\"library-catalogue-service\","
              + "\"uris\":[\"/api/v1/books/**\"]}]");

  @Test
  void backsOffDefaultSecurityFilterChainWhenApplicationDefinesOneWithStarterBeanName() {
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(SecurityFilterChain.class);
      assertThat(context).hasBean("oauth2SecurityFilterChain");
      assertThat(context).hasSingleBean(EndpointAccessManager.class);
      assertThat(context).hasSingleBean(JwtAuthenticationConverter.class);
    });
  }

  private static class TestSecurityFilterChain implements SecurityFilterChain {

    @Override
    public boolean matches(HttpServletRequest request) {
      return true;
    }

    @Override
    public List<Filter> getFilters() {
      return List.of();
    }
  }
}
