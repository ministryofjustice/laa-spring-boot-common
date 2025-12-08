package uk.gov.laa.springboot.metrics.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to measure return values of a method.
 *
 * @author Jamie Briggs
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(HistogramMetrics.class)
public @interface HistogramMetric {

  /**
   * The name of the metric you wish to record. Will be appended to the base metric name.
   *
   * @return the metric name
   */
  String metricName();

  /**
   * The value strategy which should be used when recording a value from the annotated method.
   *
   * @return the value strategy
   */
  ValueCaptureStrategy valueStrategy();

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

