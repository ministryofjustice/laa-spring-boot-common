package uk.gov.laa.ccms.springboot.dialect;

/**
 * DatePickerAttributes.
 */
public record DatePickerAttributes(
    String id,
    String name,
    String label,
    String hint,
    String errorMessage,
    String value,
    String minDate,
    String maxDate) {

  public boolean hasError() {
    return errorMessage != null && !errorMessage.isBlank();
  }
}
