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
          scanObject(args[i], "arg[" + i + "]", true, new IdentityHashMap<>());
        }
      }
    }
  }

  // --- Entry point -----------------------------------------------------------------
  void scanArguments(Object[] args) {
    if (args == null) {
      return;
    }

    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      if (arg == null) {
        continue;
      }

      boolean shouldScan = arg.getClass().isAnnotationPresent(ScanForSql.class);
      scanObject(arg, "arg[" + i + "]", shouldScan, new IdentityHashMap<>());
    }
  }

  // --- Core recursive method -------------------------------------------------------
  private void scanObject(Object obj, String fieldName, boolean shouldScan,
                          Map<Object, Boolean> visited) {
    if (obj == null || visited.containsKey(obj)) {
      return;
    }
    visited.put(obj, Boolean.TRUE);

    // --- String ---
    if (obj instanceof String s) {
      if (shouldScan) {
        checkValue(s, fieldName);
      }
      return;
    }

    Class<?> type = obj.getClass();

    // --- Simple types ---
    if (type.isPrimitive() || type.isEnum() || isWrapper(type)) {
      return;
    }

    // --- Collections ---
    if (obj instanceof Collection<?> col) {
      int idx = 0;
      for (Object e : col) {
        scanObject(e, fieldName + "[" + idx++ + "]", shouldScan, visited);
      }
      return;
    }

    // --- Maps ---
    if (obj instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        scanObject(entry.getValue(), fieldName + "[" + entry.getKey() + "]",
            shouldScan, visited);
      }
      return;
    }

    // --- Arrays ---
    if (type.isArray()) {
      int idx = 0;
      for (Object e : (Object[]) obj) {
        scanObject(e, fieldName + "[" + idx++ + "]", shouldScan, visited);
      }
      return;
    }

    // --- Records ---
    if (type.isRecord()) {
      for (RecordComponent rc : type.getRecordComponents()) {
        if (!shouldScan && !rc.isAnnotationPresent(ScanForSql.class)) {
          continue;
        }

        try {
          Object value = rc.getAccessor().invoke(obj);
          scanValueOrRecurse(value, fieldName + "." + rc.getName(), visited);
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
      if (!shouldScan && !f.isAnnotationPresent(ScanForSql.class)) {
        continue;
      }

      try {
        if (!f.canAccess(obj)) {
          f.setAccessible(true);
        }
        Object value = f.get(obj);
        scanValueOrRecurse(value, fieldName + "." + f.getName(), visited);

      } catch (Exception e) {
        log.debug("Cannot read field {}", f.getName());
      }
    }
  }

  // --- Scan a value or recurse -----------------------------------------------------

  private void scanValueOrRecurse(
      Object value, String fieldName, Map<Object, Boolean> visited) {

    if (value instanceof String s) {
      checkValue(s, fieldName);
    } else {
      scanObject(value, fieldName, true, visited);
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