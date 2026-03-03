package uk.gov.laa.springboot.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Configuration for ECS structured logging.
 */
@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnProperty(
        prefix = "laa.springboot.starter.observability",
        name = "enabled",
        havingValue = "true"
)
public class ObservabilityAutoConfiguration {

  @Bean
  public ObservabilityFilter observabilityFilter() {
    return new ObservabilityFilter();
  }
}