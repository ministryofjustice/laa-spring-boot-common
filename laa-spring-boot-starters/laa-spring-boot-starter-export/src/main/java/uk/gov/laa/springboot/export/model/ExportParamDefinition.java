package uk.gov.laa.springboot.export.model;

import java.util.Collections;
import java.util.List;

/**
 * Parameter definition metadata.
 */
public final class ExportParamDefinition {
  private final String name;
  private final String requestName;
  private final String type;
  private final String enumClass;
  private final List<String> allowed;
  private final boolean required;
  private final String defaultValue;

  /**
   * Creates a parameter definition.
   */
  public ExportParamDefinition(
      String name,
      String requestName,
      String type,
      String enumClass,
      List<String> allowed,
      boolean required,
      String defaultValue) {
    this.name = name;
    this.requestName = requestName;
    this.type = type;
    this.enumClass = enumClass;
    this.allowed = allowed == null ? List.of() : List.copyOf(allowed);
    this.required = required;
    this.defaultValue = defaultValue;
  }

  /**
   * Creates a parameter definition without an explicit request name override.
   */
  public ExportParamDefinition(
      String name,
      String type,
      String enumClass,
      List<String> allowed,
      boolean required,
      String defaultValue) {
    this(name, null, type, enumClass, allowed, required, defaultValue);
  }

  public String getName() {
    return name;
  }

  public String getRequestName() {
    return requestName;
  }

  public String getType() {
    return type;
  }

  public String getEnumClass() {
    return enumClass;
  }

  public List<String> getAllowed() {
    return Collections.unmodifiableList(allowed);
  }

  public boolean isRequired() {
    return required;
  }

  public String getDefaultValue() {
    return defaultValue;
  }
}
