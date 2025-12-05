package uk.gov.laa.springboot.metrics.aspect;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.math.BigDecimal;
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
        counterMetricService.getMetric("method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("method_counter_two").labelValues("valueThree")
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
        counterMetricService.getMetric("method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("method_counter_two").labelValues("valueThree")
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
        counterMetricService.getMetric("method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("method_counter_two").labelValues("valueThree")
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
        counterMetricService.getMetric("method_counter").labelValues("value").get();
    double resultTwo =
        counterMetricService.getMetric("method_counter").labelValues("valueTwo").get();
    double resultThree =
        counterMetricService.getMetric("method_counter_two").labelValues("valueThree")
            .get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isZero();
    assertThat(resultThree).isEqualTo(1);
  }

  @Test
  @DisplayName("Should count multiple metrics when multiple matching annotations")
  void shouldCountMultipleMetricsWhenMultipleMatchingAnnotations() {
    // When
    metricTestClass.someThirdMethod();
    // Then
    double resultOne =
        counterMetricService.getMetric("method_counter_two").labelValues("valueThree")
            .get();
    double resultTwo =
        counterMetricService.getMetric("method_counter_two").labelValues("valueFour")
            .get();
    assertThat(resultOne).isEqualTo(1);
    assertThat(resultTwo).isEqualTo(1);
  }

  @Test
  @DisplayName("Should count conditional String return A")
  void shouldCountConditionalMethodStringA() {
    // When
    metricTestClass.conditional("Cats");
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("A").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("B").get();
    assertThat(resultOne).isEqualTo(1);
    assertThat(resultTwo).isZero();
  }

  @Test
  @DisplayName("Should count conditional String return B")
  void shouldCountConditionalMethodStringB() {
    // When
    metricTestClass.conditional("Dogs");
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("A").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("B").get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isEqualTo(1);
  }

  @Test
  @DisplayName("Should count conditional Int return 1")
  void shouldCountConditionalIntMethodA() {
    // When
    metricTestClass.conditional(1);
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("C").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("D").get();
    assertThat(resultOne).isEqualTo(1);
    assertThat(resultTwo).isZero();
  }

  @Test
  @DisplayName("Should count conditional Int return 2")
  void shouldCountConditionalIntMethodB() {
    // When
    metricTestClass.conditional(2);
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("C").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("D").get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isEqualTo(1);
  }


  @Test
  @DisplayName("Should count conditional BigDecimal return 1.52")
  void shouldCountConditionalBigDecimalReturn() {
    // When
    metricTestClass.conditional(BigDecimal.valueOf(1.52));
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("E").get();
    assertThat(resultOne).isEqualTo(1);
  }

  public enum TestEnum{
    VALUE_ONE,
    VALUE_TWO
  }

  @Test
  @DisplayName("Should count conditional enum return VALUE_ONE")
  void shouldCountConditionalEnumValueOne() {
    // When
    metricTestClass.conditional(TestEnum.VALUE_ONE);
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("ONE").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("TWO").get();
    assertThat(resultOne).isEqualTo(1);
    assertThat(resultTwo).isZero();
  }

  @Test
  @DisplayName("Should count conditional enum return VALUE_TWO")
  void shouldCountConditionalEnumValueTwo() {
    // When
    metricTestClass.conditional(TestEnum.VALUE_TWO);
    // Then
    double resultOne =
        counterMetricService.getMetric("conditional_counter").labelValues("ONE").get();
    double resultTwo =
        counterMetricService.getMetric("conditional_counter").labelValues("TWO").get();
    assertThat(resultOne).isZero();
    assertThat(resultTwo).isEqualTo(1);
  }

  @Test
  @DisplayName("Should save return value")
  void shouldSaveReturnValue() {
    // When
    metricTestClass.conditional(TestEnum.VALUE_TWO);
    // Then
    double resultOne =
        counterMetricService.getMetric("store_value_counter").labelValues("VALUE_TWO").get();
    assertThat(resultOne).isEqualTo(1);
  }
}