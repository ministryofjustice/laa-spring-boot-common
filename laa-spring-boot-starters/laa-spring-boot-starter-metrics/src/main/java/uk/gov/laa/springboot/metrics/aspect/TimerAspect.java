package uk.gov.laa.springboot.metrics.aspect;

import io.prometheus.metrics.core.datapoints.Timer;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.MetricsProperties;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryMetric;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

/**
 * Aspect to measure execution time of methods annotated with {@link SummaryMetric}.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TimerAspect {

  private final SummaryMetricService summaryMetricService;
  private final HistogramMetricService histogramMetricService;
  private final MetricsProperties metricsProperties;

  /**
   * Measures execution time of methods annotated with {@link SummaryMetric}.
   *
   * @param pjp           the proceeding join point
   * @param summaryMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(summaryMetric)")
  public Object measureSummaryExecutionTime(ProceedingJoinPoint pjp, SummaryMetric summaryMetric)
      throws Throwable {
    String[] labelValues =
        getLabelValues(summaryMetric.labels());
    String metricName = "%s_%s".formatted(metricsProperties.getMetricNamePrefix(), summaryMetric.metricName());
    try (Timer timer = summaryMetricService.startTimer(metricName, labelValues)) {
      try {
        return pjp.proceed();
      } finally {

        double duration = timer.observeDuration();

        String methodName = pjp.getSignature().toShortString();
        String label =
            metricName.isEmpty() ? methodName : metricName;

        log.warn("{} took {} seconds", label, duration);
      }
    }
  }


  /**
   * Measures execution time of methods annotated with {@link HistogramMetric}.
   *
   * @param pjp             the proceeding join point
   * @param histogramMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(histogramMetric)")
  public Object measureHistogramExecutionTime(ProceedingJoinPoint pjp,
      HistogramMetric histogramMetric)
      throws Throwable {
    String[] labelValues =
        getLabelValues(histogramMetric.labels());
    String metricName = "%s_%s".formatted(metricsProperties.getMetricNamePrefix(), histogramMetric.metricName());
    try (Timer timer = histogramMetricService.startTimer(metricName, labelValues)) {
      try {
        return pjp.proceed();
      } finally {

        double duration = timer.observeDuration();

        String methodName = pjp.getSignature().toShortString();
        String label =
            metricName.isEmpty() ? methodName : metricName;

        log.warn("{} took {} seconds", label, duration);
      }
    }
  }

  private static String[] getLabelValues(String[] metricLabels) {
    return Arrays.stream(metricLabels).map(x -> x.split("=")[1]).toList()
        .toArray(String[]::new);
  }

}
