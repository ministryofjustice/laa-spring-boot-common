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

  protected HistogramMetricService(
      PrometheusRegistry prometheusRegistry) {
    super(prometheusRegistry);
  }

  @Override
  protected Histogram buildMetric(String metricName, String help) {
    return Histogram.builder().name(metricName).help(metricName)
        .register(prometheusRegistry);
  }

  public Timer startTimer(String metricName) {
    return metrics.get(metricName).startTimer();
  }
}
