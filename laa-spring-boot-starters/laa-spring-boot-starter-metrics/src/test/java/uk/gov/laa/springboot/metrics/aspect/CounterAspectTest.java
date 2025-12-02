package uk.gov.laa.springboot.metrics.aspect;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
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

  @Test
  @DisplayName("Should not count when methods not called")
  void shouldNotCountWhenMethodsNotCalled(){
    // When
    double result = counterMetricService.getMetric("test_metric_method_counter").get();
    // Then
    assertThat(result).isEqualTo(0);
  }

  @Test
  @DisplayName("Should count up once when method called")
  void shouldCountUpOnceWhenMethodCalled(){
    // When
    metricTestClass.someMethod();
    double result = counterMetricService.getMetric("test_metric_method_counter").get();
    // Then
    assertThat(result).isEqualTo(1);
  }
}