package uk.gov.laa.springboot.sqlscanner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type, field, or record component for SQL-like pattern scanning.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanForSql {

  /**
   * Classes to be completely ignored during scanning under this annotated scope.
   */
  Class<?>[] ignoreClasses() default {};
}
