package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

class SqlScanAspectTest {

  private SqlScanAspect aspect;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setup() {
    aspect = new SqlScanAspect(new SqlScanner());

    appender = new ListAppender<>();
    appender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(SqlScanAspect.class);
    logger.addAppender(appender);
  }

  @AfterEach
  void cleanup() {
    Logger logger = (Logger) LoggerFactory.getLogger(SqlScanAspect.class);
    logger.detachAppender(appender);
  }

  // -----------------------------
  // Basic record and POJO tests
  // -----------------------------
  @Test
  void scansEveryComponentOfAnnotatedRecord() {
    var request = new CreateCustomerRequest("Sam", "sam@example.com", "drop table users");
    aspect.scanArguments(new Object[]{request});
    assertLogContains("drop", "comments");
  }

  @Test
  void scansOnlyAnnotatedRecordComponentsWhenTypeNotAnnotated() {
    var request = new FeedbackRequest("Sam", "hello; delete from accounts");
    aspect.scanArguments(new Object[]{request});
    assertLogContains("delete", "message");
  }

  @Test
  void ignoresUnannotatedTypes() {
    aspect.scanArguments(new Object[]{new UnannotatedRequest("select * from dual")});
    assertThat(appender.list).isEmpty();
  }

  @Test
  void ignoresUnannotatedPojo() {
    aspect.scanArguments(new Object[]{new UnannotatedPojo("safe", "select * from dual")});
    assertThat(appender.list).isEmpty();
  }

  @Test
  void handlesCyclicObjectGraphsWithoutStackOverflow() {
    CyclicA a = new CyclicA();
    CyclicB b = new CyclicB();
    a.b = b;
    b.a = a;
    a.payload = "delete from t";

    aspect.scanArguments(new Object[]{a});
    assertLogContains("delete", "payload");
  }

  @Test
  void scansFieldsOnTypesAnnotatedWithScanForSql() {
    aspect.scanArguments(new Object[]{new AnnotatedPojo("drop view")});
    assertThat(appender.list).hasSize(1);
  }

  @Test
  void respectsFieldAnnotationWhenClassNotAnnotated() {
    aspect.scanArguments(new Object[]{new FieldAnnotatedPojo("ok", "drop trigger")});
    assertLogContains("drop", "danger");
  }

  @Test
  void doesNotScanPrimitiveOrWrapperTypes() {
    aspect.scanArguments(new Object[]{new PrimitiveHolder(42, true, 1.0, "drop table y")});
    assertLogContains("drop", "val");
  }

  @Test
  void scanParameterAnnotationsTriggersScan() throws Exception {
    JoinPoint jp = Mockito.mock(JoinPoint.class);
    MethodSignature sig = Mockito.mock(MethodSignature.class);
    Mockito.when(jp.getSignature()).thenReturn(sig);
    Mockito.when(sig.getMethod()).thenReturn(TestMethods.class.getDeclaredMethods()[0]);
    Mockito.when(jp.getArgs()).thenReturn(new Object[]{"delete from data"});

    aspect.scanForSqlController(jp);
    assertLogContains("delete", "arg[0]");
  }

  // -----------------------------
  // Repository advice tests
  // -----------------------------
  @Test
  void repositorySaveMethodTriggersScan() throws Exception {
    var entity = new AnnotatedPojo("drop schema test");
    JoinPoint jp = mockRepositoryJoinPoint("save", new Class[]{Object.class}, new Object[]{entity});
    aspect.scanForSqlDb(jp);
    assertLogContains("drop", "text");
  }

  @Test
  void repositoryUpdateMethodTriggersScan() throws Exception {
    var entity = new FieldAnnotatedPojo("good", "drop table x");
    JoinPoint jp = mockRepositoryJoinPoint("updateCustomer", new Class[]{Object.class}, new Object[]{entity});
    aspect.scanForSqlDb(jp);
    assertLogContains("drop", "danger");
  }

  @Test
  void nonSaveOrUpdateRepositoryMethodsAreIgnored() throws Exception {
    JoinPoint jp = mockRepositoryJoinPoint("findAll", new Class[]{}, new Object[]{});
    aspect.scanForSqlDb(jp);
    assertThat(appender.list).isEmpty();
  }

  @Test
  void saveDoesNotScanUnannotatedTypesUnlessFieldAnnotated() throws Exception {
    var obj = new UnannotatedRequest("drop everything");
    JoinPoint jp = mockRepositoryJoinPoint("saveItem", new Class[]{Object.class}, new Object[]{obj});
    aspect.scanForSqlDb(jp);
    assertThat(appender.list).isEmpty();
  }

  @Test
  void doesNotScanJdkClassesLikeUUID() {
    var uuid = java.util.UUID.randomUUID();

    aspect.scanArguments(new Object[]{uuid});

    // No scanning should occur
    assertThat(appender.list).isEmpty();
  }

