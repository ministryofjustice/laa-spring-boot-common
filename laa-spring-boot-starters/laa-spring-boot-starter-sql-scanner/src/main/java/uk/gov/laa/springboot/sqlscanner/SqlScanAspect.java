package uk.gov.laa.springboot.sqlscanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect that inspects controller arguments for SQL-like patterns on annotated types and members.
 */
@Aspect
public class SqlScanAspect {

  private static final Logger log = LoggerFactory.getLogger(SqlScanAspect.class);

  private final SqlScanner scanner;

  public SqlScanAspect(SqlScanner scanner) {
    this.scanner = scanner;
  }

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)"
      + " || within(@org.springframework.stereotype.Controller *)")
  private void controllerMethods() {
    // Pointcut for controller classes.
  }

  /**
   * Scans any controller arguments annotated with {@link ScanForSql} for SQL-like content.
   *
   * @param joinPoint the intercepted controller invocation.
   */
  @Before("controllerMethods()")
  public void scanForSql(JoinPoint joinPoint) {
    scanArguments(joinPoint.getArgs());
  }

  void scanArguments(Object[] arguments) {
    if (arguments == null) {
      return;
    }

    for (Object argument : arguments) {
      if (argument == null) {
        continue;
      }
      scanTarget(argument);
    }
  }

  private void scanTarget(Object argument) {
    Class<?> type = argument.getClass();
    if (type.isAnnotationPresent(ScanForSql.class)) {
      scanAllStringMembers(argument);
      return;
    }

    scanAnnotatedMembers(argument);
  }

  private void scanAnnotatedMembers(Object target) {
    if (target.getClass().isRecord()) {
      scanRecordComponents(target, true);
    } else {
      scanFields(target, true);
    }
  }

  private void scanAllStringMembers(Object target) {
    if (target.getClass().isRecord()) {
      scanRecordComponents(target, false);
    } else {
      scanFields(target, false);
    }
  }

  private void scanRecordComponents(Object target, boolean annotatedOnly) {
    RecordComponent[] components = target.getClass().getRecordComponents();
    if (components == null) {
      return;
    }

    for (RecordComponent component : components) {
      if (component.getType() != String.class) {
        continue;
      }

      if (annotatedOnly && !component.isAnnotationPresent(ScanForSql.class)) {
        continue;
      }

      try {
        String value = (String) component.getAccessor().invoke(target);
        checkValue(value, component.getName());
      } catch (ReflectiveOperationException ex) {
        log.debug("Unable to read record component '{}' for SQL scan", component.getName(), ex);
      }
    }
  }

  private void scanFields(Object target, boolean annotatedOnly) {
    for (Field field : target.getClass().getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      if (field.getType() != String.class) {
        continue;
      }

      if (annotatedOnly && !field.isAnnotationPresent(ScanForSql.class)) {
        continue;
      }

      try {
        if (!field.canAccess(target) && !field.trySetAccessible()) {
          log.debug("Skipping field '{}' for SQL scan because it is not accessible",
              field.getName());
          continue;
        }
      } catch (RuntimeException ex) {
        log.debug("Skipping field '{}' for SQL scan due to inaccessible module boundaries",
            field.getName(), ex);
        continue;
      }

      try {
        String value = (String) field.get(target);
        checkValue(value, field.getName());
      } catch (IllegalAccessException ex) {
        log.debug("Unable to read field '{}' for SQL scan", field.getName(), ex);
      }
    }
  }

  private void checkValue(String value, String fieldName) {
    scanner.scan(value).ifPresent(pattern ->
        log.warn("Suspicious SQL-like pattern '{}' detected in field '{}': value='{}'",
            pattern, fieldName, value)
    );
  }
}
