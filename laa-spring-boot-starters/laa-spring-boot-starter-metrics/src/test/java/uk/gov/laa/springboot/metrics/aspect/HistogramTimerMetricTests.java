package uk.gov.laa.springboot.metrics.aspect;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.laa.springboot.metrics.config.MetricAnnotationConfiguration;
import uk.gov.laa.springboot.metrics.config.MetricAnnotationConfiguration.MetricTestClass;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
    MetricAnnotationConfiguration.class,
})
@EnableAspectJAutoProxy
@ComponentScan("uk.gov.laa.springboot.metrics.aspect")
class HistogramTimerMetricTests {

  @MockitoBean
  HistogramMetricService histogramMetricService;

  @Autowired
  MetricTestClass metricTestClass;

  @MockitoBean
  public PrometheusRegistry prometheusRegistry;


  @Test
  @DisplayName("Should count when method one called")
  void shouldCountWhenMethodOneCalled() throws InterruptedException {
    // Given
    Timer timer = Mockito.mock(Timer.class);
    when(histogramMetricService.startTimer("method_histogram", "value")).thenReturn(
        timer);

    // When
    metricTestClass.someMethod();

    // Then
    verify(histogramMetricService, times(1)).startTimer("method_histogram", "value");
    verify(timer, times(1)).observeDuration();
  }

  @Test
  @DisplayName("Should count when method two called")
  void shouldCountWhenMethodTwoCalled() {
    // Given
    Timer timer = Mockito.mock(Timer.class);
    when(histogramMetricService.startTimer("method_histogram", "valueTwo")).thenReturn(
        timer);

    // When
    metricTestClass.someSecondMethod();

    // Then
    verify(histogramMetricService, times(1)).startTimer("method_histogram", "valueTwo");
    verify(timer, times(1)).observeDuration();
  }

  @Test
  @DisplayName("Should count when method three called")
  void shouldCountWhenMethodThreeCalled() {
    // Given
    Timer timer = Mockito.mock(Timer.class);
    when(histogramMetricService.startTimer(
        "method_histogram_two",
        "valueThree")).thenReturn(timer);

    // When
    metricTestClass.someThirdMethod();

    // Then
    verify(histogramMetricService, times(1)).startTimer(
        "method_histogram_two", "valueThree");
    verify(timer, times(1)).observeDuration();
  }

}