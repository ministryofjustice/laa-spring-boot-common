package uk.gov.laa.springboot.metrics.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect to measure execution time of methods annotated with {@link HistogramTimer}.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Aspect
@Component
public class TimedAspect {

  /**
   * Measures execution time of methods annotated with {@link HistogramTimer}.
   *
   * @param pjp            the proceeding join point
   * @param histogramTimer the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(histogramTimer)")
  public Object measureExecutionTime(ProceedingJoinPoint pjp, HistogramTimer histogramTimer)
      throws Throwable {
    long start = System.nanoTime();
    try {
      return pjp.proceed();
    } finally {
      long end = System.nanoTime();
      long durationMs = (end - start) / 1_000_000;

      String methodName = pjp.getSignature().toShortString();
      String label =
          histogramTimer.metricName().isEmpty() ? methodName : histogramTimer.metricName();

      log.info("Execution time for {}: {} ms", label, durationMs);
    }
  }
}
