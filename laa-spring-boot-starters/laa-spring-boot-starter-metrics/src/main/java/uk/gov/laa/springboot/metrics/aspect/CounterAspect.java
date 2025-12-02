package uk.gov.laa.springboot.metrics.aspect;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryMetric;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;

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

  /**
   * Measures execution time of methods annotated with {@link CounterMetric}.
   *
   * @param pjp          the proceeding join point
   * @param counter the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(counter)")
  public Object measureSummaryExecutionTime(ProceedingJoinPoint pjp, CounterMetric counter)
      throws Throwable {

    boolean success = false;
    try {
      Object result = pjp.proceed();
      success = true;
      return result;
    } finally {
      if (!counter.recordSuccessOnly() || success) {
        String methodName = pjp.getSignature().toShortString();
        String metricName = counter.metricName().isEmpty()
            ? methodName
            : counter.metricName();

        String[] labelValues =
            Arrays.stream(counter.labels()).map(x -> x.split("=")[1]).toList()
                .toArray(String[]::new);
        counterMetricService.increment(metricName, counter.amount(), labelValues);
        log.debug("Incremented counter {} by {}", metricName, counter.amount());
      }
    }
  }

}
