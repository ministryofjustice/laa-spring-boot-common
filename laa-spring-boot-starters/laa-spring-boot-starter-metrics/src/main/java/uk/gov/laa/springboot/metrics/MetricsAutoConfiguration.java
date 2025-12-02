package uk.gov.laa.springboot.metrics;

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
@ConditionalOnProperty(prefix = "laa.springboot.starter.metrics", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MetricsProperties.class)
public class MetricsAutoConfiguration {

}
