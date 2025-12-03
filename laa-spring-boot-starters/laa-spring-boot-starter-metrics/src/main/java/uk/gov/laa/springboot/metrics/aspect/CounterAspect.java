package uk.gov.laa.springboot.metrics.aspect;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.MetricsProperties;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
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
  private final MetricsProperties metricsProperties;


  /**
   * Measures execution time of methods annotated with {@link CounterMetric}.
   *
   * @param pjp     the proceeding join point
   * @param counter the annotation
   * @return Object
   * @throws Throwable the throwable
   */
  @Around("@annotation(counter)")
  public Object countUp(ProceedingJoinPoint pjp, CounterMetric counter) {

    boolean success = false;
    try {
      Object result = pjp.proceed();
      success = true;
      return result;
    } catch (Throwable e) {
      log.error("Error counting up counter {}", counter.metricName(), e);
    } finally {
      if (!counter.recordSuccessOnly() || success) {
        String metricName =
            "%s_%s".formatted(metricsProperties.getMetricNamePrefix(), counter.metricName());

        String[] labelValues =
            Arrays.stream(counter.labels()).map(x -> x.split("=")[1]).toList()
                .toArray(String[]::new);
        counterMetricService.increment(metricName, counter.amount(), labelValues);
        log.debug("Incremented counter {} by {}", metricName, counter.amount());
      }
    }
    return success;

  }

}
