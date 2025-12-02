package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.stereotype.Component;

/**
 * Stores histogram metrics in the Prometheus registry.
 *
 * @author Jamie Briggs
 */
@Component
public class HistogramMetricService extends AbstractMetricService<Histogram> {

  public HistogramMetricService(
      PrometheusRegistry prometheusRegistry) {
    super(prometheusRegistry);
  }

  @Override
  protected Histogram buildMetric(String metricName, String help, String... labels) {
    return Histogram.builder()
        .labelNames(labels)
        .name(metricName).help(metricName)
        .register(prometheusRegistry);
  }

  public Timer startTimer(String metricName, String... labelValues) {
    var metric = metrics.get(metricName);
    return metric.labelValues(labelValues).startTimer();
  }
}