  @Test
  void domainObjectWithJdkFieldsStillScansAnnotatedFieldsButSkipsJdkFields() {
    var holder = new DomainWithUuid("drop table x", java.util.UUID.randomUUID());

    aspect.scanArguments(new Object[]{holder});

    // Should log SQL pattern from annotated field
    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg -> assertThat(msg).contains("drop").contains("danger"));

    // Ensure nothing references UUID internals
    assertThat(appender.list.get(0).getFormattedMessage())
        .doesNotContain("leastSigBits")
        .doesNotContain("mostSigBits");
  }

  private JoinPoint mockRepositoryJoinPoint(String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
    Method method = FakeRepository.class.getDeclaredMethod(methodName, paramTypes);
    JoinPoint jp = Mockito.mock(JoinPoint.class);
    MethodSignature sig = Mockito.mock(MethodSignature.class);
    Mockito.when(jp.getSignature()).thenReturn(sig);
    Mockito.when(sig.getMethod()).thenReturn(method);
    Mockito.when(jp.getArgs()).thenReturn(args);
    return jp;
  }

  private void assertLogContains(String pattern, String fieldPart) {
    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg -> assertThat(msg).contains(pattern).contains(fieldPart));
  }

  // -----------------------------
  // Parameterized tests for containers
  // -----------------------------
  @ParameterizedTest
  @MethodSource("provideObjectsForScanning")
  void parameterizedScanTest(Object holder, String expectedPattern, String expectedFieldPart) {
    aspect.scanArguments(new Object[]{holder});
    assertLogContains(expectedPattern, expectedFieldPart);
  }

  private static Stream<Object[]> provideObjectsForScanning() {
    return Stream.of(
        new Object[]{new ListHolder(List.of("safe", "DROP TABLE users")), "DROP", "[1]"},
        new Object[]{new MapHolder(Map.of("k1", "safe", "k2", "DELETE FROM accounts")), "DELETE", "[k2]"},
        new Object[]{new ArrayHolder(new String[]{"ok", "DROP DATABASE"}), "DROP", "[1]"},
        new Object[]{new ListHolder(List.of("", "DROP VIEW x")), "DROP", "[1]"},
        new Object[]{new MapHolder(Map.of("key", "", "key2", "DROP SCHEMA")), "DROP", "[key2]"},
        new Object[]{new NestedRoot(new NestedLevel1(new NestedLevel2("DROP TABLE nested"))), "DROP", "level2Field"}
    );
  }

  // -----------------------------
  // Supporting classes
  // -----------------------------
  static class FakeRepository {
    public void save(Object o) {}
    public void saveItem(Object o) {}
    public void updateCustomer(Object o) {}
    public void findAll() {}
  }

  @ScanForSql
  record CreateCustomerRequest(String name, String email, String comments) {}

  record FeedbackRequest(String name, @ScanForSql String message) {}

  record UnannotatedRequest(String payload) {}

  @ScanForSql
  static class NestedRoot { NestedLevel1 level1; NestedRoot(NestedLevel1 l) { level1 = l; } }
  static class NestedLevel1 { NestedLevel2 level2; NestedLevel1(NestedLevel2 l) { level2 = l; } }
  static class NestedLevel2 { @ScanForSql String level2Field; NestedLevel2(String s) { level2Field = s; } }

  static class ListHolder { @ScanForSql List<String> list; ListHolder(List<String> list) { this.list = list; } }
  static class MapHolder { @ScanForSql Map<String,String> map; MapHolder(Map<String,String> map) { this.map = map; } }
  static class ArrayHolder { @ScanForSql String[] arr; ArrayHolder(String[] arr) { this.arr = arr; } }

  static class CyclicA { CyclicB b; @ScanForSql String payload; }
  static class CyclicB { CyclicA a; }

  @ScanForSql
  static class AnnotatedPojo { String text; AnnotatedPojo(String t) { text = t; } }

  static class FieldAnnotatedPojo { String safe; @ScanForSql String danger; FieldAnnotatedPojo(String safe, String danger) { this.safe = safe; this.danger = danger; } }

  static class UnannotatedPojo { String safe; String danger; UnannotatedPojo(String safe, String danger) { this.safe = safe; this.danger = danger; } }

  static class PrimitiveHolder { int i; boolean b; double d; @ScanForSql String val; PrimitiveHolder(int i, boolean b, double d, String val) { this.i = i; this.b = b; this.d = d; this.val = val; } }

  static class TestMethods { public void methodWithParamAnnotation(@ScanForSql String x) {} }

  static class DomainWithUuid {
    @ScanForSql
    String danger;

    java.util.UUID id;

    DomainWithUuid(String danger, java.util.UUID id) {
      this.danger = danger;
      this.id = id;
    }
  }
}
