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
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramTimerMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryTimerMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.ValueCaptureStrategy;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

@TestConfiguration
public class MetricAnnotationConfiguration {


  @Bean
  public CounterAspect counterAspect(CounterMetricService counterMetricService,
      HistogramMetricService histogramMetricService) {
    return new CounterAspect(counterMetricService, histogramMetricService);
  }

  @Bean
  public TimerAspect timerAspect(HistogramMetricService histogramMetricService,
      SummaryMetricService summaryMetricService) {
    return new TimerAspect(summaryMetricService, histogramMetricService);
  }

  @Bean
  public MetricAnnotationScanner metricAnnotationScanner(
      SummaryMetricService summaryMetricService,
      HistogramMetricService histogramMetricService,
      CounterMetricService counterMetricService) {
    return new MetricAnnotationScanner(
        summaryMetricService, histogramMetricService,
        counterMetricService);
  }

  public static class MetricTestClass {

    @HistogramTimerMetric(metricName = "method_histogram", hintText = "hint-text", labels = {
        "key=value"})
    @SummaryTimerMetric(metricName = "method_summary", hintText = "hint-text", labels = {"key=value"})
    @CounterMetric(metricName = "method_counter", hintText = "hint-text", labels = {"key=value"})
    public void someMethod() throws InterruptedException {
      // Does nothing
    }

    @HistogramTimerMetric(metricName = "method_histogram", hintText = "hint-text", labels = {
        "key=valueTwo"})
    @SummaryTimerMetric(metricName = "method_summary", hintText = "hint-text", labels = {"key=valueTwo"})
    @CounterMetric(metricName = "method_counter", hintText = "hint-text", labels = {"key=valueTwo"})
    public void someSecondMethod() {
      // Does nothing
    }

    @HistogramTimerMetric(metricName = "method_histogram_two", hintText = "hint-text", labels = {
        "key=valueThree"})
    @SummaryTimerMetric(metricName = "method_summary_two", hintText = "hint-text", labels = {
        "key=valueThree"})
    @CounterMetric(metricName = "method_counter_two", hintText = "hint-text", labels = {
        "key=valueThree"})
    @CounterMetric(metricName = "method_counter_two", hintText = "hint-text", labels = {
        "key=valueFour"})
    public void someThirdMethod() {
      // Does nothing
    }

    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=A"}, conditionalOnReturn = "Cats")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=B"}, conditionalOnReturn = "Dogs")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=C"}, conditionalOnReturn = "1")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=D"}, conditionalOnReturn = "2")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=E"}, conditionalOnReturn = "1.52")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=ONE"}, conditionalOnReturn = "VALUE_ONE")
    @CounterMetric(metricName = "conditional_counter", hintText = "hint-text", labels = {
        "type=TWO"}, conditionalOnReturn = "VALUE_TWO")
    @CounterMetric(metricName = "store_value_counter", hintText = "hint-text",
        saveReturnValue = true)
    public Object conditional(Object type) {
      return type;
    }

    @HistogramMetric(metricName = "histogram_return", hintText = "hint-text", labels = {
        "type=key"}, valueStrategy = ValueCaptureStrategy.RETURN_VALUE)
    public Object histogramReturnValue(Object type) {
      return type;
    }

    @HistogramMetric(metricName = "histogram_param", hintText = "hint-text", labels = {
        "type=key"}, valueStrategy = ValueCaptureStrategy.PARAM_0)
    @HistogramMetric(metricName = "histogram_param", hintText = "hint-text", labels = {
        "type=key"}, valueStrategy = ValueCaptureStrategy.PARAM_1)
    @HistogramMetric(metricName = "histogram_param_diff", hintText = "hint-text", labels = {
        "type=key"}, valueStrategy = ValueCaptureStrategy.PARAM_2)
    public Object histogramParamValues(Object valueOne, Object valueTwo, Object valueThree) {
      return 123;
    }
  }

  @Bean
  public MetricTestClass metricsTestClass() {
    return new MetricTestClass();
  }

  @Bean
  public SummaryMetricService summaryMetricService(PrometheusRegistry prometheusRegistry) {
    return new SummaryMetricService(prometheusRegistry, metricsProperties());
  }

  @Bean
  public HistogramMetricService histogramMetricService(PrometheusRegistry prometheusRegistry) {
    return new HistogramMetricService(prometheusRegistry, metricsProperties());
  }

  @Bean
  public CounterMetricService counterMetricService(PrometheusRegistry prometheusRegistry) {
    return new CounterMetricService(prometheusRegistry, metricsProperties());
  }

  protected MetricsProperties metricsProperties() {
    MetricsProperties metricsProperties = new MetricsProperties();
    metricsProperties.setMetricNamePrefix("test_metrics");
    return metricsProperties;
  }

}