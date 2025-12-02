package uk.gov.laa.springboot.metrics.aspect;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.MetricsProperties;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryMetric;
import uk.gov.laa.springboot.metrics.service.CounterMetricService;
import uk.gov.laa.springboot.metrics.service.HistogramMetricService;
import uk.gov.laa.springboot.metrics.service.SummaryMetricService;

/**
 * Scans for any metric focused annotations on beans and registers them with the metric service.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Component
public class MetricAnnotationScanner implements ApplicationListener<ContextRefreshedEvent> {

  private final SummaryMetricService summaryMetricService;
  private final HistogramMetricService histogramMetricService;
  private final CounterMetricService counterMetricService;
  private final String metricNamePrefix;

  /**
   * Constructs the MetricAnnotationScanner.
   *
   * @param summaryMetricService   the summary metric service to register metrics with
   * @param histogramMetricService the histogram metric service to register metrics with
   * @param counterMetricService   the counter metric service to register metrics with
   * @param properties             the metrics properties
   */
  public MetricAnnotationScanner(SummaryMetricService summaryMetricService,
      HistogramMetricService histogramMetricService, CounterMetricService counterMetricService,
      MetricsProperties properties) {
    this.summaryMetricService = summaryMetricService;
    this.histogramMetricService = histogramMetricService;
    this.counterMetricService = counterMetricService;
    this.metricNamePrefix = properties.getMetricNamePrefix();
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext context = event.getApplicationContext();
    String[] beanNames = context.getBeanDefinitionNames();

    for (String beanName : beanNames) {
      Class<?> beanType = context.getType(beanName);
      if (beanType == null) {
        continue;
      }

      // Handle Spring proxies (e.g. @Transactional)
      Class<?> targetClass = AopUtils.getTargetClass(context.getBean(beanName));
      if (Objects.isNull(targetClass)) {
        targetClass = beanType;
      }

      scanMethods(metricNamePrefix, targetClass, SummaryMetric.class);
      scanMethods(metricNamePrefix, targetClass, HistogramMetric.class);
      scanMethods(metricNamePrefix, targetClass, CounterMetric.class);
    }
  }

  private void scanMethods(String metricPrefixName, Class<?> targetClass,
      Class<? extends java.lang.annotation.Annotation> annotationClass) {
    Arrays.stream(targetClass.getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(annotationClass))
        .forEach(m -> {
          log.info("Found {} on method {}", annotationClass.getName(), m.getName());

          if (annotationClass.equals(SummaryMetric.class)) {
            SummaryMetric annotation = m.getAnnotation(SummaryMetric.class);
            summaryMetricService.register(
                "%s_%s".formatted(metricPrefixName, annotation.metricName()),
                annotation.hintText());
          } else if (annotationClass.equals(HistogramMetric.class)) {
            HistogramMetric annotation = m.getAnnotation(HistogramMetric.class);
            histogramMetricService.register(
                "%s_%s".formatted(metricPrefixName, annotation.metricName()),
                annotation.hintText());
          } else if (annotationClass.equals(CounterMetric.class)) {
            CounterMetric annotation = m.getAnnotation(CounterMetric.class);
            counterMetricService.register(
                "%s_%s".formatted(metricPrefixName, annotation.metricName()),
                annotation.hintText());
          }
        });
  }

}
