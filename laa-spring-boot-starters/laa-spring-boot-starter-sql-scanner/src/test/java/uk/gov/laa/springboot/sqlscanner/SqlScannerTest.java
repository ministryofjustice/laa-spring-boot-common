package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlScannerTest {

  private final SqlScanner scanner = new SqlScanner();

  @Test
  void returnsPatternWhenSqlLikeContentFound() {
    assertThat(scanner.scan("DROP table users")).contains("drop ");
  }

  @Test
  void returnsEmptyWhenValueIsSafe() {
    assertThat(scanner.scan("hello world")).isEmpty();
  }
}
