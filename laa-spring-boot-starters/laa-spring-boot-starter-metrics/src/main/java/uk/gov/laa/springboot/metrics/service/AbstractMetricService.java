package uk.gov.laa.springboot.metrics.service;

import io.prometheus.metrics.core.metrics.Metric;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
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

  protected abstract T buildMetric(String metricName, String help);

  public void register(String metricName, String help) {
    T metric = buildMetric(metricName, help);
    metrics.put(metricName, metric);
  }
}
