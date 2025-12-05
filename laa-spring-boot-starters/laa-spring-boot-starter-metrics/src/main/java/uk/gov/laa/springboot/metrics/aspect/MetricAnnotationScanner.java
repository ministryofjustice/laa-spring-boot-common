package uk.gov.laa.springboot.metrics.aspect;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.CounterMetrics;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetrics;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramTimerMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.SummaryTimerMetric;
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

  /**
   * Constructs the MetricAnnotationScanner.
   *
   * @param summaryMetricService   the summary metric service to register metrics with
   * @param histogramMetricService the histogram metric service to register metrics with
   * @param counterMetricService   the counter metric service to register metrics with
   */
  public MetricAnnotationScanner(SummaryMetricService summaryMetricService,
      HistogramMetricService histogramMetricService, CounterMetricService counterMetricService) {
    this.summaryMetricService = summaryMetricService;
    this.histogramMetricService = histogramMetricService;
    this.counterMetricService = counterMetricService;
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

      scanMethods(targetClass, SummaryTimerMetric.class);
      scanMethods(targetClass, HistogramTimerMetric.class);
      scanMethods(targetClass, CounterMetric.class);
      scanMethods(targetClass, CounterMetrics.class);
    }
  }

  private void scanMethods(Class<?> targetClass,
      Class<? extends java.lang.annotation.Annotation> annotationClass) {
    Arrays.stream(targetClass.getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(annotationClass))
        .forEach(m -> {
          log.info("Found {} on method {}", annotationClass.getName(), m.getName());

          if (annotationClass.equals(SummaryTimerMetric.class)) {
            SummaryTimerMetric annotation = m.getAnnotation(SummaryTimerMetric.class);
            summaryMetricService.register(
                annotation.metricName(),
                annotation.hintText(), annotation.labels());
          } else if (annotationClass.equals(HistogramTimerMetric.class)) {
            HistogramTimerMetric annotation = m.getAnnotation(HistogramTimerMetric.class);
            histogramMetricService.register(
                annotation.metricName(),
                annotation.hintText(), annotation.labels());
          } else if (annotationClass.equals(CounterMetric.class)) {
            CounterMetric annotation = m.getAnnotation(CounterMetric.class);
            counterMetricService.register(
                annotation.metricName(),
                annotation.hintText(), annotation.labels());
          } else if (annotationClass.equals(CounterMetrics.class)) {
            CounterMetrics metrics = m.getAnnotation(CounterMetrics.class);
            for (CounterMetric metric : metrics.value()) {
              String[] labels = metric.labels();
              if (metric.saveReturnValue()) {
                String[] newLabels = new String[labels.length + 1];
                System.arraycopy(labels, 0, newLabels, 0, labels.length);
                newLabels[labels.length] = "value=?";
                labels = newLabels;
              }
              counterMetricService.register(
                  metric.metricName(), metric.hintText(), labels);
            }
          } else if (annotationClass.equals(HistogramMetric.class)) {
            CounterMetric annotation = m.getAnnotation(CounterMetric.class);
            counterMetricService.register(
                annotation.metricName(),
                annotation.hintText(), annotation.labels());
          } else if (annotationClass.equals(HistogramMetrics.class)) {
            HistogramMetrics metrics = m.getAnnotation(HistogramMetrics.class);
            for (HistogramMetric metric : metrics.value()) {
              String[] labels = metric.labels();
              counterMetricService.register(
                  metric.metricName(), metric.hintText(), labels);
            }
          }
        });
  }


}
