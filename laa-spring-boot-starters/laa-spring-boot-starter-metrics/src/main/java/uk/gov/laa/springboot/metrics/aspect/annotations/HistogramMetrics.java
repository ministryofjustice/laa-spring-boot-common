package uk.gov.laa.springboot.metrics.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple {@link HistogramMetric} annotations.
 *
 * @author Jamie Briggs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HistogramMetrics {

  /**
   * Returns an array of {@link HistogramMetric} annotations associated with the annotated method.
   *
   * @return an array of {@link HistogramMetric}
   */
  HistogramMetric[] value();
}