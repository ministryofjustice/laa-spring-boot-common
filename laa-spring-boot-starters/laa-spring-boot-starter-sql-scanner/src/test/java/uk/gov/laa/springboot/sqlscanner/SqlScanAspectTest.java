package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class SqlScanAspectTest {

  private final SqlScanAspect aspect = new SqlScanAspect(new SqlScanner());

  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUpAppender() {
    appender = new ListAppender<>();
    appender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(SqlScanAspect.class);
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDownAppender() {
    if (appender != null) {
      Logger logger = (Logger) LoggerFactory.getLogger(SqlScanAspect.class);
      logger.detachAppender(appender);
    }
  }

  @Test
  void scansEveryComponentOfAnnotatedRecord() {
    var request = new CreateCustomerRequest("Sam", "sam@example.com", "drop table users");

    aspect.scanArguments(new Object[]{request});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(message -> assertThat(message).contains("drop ").contains("comments"));
  }

  @Test
  void scansOnlyAnnotatedRecordComponentsWhenTypeNotAnnotated() {
    var request = new FeedbackRequest("Sam", "hello; delete from accounts");

    aspect.scanArguments(new Object[]{request});

    assertThat(appender.list)
        .singleElement()
        .extracting(ILoggingEvent::getFormattedMessage)
        .satisfies(message -> assertThat(message).contains("delete").contains("message"));
  }

  @Test
  void ignoresUnannotatedTypes() {
    aspect.scanArguments(new Object[]{new UnannotatedRequest("select * from dual")});

    assertThat(appender.list).isEmpty();
  }

  @ScanForSql
  record CreateCustomerRequest(String name, String email, String comments) {
  }

  record FeedbackRequest(String name, @ScanForSql String message) {
  }

  record UnannotatedRequest(String payload) {
  }
}
