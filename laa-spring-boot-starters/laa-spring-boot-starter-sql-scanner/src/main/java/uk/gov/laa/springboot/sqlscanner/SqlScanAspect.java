package uk.gov.laa.springboot.sqlscanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect that inspects controller arguments for SQL-like patterns
 * on annotated types and members.
 */
@Aspect
public class SqlScanAspect {

  private static final Logger log = LoggerFactory.getLogger(SqlScanAspect.class);
  private static final List<String> ALLOWED_PACKAGES =
      List.of("uk.gov.justice", "uk.gov.laa");

  private final SqlScanner scanner;

  public SqlScanAspect(SqlScanner scanner) {
    this.scanner = scanner;
  }

  @Pointcut(
      "within(@org.springframework.web.bind.annotation.RestController *)"
          + " || within(@org.springframework.stereotype.Controller *)"
  )
  private void controllerMethods() {}

  @Pointcut(
      "(within(@org.springframework.stereotype.Repository *)"
          + " || within(org.springframework.data.repository.Repository+))"
          + " && (execution(* save*(..)) || execution(* update*(..)))"
  )
  private void repositorySaveOrUpdate() {}

  @Before("controllerMethods()")
  public void scanForSqlController(JoinPoint jp) {
    scanArguments(jp.getArgs());
    scanParamsAnnotated(jp);
  }

  @Before("repositorySaveOrUpdate()")
  public void scanForSqlDb(JoinPoint jp) {
    scanArguments(jp.getArgs());
  }

  private void scanParamsAnnotated(JoinPoint jp) {
    MethodSignature sig = (MethodSignature) jp.getSignature();
    Annotation[][] paramAnnotations = sig.getMethod().getParameterAnnotations();
    Object[] args = jp.getArgs();

    for (int i = 0; i < paramAnnotations.length; i++) {
      for (Annotation a : paramAnnotations[i]) {
        if (a.annotationType() == ScanForSql.class) {
          scanObject(args[i], "arg[" + i + "]", true, new IdentityHashMap<>());
        }
      }
    }
  }

  void scanArguments(Object[] args) {
    if (args == null) {
      return;
    }

    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      if (arg == null) {
        continue;
      }

      boolean classAnnotated = arg.getClass().isAnnotationPresent(ScanForSql.class);
      scanObject(arg, "arg[" + i + "]", classAnnotated, new IdentityHashMap<>());
    }
  }

  private void scanObject(
      Object obj, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    if (obj == null || visited.containsKey(obj)) {
      return;
    }
    visited.put(obj, Boolean.TRUE);

    if (scanString(obj, fieldName, shouldScan)) {
      return;
    }

    Class<?> type = obj.getClass();

    if (isPrimitiveOrWrapper(type) || type.isEnum()) {
      return;
    }

    if (type.isArray()) {
      scanArray(obj, fieldName, shouldScan, visited);
      return;
    }

    if (obj instanceof Collection<?> col) {
      scanCollection(col, fieldName, shouldScan, visited);
      return;
    }

    if (obj instanceof Map<?, ?> map) {
      scanMap(map, fieldName, shouldScan, visited);
      return;
    }

    if (type.isRecord()) {
      scanRecord(obj, fieldName, shouldScan, visited);
      return;
    }

    scanPojoFields(obj, fieldName, shouldScan, visited);
  }

  private boolean scanString(Object obj, String fieldName, boolean shouldScan) {
    if (obj instanceof String s) {
      if (shouldScan) {
        checkValue(s, fieldName);
      }
      return true;
    }
    return false;
  }

  private void scanArray(
      Object array, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    int len = Array.getLength(array);
    for (int i = 0; i < len; i++) {
      Object element = Array.get(array, i);
      scanObject(
          element, nestedFieldName(fieldName, "[" + i + "]"), shouldScan, visited
      );
    }
  }

  private void scanCollection(
      Collection<?> col, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    int i = 0;
    for (Object e : col) {
      scanObject(
          e, nestedFieldName(fieldName, "[" + i + "]"), shouldScan, visited
      );
      i++;
    }
  }

  private void scanMap(
      Map<?, ?> map, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      scanObject(
          entry.getValue(),
          nestedFieldName(fieldName, "[" + entry.getKey() + "]"),
          shouldScan,
          visited
      );
    }
  }

  private void scanRecord(
      Object record, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    for (RecordComponent rc : record.getClass().getRecordComponents()) {
      boolean process = shouldScan || rc.isAnnotationPresent(ScanForSql.class);
      if (!process) {
        continue;
      }

      try {
        Object value = rc.getAccessor().invoke(record);
        scanObject(
            value,
            nestedFieldName(fieldName, rc.getName()),
            process,
            visited
        );
      } catch (Exception ex) {
        log.debug("Cannot read record component {}", rc.getName());
      }
    }
  }

  private void scanPojoFields(
      Object obj, String fieldName, boolean shouldScan, Map<Object, Boolean> visited
  ) {
    // Only scan classes from your application
    Package pkg = obj.getClass().getPackage();
    if (pkg == null || !isAllowedPackage(pkg.getName())) {
      return; // skip all non-domain types (JDK, libraries, etc.)
    }

    for (Field f : obj.getClass().getDeclaredFields()) {

      if (Modifier.isStatic(f.getModifiers())) {
        continue;
      }

      // Restrict by declaring class package too
      String fieldPackage = f.getDeclaringClass().getPackageName();
      if (!isAllowedPackage(fieldPackage)) {
        continue;
      }

      boolean process = shouldScan || f.isAnnotationPresent(ScanForSql.class);
      if (!process) {
        continue;
      }

      try {
        if (!f.canAccess(obj)) {
          f.setAccessible(true);
        }
        Object value = f.get(obj);
        scanObject(
            value,
            nestedFieldName(fieldName, f.getName()),
            process,
            visited
        );
      } catch (Exception ex) {
        log.debug("Cannot read field {}", f.getName());
      }
    }
  }

  private boolean isAllowedPackage(String pkg) {
    return ALLOWED_PACKAGES.stream().anyMatch(pkg::startsWith);
  }

  private void checkValue(String value, String fieldName) {
    scanner.scan(value).ifPresent(pattern -> {
      log.warn(
          "Suspicious SQL-like pattern '{}' in field '{}': '{}'",
          pattern,
          fieldName,
          value
      );
    });
  }

  private boolean isPrimitiveOrWrapper(Class<?> t) {
    return t.isPrimitive()
        || t == Integer.class
        || t == Long.class
        || t == Double.class
        || t == Float.class
        || t == Boolean.class
        || t == Byte.class
        || t == Short.class
        || t == Character.class;
  }

  /**
   * Returns a nested field name like "parent.child" or just "child" if parent is empty.
   */
  private String nestedFieldName(String parent, String child) {
    if (parent == null || parent.isEmpty()) {
      return child;
    } else {
      return parent + "." + child;
    }
  }
}
