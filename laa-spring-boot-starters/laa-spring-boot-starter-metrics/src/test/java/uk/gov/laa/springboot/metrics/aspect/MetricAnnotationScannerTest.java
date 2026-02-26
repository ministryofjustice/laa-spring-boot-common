package uk.gov.laa.springboot.metrics.aspect;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.laa.springboot.metrics.config.MetricAnnotationConfiguration;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MetricAnnotationConfiguration.class)
@ComponentScan("uk.gov.laa.springboot.metrics.aspect")
class MetricAnnotationScannerTest {

  @Autowired
  CounterMetricService counterMetricService;

  @MockitoBean
  public PrometheusRegistry prometheusRegistry;

  @Autowired
  private SummaryMetricService summaryMetricService;
  @Autowired
  private HistogramMetricService histogramMetricService;

  @Test
  @DisplayName("Should register counter metric")
  void shouldRegisterCounterMetric() {
    // check counter exists
    Counter counter = counterMetricService.getMetric("method_counter");
    Counter counterTwo = counterMetricService.getMetric("method_counter_two");
    assertThat(counter).isNotNull();
    assertThat(counterTwo).isNotNull();
  }

  @Test
  @DisplayName("Should register summary metric")
  void shouldRegisterSummaryMetric() {
    // check summary exists
    Summary summary = summaryMetricService.getMetric("method_summary");
    Summary summaryTwo = summaryMetricService.getMetric("method_summary_two");
    assertThat(summary).isNotNull();
    assertThat(summaryTwo).isNotNull();
  }

  @Test
  @DisplayName("Should register histogram metric")
  void shouldRegisterHistogramMetric() {
    // check summary exists
    Histogram histogram = histogramMetricService.getMetric("method_histogram");
    Histogram histogramTwo = histogramMetricService.getMetric("method_histogram_two");
    assertThat(histogram).isNotNull();
    assertThat(histogramTwo).isNotNull();
  }
}