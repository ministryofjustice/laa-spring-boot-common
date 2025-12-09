package uk.gov.laa.springboot.sqlscanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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

  // --- Pointcuts -------------------------------------------------------------------

  @Pointcut(
      "within(@org.springframework.web.bind.annotation.RestController *)"
          + " || within(@org.springframework.stereotype.Controller *)")
  private void controllerMethods() {}

  @Pointcut(
      "( within(@org.springframework.stereotype.Repository *)"
          + " || within(org.springframework.data.repository.Repository+) )"
          + " && ( execution(* save*(..)) || execution(* update*(..)) )")
  private void repositorySaveOrUpdate() {}

  // --- Advice ----------------------------------------------------------------------

  @Before("controllerMethods()")
  public void scanForSqlController(JoinPoint jp) {
    scanArguments(jp.getArgs());
    scanParamsAnnotated(jp);
  }

  @Before("repositorySaveOrUpdate()")
  public void scanForSqlDb(JoinPoint jp) {
    scanArguments(jp.getArgs());
  }

  // --- Scan parameter-level @ScanForSql --------------------------------------------

  private void scanParamsAnnotated(JoinPoint jp) {
    Method method = ((MethodSignature) jp.getSignature()).getMethod();
    Annotation[][] annotations = method.getParameterAnnotations();
    Object[] args = jp.getArgs();

    for (int i = 0; i < annotations.length; i++) {
      for (Annotation a : annotations[i]) {
        if (a.annotationType() == ScanForSql.class) {
          log.info("@ScanForSql found on parameter index {}", i);
          scanObject(args[i], false, new IdentityHashMap<>());
        }
      }
    }
  }

  // --- Entry point -----------------------------------------------------------------

  void scanArguments(Object[] args) {
    if (args == null) {
      return;
    }

    for (Object arg : args) {
      if (arg == null) {
        continue;
      }

      boolean annotatedClass = arg.getClass().isAnnotationPresent(ScanForSql.class);
      scanObject(arg, !annotatedClass, new IdentityHashMap<>());
    }
  }

  // --- Core recursive method -------------------------------------------------------

  private void scanObject(Object obj, boolean annotatedOnly, Map<Object, Boolean> visited) {
    if (obj == null || visited.containsKey(obj)) {
      return;
    }
    visited.put(obj, Boolean.TRUE);

    // --- String ---
    if (obj instanceof String s) {
      checkValue(s, null);
      return;
    }

    Class<?> type = obj.getClass();

    // --- Simple types ---
    if (type.isPrimitive() || type.isEnum() || isWrapper(type)) {
      return;
    }

    // --- Collections ---
    if (obj instanceof Collection<?> col) {
      col.forEach(e -> scanObject(e, annotatedOnly, visited));
      return;
    }

    // --- Maps ---
    if (obj instanceof Map<?, ?> map) {
      map.values().forEach(v -> scanObject(v, annotatedOnly, visited));
      return;
    }

    // --- Arrays ---
    if (type.isArray()) {
      for (Object element : (Object[]) obj) {
        scanObject(element, annotatedOnly, visited);
      }
      return;
    }

    // --- Records ---
    if (type.isRecord()) {
      for (RecordComponent rc : type.getRecordComponents()) {
        if (annotatedOnly && !rc.isAnnotationPresent(ScanForSql.class)) {
          continue;
        }

        try {
          Object value = rc.getAccessor().invoke(obj);
          scanValueOrRecurse(value, rc.getName(), annotatedOnly, visited);
        } catch (Exception e) {
          log.debug("Cannot read record component {}", rc.getName());
        }
      }
      return;
    }

    // --- POJO fields ---
    for (Field f : type.getDeclaredFields()) {

      if (Modifier.isStatic(f.getModifiers())) {
        continue;
      }
      if (annotatedOnly && !f.isAnnotationPresent(ScanForSql.class)) {
        continue;
      }

      try {
        if (!f.canAccess(obj)) {
          f.setAccessible(true);
        }
        Object value = f.get(obj);
        scanValueOrRecurse(value, f.getName(), annotatedOnly, visited);

      } catch (Exception e) {
        log.debug("Cannot read field {}", f.getName());
      }
    }
  }

  // --- Scan a value or recurse -----------------------------------------------------

  private void scanValueOrRecurse(
      Object value, String name, boolean annotatedOnly, Map<Object, Boolean> visited) {

    if (value instanceof String s) {
      checkValue(s, name);
    } else {
      scanObject(value, annotatedOnly, visited);
    }
  }

  // --- SQL detection ---------------------------------------------------------------

  private void checkValue(String value, String fieldName) {
    scanner
        .scan(value)
        .ifPresent(
            pattern ->
                log.warn(
                    "Suspicious SQL-like pattern '{}' in field '{}': '{}'",
                    pattern,
                    fieldName,
                    value));
  }

  private boolean isWrapper(Class<?> t) {
    return t == Integer.class
        || t == Long.class
        || t == Double.class
        || t == Float.class
        || t == Boolean.class
        || t == Byte.class
        || t == Short.class
        || t == Character.class;
  }
}