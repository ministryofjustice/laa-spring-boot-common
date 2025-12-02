package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.metrics.Metric;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.Arrays;
import java.util.HashMap;
import org.springframework.stereotype.Component;

/**
 * A generic abstract class designed to manage metrics of a specific type. The class is
 * parameterized to support various implementations of metrics defined in the application.
 *
 * @param <T> the type of metric that this service will handle, constrained to types extending
 *            {@link Metric}
 * @author Jamie Briggs
 */
@Component
public abstract class AbstractMetricService<T extends Metric> {

  protected final PrometheusRegistry prometheusRegistry;
  protected final HashMap<String, T> metrics = new HashMap<>();

  protected AbstractMetricService(PrometheusRegistry prometheusRegistry) {
    this.prometheusRegistry = prometheusRegistry;
  }

  protected abstract T buildMetric(String metricName, String help, String... labels);

  /**
   * Registers a metric with the Prometheus registry.
   *
   * @param metricName the name of the metric to register
   * @param help the help text for the metric
   * @param labels the labels for the metric
   */
  public void register(String metricName, String help, String... labels) {
    String[] labelNames =
        Arrays.stream(labels).map(x -> x.split("=")[0]).toList()
            .toArray(String[]::new);
    T metric = buildMetric(metricName, help, labelNames);
    metrics.put(metricName, metric);
  }

  public T getMetric(String metricName) {
    return metrics.get(metricName);
  }
}
