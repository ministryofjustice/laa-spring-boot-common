package uk.gov.laa.springboot.metrics.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple {@link CounterMetric} annotations.
 *
 * @author Jamie Briggs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CounterMetrics {

  /**
   * Returns an array of {@link CounterMetric} annotations associated with the annotated method.
   *
   * @return an array of {@link CounterMetric}
   */
  CounterMetric[] value();
}