package uk.gov.laa.springboot.metrics.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

/**
 * Aspect to measure execution time of methods annotated with {@link SummaryTimer}.
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
   * Measures execution time of methods annotated with {@link SummaryTimer}.
   *
   * @param pjp          the proceeding join point
   * @param summaryTimer the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(summaryTimer)")
  public Object measureSummaryExecutionTime(ProceedingJoinPoint pjp, SummaryTimer summaryTimer)
      throws Throwable {
    var timer = summaryMetricService.startTimer(summaryTimer.metricName());
    try {
      return pjp.proceed();
    } finally {

      double duration = timer.observeDuration();

      String methodName = pjp.getSignature().toShortString();
      String label =
          summaryTimer.metricName().isEmpty() ? methodName : summaryTimer.metricName();

      log.warn("{} took {} seconds", label, duration);
    }
  }

  /**
   * Measures execution time of methods annotated with {@link HistogramTimer}.
   *
   * @param pjp            the proceeding join point
   * @param histogramTimer the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(histogramTimer)")
  public Object measureHistogramExecutionTime(ProceedingJoinPoint pjp,
      HistogramTimer histogramTimer)
      throws Throwable {
    var timer = histogramMetricService.startTimer(histogramTimer.metricName());
    try {
      return pjp.proceed();
    } finally {

      double duration = timer.observeDuration();

      String methodName = pjp.getSignature().toShortString();
      String label =
          histogramTimer.metricName().isEmpty() ? methodName : histogramTimer.metricName();

      log.warn("{} took {} seconds", label, duration);
    }
  }
}
