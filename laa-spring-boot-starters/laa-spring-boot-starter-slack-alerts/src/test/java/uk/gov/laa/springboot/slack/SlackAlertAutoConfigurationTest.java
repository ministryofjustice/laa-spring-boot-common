package uk.gov.laa.springboot.slack;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SlackAlertAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(SlackAlertAutoConfiguration.class));

  @Test
  void createsBeansWhenEnabled() {
    contextRunner
        .withPropertyValues(
            "laa.springboot.starter.slack-alerts.webhook=https://hooks.slack.test/12345")
        .run(
            context -> {
              assertThat(context).hasSingleBean(SlackNotifier.class);
              assertThat(context).hasSingleBean(AlertService.class);
            });
  }

  @Test
  void doesNotCreateBeansWhenDisabled() {
    contextRunner
        .withPropertyValues("laa.springboot.starter.slack-alerts.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(SlackNotifier.class);
              assertThat(context).doesNotHaveBean(AlertService.class);
            });
  }
}
