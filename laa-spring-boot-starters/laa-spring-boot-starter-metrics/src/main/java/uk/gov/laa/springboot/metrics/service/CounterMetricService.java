package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.MetricsProperties;

/**
 * Stores histogram metrics in the Prometheus registry.
 *
 * @author Jamie Briggs
 */
@Component
public class CounterMetricService extends AbstractMetricService<Counter> {

  public CounterMetricService(
      PrometheusRegistry prometheusRegistry, MetricsProperties metricsProperties) {
    super(prometheusRegistry, metricsProperties);
  }

  @Override
  protected Counter buildMetric(String metricName, String help, String... labels) {
    return Counter.builder()
        .name(metricName)
        .labelNames(labels)
        .help(help)
        .register(prometheusRegistry);
  }

  /**
   * Increments the counter by the specified amount.
   *
   * @param metricName  the name of the counter to increment
   * @param amount      the amount to increment the counter by
   * @param labelValues the label values to associate with the counter
   */
  public void increment(String metricName, double amount, String... labelValues) {
    this.getMetric(metricName).labelValues(labelValues).inc(amount);
  }

  /**
   * Resets all of the counters back to zero.
   */
  public void resetAll() {
    for (Counter counter : metrics.values()) {
      counter.clear();
    }
  }
}
