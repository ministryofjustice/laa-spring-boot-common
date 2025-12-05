package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.MetricsProperties;

/**
 * Stores histogram metrics in the Prometheus registry.
 *
 * @author Jamie Briggs
 */
@Component
public class HistogramMetricService extends AbstractMetricService<Histogram> {

  public HistogramMetricService(
      PrometheusRegistry prometheusRegistry, MetricsProperties metricsProperties) {
    super(prometheusRegistry, metricsProperties);
  }

  @Override
  protected Histogram buildMetric(String metricName, String help, String... labels) {
    return Histogram.builder()
        .labelNames(labels)
        .name(metricName).help(metricName)
        .register(prometheusRegistry);
  }

  public Timer startTimer(String metricName, String... labelValues) {
    var metric = getMetric(metricName);
    return metric.labelValues(labelValues).startTimer();
  }

  public void recordValue(String metricName, double value, String... labelValues) {
    var metric = getMetric(metricName);
    metric.labelValues(labelValues).observe(value);
  }

}
