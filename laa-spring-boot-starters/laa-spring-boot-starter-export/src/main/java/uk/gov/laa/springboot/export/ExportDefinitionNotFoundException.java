package uk.gov.laa.springboot.export;

/**
 * Thrown when an export key is not registered.
 */
public class ExportDefinitionNotFoundException extends RuntimeException {
  public ExportDefinitionNotFoundException(String message) {
    super(message);
  }
}
