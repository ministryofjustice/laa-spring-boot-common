package uk.gov.laa.springboot.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for prometheus metrics.
 *
 * @author Jamie Briggs
 */
@ConfigurationProperties(prefix = "laa.springboot.starter.metrics")
public class MetricsProperties {

  private boolean enabled = true;

  private String metricNamePrefix;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getMetricNamePrefix() {
    return metricNamePrefix;
  }

  public void setMetricNamePrefix(String metricNamePrefix) {
    this.metricNamePrefix = metricNamePrefix;
  }
}
