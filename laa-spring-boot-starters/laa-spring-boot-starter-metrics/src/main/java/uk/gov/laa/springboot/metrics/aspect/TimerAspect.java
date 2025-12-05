package uk.gov.laa.springboot.metrics.aspect;

import io.prometheus.metrics.core.datapoints.Timer;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramTimerMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryTimerMetric;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

/**
 * Aspect to measure execution time of methods annotated with {@link SummaryTimerMetric}.
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

  /**
   * Measures execution time of methods annotated with {@link SummaryTimerMetric}.
   *
   * @param pjp           the proceeding join point
   * @param summaryTimerMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(summaryTimerMetric)")
  public Object measureSummaryExecutionTime(ProceedingJoinPoint pjp,
      SummaryTimerMetric summaryTimerMetric)
      throws Throwable {
    String[] labelValues =
        getLabelValues(summaryTimerMetric.labels());
    String metricName = summaryTimerMetric.metricName();
    try (Timer timer = summaryMetricService.startTimer(metricName, labelValues)) {
      try {
        return pjp.proceed();
      } finally {
        if (!Objects.isNull(timer)) {
          double duration = timer.observeDuration();

          String methodName = pjp.getSignature().toShortString();
          String label =
              metricName.isEmpty() ? methodName : metricName;

          log.warn("{} took {} seconds", label, duration);

        }
      }
    }
  }


  /**
   * Measures execution time of methods annotated with {@link HistogramTimerMetric}.
   *
   * @param pjp             the proceeding join point
   * @param histogramTimerMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(histogramTimerMetric)")
  public Object measureHistogramExecutionTime(ProceedingJoinPoint pjp,
      HistogramTimerMetric histogramTimerMetric)
      throws Throwable {
    String[] labelValues =
        getLabelValues(histogramTimerMetric.labels());
    String metricName =
        histogramTimerMetric.metricName();
    try (Timer timer = histogramMetricService.startTimer(metricName, labelValues)) {
      try {
        return pjp.proceed();
      } finally {
        if (!Objects.isNull(timer)) {

          double duration = timer.observeDuration();

          String methodName = pjp.getSignature().toShortString();
          String label =
              metricName.isEmpty() ? methodName : metricName;

          log.warn("{} took {} seconds", label, duration);
        }
      }
    }
  }

  private static String[] getLabelValues(String[] metricLabels) {
    return Arrays.stream(metricLabels).map(x -> x.split("=")[1]).toList()
        .toArray(String[]::new);
  }

}
