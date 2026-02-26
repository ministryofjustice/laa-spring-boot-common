package uk.gov.laa.springboot.metrics.aspect.annotations;

import java.lang.annotation.ElementType;
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
public @interface SummaryTimerMetric {

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
   * Static labels in the form "key=value".
   */
  String[] labels() default {};
}
