package uk.gov.laa.springboot.metrics.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.laa.springboot.metrics.MetricsProperties;
import uk.gov.laa.springboot.metrics.aspect.CounterAspect;
import uk.gov.laa.springboot.metrics.aspect.MetricAnnotationScanner;
import uk.gov.laa.springboot.metrics.aspect.TimerAspect;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryMetric;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

@TestConfiguration
public class MetricAnnotationConfiguration {


  @Bean
  public CounterAspect counterAspect(CounterMetricService counterMetricService) {
    MetricsProperties properties = new MetricsProperties();
    properties.setMetricNamePrefix("test_metric");
    return new CounterAspect(counterMetricService, properties);
  }

  @Bean
  public TimerAspect timerAspect(HistogramMetricService histogramMetricService,
      SummaryMetricService summaryMetricService) {
    MetricsProperties properties = new MetricsProperties();
    properties.setMetricNamePrefix("test_metric");
    return new TimerAspect(summaryMetricService, histogramMetricService, properties);
  }

  @Bean
  public MetricAnnotationScanner metricAnnotationScanner(
      SummaryMetricService summaryMetricService,
      HistogramMetricService histogramMetricService,
      CounterMetricService counterMetricService) {
    MetricsProperties properties = new MetricsProperties();
    properties.setMetricNamePrefix("test_metric");
    return new MetricAnnotationScanner(
        summaryMetricService, histogramMetricService,
        counterMetricService,
        properties);
  }

  public static class MetricTestClass {

    @HistogramMetric(metricName = "method_histogram", hintText = "hint-text", labels = {
        "key=value"})
    @SummaryMetric(metricName = "method_summary", hintText = "hint-text", labels = {"key=value"})
    @CounterMetric(metricName = "method_counter", hintText = "hint-text", labels = {"key=value"})
    public void someMethod() {
      // Does nothing
    }

    @HistogramMetric(metricName = "method_histogram", hintText = "hint-text")
    @SummaryMetric(metricName = "method_summary", hintText = "hint-text")
    @CounterMetric(metricName = "method_counter", hintText = "hint-text")
    public void someSecondMethod() {
      // Does nothing
    }

    @HistogramMetric(metricName = "method_histogram_two", hintText = "hint-text", labels = {
        "key=value"})
    @SummaryMetric(metricName = "method_summary_two", hintText = "hint-text", labels = {
        "key=value"})
    @CounterMetric(metricName = "method_counter_two", hintText = "hint-text", labels = {
        "key=value"})
    public void someThirdMethod() {
      // Does nothing
    }
  }

  @Bean
  public MetricTestClass metricsTestClass() {
    return new MetricTestClass();
  }

  @Bean
  public SummaryMetricService summaryMetricService(PrometheusRegistry prometheusRegistry) {
    return new SummaryMetricService(prometheusRegistry);
  }

  @Bean
  public HistogramMetricService histogramMetricService(PrometheusRegistry prometheusRegistry) {
    return new HistogramMetricService(prometheusRegistry);
  }

  @Bean
  public CounterMetricService counterMetricService(PrometheusRegistry prometheusRegistry) {
    return new CounterMetricService(prometheusRegistry);
  }

}