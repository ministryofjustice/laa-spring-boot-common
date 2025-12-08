package uk.gov.laa.springboot.metrics.aspect;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class HistogramMetricTests {

  @MockitoBean
  HistogramMetricService histogramMetricService;

  @Autowired
  MetricTestClass metricTestClass;

  @MockitoBean
  public PrometheusRegistry prometheusRegistry;


  @Nested
  @DisplayName("Return value tests")
  class ReturnValueTests {


    @Test
    @DisplayName("Should store integer")
    void shouldStoreInteger() {
      // Given
      // When
      metricTestClass.histogramReturnValue(1);

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_return", 1.0d, "key");
    }

    @Test
    @DisplayName("Should store float")
    void shouldStoreFloat() {
      // Given
      // When
      metricTestClass.histogramReturnValue(1.52f);

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_return", 1.52d, "key");
    }


    @Test
    @DisplayName("Should store BigDecimal")
    void shouldStoreBigDecimal() {
      // Given
      // When
      metricTestClass.histogramReturnValue(BigDecimal.valueOf(2.453));

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_return", 2.453d, "key");
    }

  }

  @Nested
  @DisplayName("Parameter value tests")
  class ParamTests{
    @Test
    @DisplayName("Should store integer")
    void shouldStoreInteger() {
      // Given
      // When
      metricTestClass.histogramParamValues(1, 2 , 3);

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 1.0d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 2.0d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param_diff", 3.0d, "key");
    }

    @Test
    @DisplayName("Should store float")
    void shouldStoreFloat() {
      // Given
      // When
      metricTestClass.histogramParamValues(1.52f, 3.21f, 5.34f);

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 1.52d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 3.21d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param_diff", 5.34d, "key");
    }


    @Test
    @DisplayName("Should store BigDecimal")
    void shouldStoreBigDecimal() {
      // Given
      // When
      metricTestClass.histogramParamValues(BigDecimal.valueOf(2.453),
          BigDecimal.valueOf(2.456), BigDecimal.valueOf(5.345));

      // Then
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 2.453d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param", 2.456d, "key");
      verify(histogramMetricService, times(1))
          .recordValue("histogram_param_diff", 5.345d, "key");
    }
  }

}