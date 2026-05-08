package uk.gov.laa.springboot.metrics.exception;

/**
 * Exception thrown when there was an error found specifically within the LAA Metrics Starter.
 *
 * @author Jamie Briggs
 */
public class MetricsException extends RuntimeException {

  public MetricsException(String message) {
    super(message);
  }
}
