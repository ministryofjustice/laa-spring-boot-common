package uk.gov.laa.springboot.slack;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for Slack alerting.
 */
@AutoConfiguration
@ConditionalOnClass(SlackNotifier.class)
@ConditionalOnProperty(prefix = "laa.springboot.starter.slack-alerts", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SlackNotificationProperties.class)
public class SlackAlertAutoConfiguration {

  /**
   * Creates a {@link SlackNotifier} bean if one is not already defined.
   *
   * @param properties  the Slack notification properties
   * @param environment the Spring environment
   * @return the SlackNotifier instance
   */
  @Bean
  @ConditionalOnMissingBean
  public SlackNotifier slackNotifier(
      SlackNotificationProperties properties, Environment environment) {
    return new SlackNotifier(properties, environment);
  }

  /**
   * Creates an {@link AlertService} bean if one is not already defined.
   *
   * @param slackNotifier the Slack notifier used for sending alerts
   * @return the AlertService instance
   */
  @Bean
  @ConditionalOnMissingBean
  public AlertService alertService(SlackNotifier slackNotifier) {
    return new AlertService(slackNotifier);
  }
}
