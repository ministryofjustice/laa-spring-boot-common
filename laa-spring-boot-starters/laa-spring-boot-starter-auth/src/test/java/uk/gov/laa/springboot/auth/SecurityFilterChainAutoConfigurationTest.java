package uk.gov.laa.springboot.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

class SecurityFilterChainAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(SecurityFilterChainAutoConfiguration.class))
      .withBean("customSecurityFilterChain", SecurityFilterChain.class,
          TestSecurityFilterChain::new)
      .withPropertyValues(
          "laa.springboot.starter.auth.authentication-header=Authorization",
          "laa.springboot.starter.auth.authorized-clients=["
              + "{\"name\":\"client\",\"roles\":[\"GROUP1\"],\"token\":\"token\"}]",
          "laa.springboot.starter.auth.authorized-roles=["
              + "{\"name\":\"GROUP1\",\"uris\":[\"/api/v1/books/**\"]}]",
          "laa.springboot.starter.auth.unprotected-uris[0]=/actuator/**");

  @Test
  void backsOffDefaultSecurityFilterChainWhenApplicationDefinesOne() {
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(SecurityFilterChain.class);
      assertThat(context).hasBean("customSecurityFilterChain");
      assertThat(context).hasSingleBean(TokenDetailsManager.class);
      assertThat(context).hasSingleBean(ApiAuthenticationProvider.class);
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
