package uk.gov.laa.springboot.metrics.aspect.annotations;

/**
 * Defines how a value will be counted on method annotated with a metric annotation.
 *
 * @see HistogramMetric
 * @see CounterMetric
 */
public enum ValueCaptureStrategy {
  RETURN_VALUE(-1),
  PARAM_0(0),
  PARAM_1(1),
  PARAM_2(2),
  ;

  private final int paramIndex;

  ValueCaptureStrategy(int paramIndex) {
    this.paramIndex = paramIndex;
  }

  public int getParamIndex() {
    return paramIndex;
  }
}
