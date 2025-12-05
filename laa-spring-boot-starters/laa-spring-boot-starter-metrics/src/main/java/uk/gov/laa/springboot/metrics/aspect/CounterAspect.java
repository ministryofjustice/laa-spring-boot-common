package uk.gov.laa.springboot.metrics.aspect;

import io.micrometer.common.util.StringUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetrics;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetrics;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;

/**
 * Aspect to track counter metrics.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CounterAspect {

  private final CounterMetricService counterMetricService;
  private final HistogramMetricService histogramMetricService;

  /**
   * Counts up a metric after method has returned.
   *
   * @param counter the counter annotation
   * @param result  the method return value
   */
  @AfterReturning(value = "@annotation(counter)", returning = "result")
  public void countUp(CounterMetric counter, Object result) {
    if (StringUtils.isNotBlank(counter.metricName())
        && (StringUtils.isBlank(counter.conditionalOnReturn())
        || Objects.equals(counter.conditionalOnReturn(), result.toString()))) {
      String metricName = counter.metricName();
      List<String> labelsList = new java.util.ArrayList<>(
          Arrays.stream(counter.labels()).map(x -> x.split("=")[1]).toList());
      if (counter.saveReturnValue()) {
        labelsList.add(String.valueOf(result.toString()));
      }
      counterMetricService.increment(
          metricName, counter.amount(), labelsList
              .toArray(String[]::new));
      log.debug("Incremented counter {} by {}", metricName, counter.amount());
    }
  }

  /**
   * Counts up a metric after method has returned. Handles methods with multiple counters
   * annotations.
   *
   * @param counters the counters collection annotation
   * @param result   the method return value
   */
  @AfterReturning(value = "@annotation(counters)", returning = "result")
  public void countUpMultiple(CounterMetrics counters, Object result) {
    for (CounterMetric metric : counters.value()) {
      countUp(metric, result);
    }
  }


  /**
   * Counts up a metric after method has returned.
   *
   * @param histogramMetric the histogramMetric annotation
   * @param result          the method return value
   */
  @AfterReturning(value = "@annotation(histogramMetric)", returning = "result")
  public void countUpHistogram(HistogramMetric histogramMetric, Object result) {
    if (result == null) {
      return;
    }
    String metricName = histogramMetric.metricName();
    List<String> labelsList = new java.util.ArrayList<>(
        Arrays.stream(histogramMetric.labels()).map(x -> x.split("=")[1]).toList());

    BigDecimal value = new BigDecimal(result.toString());
    histogramMetricService.recordValue(
        metricName, value.doubleValue(), labelsList.toArray(String[]::new));
    log.debug("Incremented histogramMetric {} by {}", metricName, value.doubleValue());
  }

  /**
   * Counts up a metric after method has returned. Handles methods with multiple histograms
   * annotations.
   *
   * @param histograms the histograms collection annotation
   * @param result   the method return value
   */
  @AfterReturning(value = "@annotation(histograms)", returning = "result")
  public void countUpMultipleHistograms(HistogramMetrics histograms, Object result) {
    for (HistogramMetric metric : histograms.value()) {
      countUpHistogram(metric, result);
    }
  }

}
