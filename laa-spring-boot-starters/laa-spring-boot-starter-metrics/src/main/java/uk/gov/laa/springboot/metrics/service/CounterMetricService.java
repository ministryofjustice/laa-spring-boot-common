package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.stereotype.Component;

/**
 * Stores histogram metrics in the Prometheus registry.
 *
 * @author Jamie Briggs
 */
@Component
public class CounterMetricService extends AbstractMetricService<Counter> {

  public CounterMetricService(
      PrometheusRegistry prometheusRegistry) {
    super(prometheusRegistry);
  }

  @Override
  protected Counter buildMetric(String metricName, String help, String... labels) {
    return Counter.builder()
        .name(metricName)
        .labelNames(labels)
        .help(help)
        .register(prometheusRegistry);
  }

  public void increment(String metricName, double amount, String... labelValues) {
    this.metrics.get(metricName).labelValues(labelValues).inc(amount);
  }

}
