package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.stereotype.Component;

/**
 * Stores histogram metrics in the Prometheus registry.
 *
 * @author Jamie Briggs
 */
@Component
public class SummaryMetricService extends AbstractMetricService<Summary> {

  public SummaryMetricService(
      PrometheusRegistry prometheusRegistry) {
    super(prometheusRegistry);
  }

  @Override
  protected Summary buildMetric(String metricName, String help, String... labels) {
    return Summary.builder().name(metricName).help(metricName)
        .labelNames(labels)
        .quantile(0.5, 0.05) // P50 with 5% error tolerance
        .quantile(0.9, 0.02) // P90 with 2% error tolerance
        .quantile(0.95, 0.01) // P95 with 1% error tolerance
        .quantile(0.99, 0.001) // P99 with 0.1% error tolerance
        .register(prometheusRegistry);
  }

  public Timer startTimer(String metricName, String... labelValues) {
    var metric = metrics.get(metricName);
    return metric.labelValues(labelValues).startTimer();
  }

}
