package uk.gov.laa.springboot.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto Configuration for ECS structured logging.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "laa.springboot.starter.observability",
        name = "enabled",
        havingValue = "true"
)
public class ObservabilityAutoConfiguration {

  @Bean
  public EcsTracingFilter observabilityFilter() {
    return new EcsTracingFilter();
  }
}