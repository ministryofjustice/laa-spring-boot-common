package uk.gov.laa.springboot.metrics.service;

import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.metrics.aspect.HistogramTimer;
import uk.gov.laa.springboot.metrics.aspect.SummaryTimer;

/**
 * Scans for any metric focused annotations on beans and registers them with the metric service.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricAnnotationScanner implements ApplicationListener<ContextRefreshedEvent> {

  private final SummaryMetricService summaryMetricService;
  private final HistogramMetricService histogramMetricService;

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

      scanMethods(beanName, targetClass, SummaryTimer.class);
      scanMethods(beanName, targetClass, HistogramTimer.class);

    }
  }

  private void scanMethods(String beanName, Class<?> targetClass,
      Class<? extends java.lang.annotation.Annotation> annotationClass) {
    Arrays.stream(targetClass.getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(annotationClass))
        .forEach(m -> {
          log.info("Found {} on method {}", annotationClass.getName(), m.getName());
          if (annotationClass.equals(SummaryTimer.class)) {
            SummaryTimer annotation = m.getAnnotation(SummaryTimer.class);
            summaryMetricService.register(annotation.metricName(), "Timer for " + m.getName());
          } else if (annotationClass.equals(HistogramTimer.class)) {
            HistogramTimer annotation = m.getAnnotation(HistogramTimer.class);
            histogramMetricService.register(annotation.metricName(), "Timer for " + m.getName());
          }
        });
  }

}
