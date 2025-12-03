package uk.gov.laa.springboot.metrics.aspect;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.laa.springboot.metrics.config.MetricAnnotationConfiguration;
import uk.gov.laa.springboot.metrics.config.MetricAnnotationConfiguration.MetricTestClass;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    MetricAnnotationConfiguration.class,
})
@EnableAspectJAutoProxy
@ComponentScan("uk.gov.laa.springboot.metrics.aspect")
class CounterAspectTest {

  @Autowired
  CounterMetricService counterMetricService;

  @Autowired
  MetricTestClass metricTestClass;

  @MockitoBean
  public PrometheusRegistry prometheusRegistry;

  @BeforeEach
  void beforeEach() {
    counterMetricService.resetAll();
  }

  @Test
  @DisplayName("Should not count when methods not called")
  void shouldNotCountWhenMethodsNotCalled() {
    // When
    double resultOne =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("test_metric_method_counter_two").labelValues("valueThree")
            .get();
    // Then
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isZero();
    assertThat(resultThree).isZero();
  }

  @Test
  @DisplayName("Should count up once when method one called")
  void shouldCountUpOnceWhenMethodOneCalled() throws InterruptedException {
    // When
    metricTestClass.someMethod();
    // Then
    double resultOne =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("test_metric_method_counter_two").labelValues("valueThree")
            .get();
    assertThat(resultOne).isEqualTo(1);
    assertThat(resultTwo).isZero();
    assertThat(resultThree).isZero();
  }

  @Test
  @DisplayName("Should count up once when method two called")
  void shouldCountUpOnceWhenMethodTwoCalled() {
    // When
    metricTestClass.someSecondMethod();
    // Then
    double resultOne =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("test_metric_method_counter_two").labelValues("valueThree")
            .get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isEqualTo(1);
    assertThat(resultThree).isZero();
  }

  @Test
  @DisplayName("Should count up once when method three called")
  void shouldCountUpOnceWhenMethodThreeCalled() {
    // When
    metricTestClass.someThirdMethod();
    // Then
    double resultOne =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("test_metric_method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("test_metric_method_counter_two").labelValues("valueThree")
            .get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isZero();
    assertThat(resultThree).isEqualTo(1);
  }
}