package uk.gov.laa.springboot.metrics.aspect;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.math.BigDecimal;
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
class HistogramMetricTests {

  @MockitoBean
  HistogramMetricService histogramMetricService;

  @Autowired
  MetricTestClass metricTestClass;

  @MockitoBean
  public PrometheusRegistry prometheusRegistry;


  @Test
  @DisplayName("Should store integer")
  void shouldStoreInteger() {
    // Given
    // When
    metricTestClass.histogramReturn(1);

    // Then
    verify(histogramMetricService, times(1))
        .recordValue("histogram_return",1.0d, "key");
  }

  @Test
  @DisplayName("Should store float")
  void shouldStoreFloat() {
    // Given
    // When
    metricTestClass.histogramReturn(1.52f);

    // Then
    verify(histogramMetricService, times(1))
        .recordValue("histogram_return",1.52d, "key");
  }


  @Test
  @DisplayName("Should store BigDecimal")
  void shouldStoreBigDecimal() {
    // Given
    // When
    metricTestClass.histogramReturn(BigDecimal.valueOf(2.453));

    // Then
    verify(histogramMetricService, times(1))
        .recordValue("histogram_return",2.453d, "key");
  }


}