package uk.gov.laa.springboot.metrics.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to measure and log the execution time of a method.
 *
 * @author Jamie Briggs
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(CounterMetrics.class)
public @interface CounterMetric {

  /**
   * The name of the metric you wish to record. Will be appended to the base metric name.
   *
   * @return the metric name
   */
  String metricName();

  /**
   * Hint text to describe what the metric is recording.
   *
   * @return the metric hint text
   */
  String hintText() default "";

  /**
   * Increment value.
   */
  double amount() default 1.0;

  /**
   * Static labels in the form "key=value".
   */
  String[] labels() default {};

  /**
   * Should only count if the string value of the return type matches.
   *
   * @return the string return value
   */
  String conditionalOnReturn() default "";

}

