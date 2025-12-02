package uk.gov.laa.springboot.metrics.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
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
public class TimedAspect {

  private final SummaryMetricService summaryMetricService;
  private final HistogramMetricService histogramMetricService;

  /**
   * Measures execution time of methods annotated with {@link SummaryMetric}.
   *
   * @param pjp          the proceeding join point
   * @param summaryMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(summaryMetric)")
  public Object measureSummaryExecutionTime(ProceedingJoinPoint pjp, SummaryMetric summaryMetric)
      throws Throwable {
    var timer = summaryMetricService.startTimer(summaryMetric.metricName());
    try {
      return pjp.proceed();
    } finally {

      double duration = timer.observeDuration();

      String methodName = pjp.getSignature().toShortString();
      String label =
          summaryMetric.metricName().isEmpty() ? methodName : summaryMetric.metricName();

      log.warn("{} took {} seconds", label, duration);
    }
  }

  /**
   * Measures execution time of methods annotated with {@link HistogramMetric}.
   *
   * @param pjp            the proceeding join point
   * @param histogramMetric the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(histogramMetric)")
  public Object measureHistogramExecutionTime(ProceedingJoinPoint pjp,
      HistogramMetric histogramMetric)
      throws Throwable {
    var timer = histogramMetricService.startTimer(histogramMetric.metricName());
    try {
      return pjp.proceed();
    } finally {

      double duration = timer.observeDuration();

      String methodName = pjp.getSignature().toShortString();
      String label =
          histogramMetric.metricName().isEmpty() ? methodName : histogramMetric.metricName();

      log.warn("{} took {} seconds", label, duration);
    }
  }
}
