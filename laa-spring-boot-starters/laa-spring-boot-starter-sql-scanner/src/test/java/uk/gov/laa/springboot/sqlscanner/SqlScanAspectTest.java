package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.mockito.Mockito;

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

  @Test
  void scansEveryComponentOfAnnotatedRecord() {
    var request = new CreateCustomerRequest("Sam", "sam@example.com", "drop table users");

    aspect.scanArguments(new Object[]{request});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop")
                .contains("comments"));
  }

  @Test
  void scansOnlyAnnotatedRecordComponentsWhenTypeNotAnnotated() {
    var request = new FeedbackRequest("Sam", "hello; delete from accounts");

    aspect.scanArguments(new Object[]{request});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("delete")
                .contains("message"));
  }

  @Test
  void ignoresUnannotatedTypes() {
    aspect.scanArguments(new Object[]{new UnannotatedRequest("select * from dual")});
    assertThat(appender.list).isEmpty();
  }

  @Test
  void scansDeeplyNestedObjects() {
    var nested = new NestedRoot(
        new NestedLevel1(
            new NestedLevel2("DROP TABLE X")
        )
    );

    aspect.scanArguments(new Object[]{nested});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("DROP")
                .contains("level2Field"));
  }

  @Test
  void scansListsContainingStrings() {
    var obj = new ListHolder(List.of("hello", "drop user", "ok"));

    aspect.scanArguments(new Object[]{obj});

    assertThat(appender.list)
        .hasSize(1)
        .first()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop").contains("[1]"));
  }

  @Test
  void scansMapsContainingStrings() {
    var obj = new MapHolder(Map.of("k1", "safe", "k2", "drop index"));

    aspect.scanArguments(new Object[]{obj});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop").contains("[k2]"));
  }

  @Test
  void scansArraysContainingStrings() {
    var obj = new ArrayHolder(new String[]{"ok", "DROP TABLE", "x"});

    aspect.scanArguments(new Object[]{obj});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("DROP").contains("[1]"));
  }

  @Test
  void handlesCyclicObjectGraphsWithoutStackOverflow() {
    CyclicA a = new CyclicA();
    CyclicB b = new CyclicB();
    a.b = b;
    b.a = a;
    a.payload = "delete from t";

    aspect.scanArguments(new Object[]{a});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg -> assertThat(msg).contains("delete"));
  }

  @Test
  void scansFieldsOnTypesAnnotatedWithScanForSql() {
    var annotated = new AnnotatedPojo("drop view");

    aspect.scanArguments(new Object[]{annotated});

    assertThat(appender.list).hasSize(1);
  }

  @Test
  void respectsFieldAnnotationWhenClassNotAnnotated() {
    var pojo = new FieldAnnotatedPojo("ok", "drop trigger");

    aspect.scanArguments(new Object[]{pojo});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop").contains("danger"));
  }

  @Test
  void doesNotScanPrimitiveOrWrapperTypes() {
    var pojo = new PrimitiveHolder(42, true, 1.0, "drop table y");

    aspect.scanArguments(new Object[]{pojo});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg -> assertThat(msg).contains("drop"));
  }

  @Test
  void scanParameterAnnotationsTriggersScan() {
    JoinPoint jp = Mockito.mock(JoinPoint.class);
    MethodSignature sig = Mockito.mock(MethodSignature.class);

    Mockito.when(jp.getSignature()).thenReturn(sig);
    Mockito.when(sig.getMethod()).thenReturn(TestMethods.class.getDeclaredMethods()[0]);
    Mockito.when(jp.getArgs()).thenReturn(new Object[]{"delete from data"});

    aspect.scanForSqlController(jp);

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg -> assertThat(msg).contains("delete").contains("arg[0]"));
  }

  // -------------------------------------------------------------------------
  //  TESTS FOR repositorySaveOrUpdate ADVICE
  // -------------------------------------------------------------------------

  @Test
  void repositorySaveMethodTriggersScan() throws Exception {
    // object containing a SQL injection payload
    var entity = new AnnotatedPojo("drop schema test");

    JoinPoint jp = mockRepositoryJoinPoint("save", new Class[]{Object.class}, new Object[]{entity});

    aspect.scanForSqlDb(jp);

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop").contains("text"));
  }

  @Test
  void repositoryUpdateMethodTriggersScan() throws Exception {
    var entity = new FieldAnnotatedPojo("good", "drop table x");

    JoinPoint jp = mockRepositoryJoinPoint("updateCustomer", new Class[]{Object.class}, new Object[]{entity});

    aspect.scanForSqlDb(jp);

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(msg ->
            assertThat(msg).contains("drop").contains("danger"));
  }

  @Test
  void nonSaveOrUpdateRepositoryMethodsAreIgnored() throws Exception {
    // "findAll" should NOT match save*/update* pointcut
    JoinPoint jp = mockRepositoryJoinPoint("findAll", new Class[]{}, new Object[]{});

    aspect.scanForSqlDb(jp);

    assertThat(appender.list).isEmpty();
  }

  @Test
  void saveDoesNotScanUnannotatedTypesUnlessFieldAnnotated() throws Exception {
    var obj = new UnannotatedRequest("drop everything");

    JoinPoint jp = mockRepositoryJoinPoint("saveItem", new Class[]{Object.class}, new Object[]{obj});

    aspect.scanForSqlDb(jp);

    assertThat(appender.list).isEmpty(); // correct: type is not annotated & no annotated fields
  }

  private JoinPoint mockRepositoryJoinPoint(
      String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {

    Method method = FakeRepository.class.getDeclaredMethod(methodName, paramTypes);

    JoinPoint jp = Mockito.mock(JoinPoint.class);
    MethodSignature sig = Mockito.mock(MethodSignature.class);

    Mockito.when(jp.getSignature()).thenReturn(sig);
    Mockito.when(sig.getMethod()).thenReturn(method);
    Mockito.when(jp.getArgs()).thenReturn(args);

    return jp;
  }

  // These method signatures are used for reflection in the JoinPoint mocks
  static class FakeRepository {
    public void save(Object o) {}
    public void saveItem(Object o) {}
    public void updateCustomer(Object o) {}
    public void findAll() {}
  }


  // -------------------------------------------------------------------------
  //  SUPPORTING TEST TYPES
  // -------------------------------------------------------------------------

  @ScanForSql
  record CreateCustomerRequest(String name, String email, String comments) {}

  record FeedbackRequest(String name, @ScanForSql String message) {}

  record UnannotatedRequest(String payload) {}

  @ScanForSql
  static class NestedRoot { NestedLevel1 level1; NestedRoot(NestedLevel1 l) { level1 = l; } }
  static class NestedLevel1 { NestedLevel2 level2; NestedLevel1(NestedLevel2 l) { level2 = l; } }
  static class NestedLevel2 { @ScanForSql String level2Field; NestedLevel2(String s) { level2Field = s; } }

  static class ListHolder {
    @ScanForSql List<String> list;
    ListHolder(List<String> list) { this.list = list; }
  }

  static class MapHolder {
    @ScanForSql Map<String,String> map;
    MapHolder(Map<String, String> map) { this.map = map; }
  }

  static class ArrayHolder {
    @ScanForSql String[] arr;
    ArrayHolder(String[] arr) { this.arr = arr; }
  }

  static class CyclicA { CyclicB b; @ScanForSql String payload; }
  static class CyclicB { CyclicA a; }

  @ScanForSql
  static class AnnotatedPojo {
    String text;
    AnnotatedPojo(String t) { text = t; }
  }

  static class FieldAnnotatedPojo {
    String safe;
    @ScanForSql String danger;
    FieldAnnotatedPojo(String safe, String danger) {
      this.safe = safe; this.danger = danger;
    }
  }

  static class PrimitiveHolder {
    int i; boolean b; double d;
    @ScanForSql String val;
    PrimitiveHolder(int i, boolean b, double d, String val) {
      this.i = i; this.b = b; this.d = d; this.val = val;
    }
  }

  static class TestMethods {
    public void methodWithParamAnnotation(@ScanForSql String x) {}
  }
}
