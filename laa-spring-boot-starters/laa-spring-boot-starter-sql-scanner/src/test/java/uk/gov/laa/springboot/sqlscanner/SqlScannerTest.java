package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlScannerTest {

  private final SqlScanner scanner = new SqlScanner();

  // =====================================================
  // Basic SQL keywords & statements
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "select * from users",
      "INSERT into audit_log values (1)",
      "update users set enabled = true",
      "delete from users where id = 1",
      "alter table claims",
      "DROP table users"
  })
  void detectsBasicSqlStatements(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // UNION & schema enumeration attacks
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "union select password from users",
      "' UNION SELECT username, password FROM users--",
      "' UNION SELECT table_name, column_name FROM information_schema.columns--",
      "'; SELECT * FROM information_schema.tables; --"
  })
  void detectsUnionAndSchemaEnumeration(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // Logical operator abuse (AND / OR)
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "status = 'A' or role = 'ADMIN'",
      "id = 1 AND enabled = true",
      "user_id = 10 or username = 'admin'",
      "user_id = 10 OR username = 'admin'"
  })
  void detectsLogicalOperatorsUsedAsSql(String input) {
    assertThat(scanner.scan(input))
        .contains("logical operator (AND/OR)");
  }

  // =====================================================
  // Always-true / boolean bypass conditions
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "' OR '1'='1' --",
      "' AND '1'='1' --",
      "or 1=1",
      "OR    1 = 1",
      "or 1 = 1 -- comment",
      "' OR 1=1 /*comment*/--",
      "'/**/OR/**/1=1--",
      "%27%20OR%201=1--"
  })
  void detectsAlwaysTrueConditions(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // LIKE / wildcard injection
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "' OR name LIKE '%'",
      " OR name LIKE '%",
      "and title like '%admin%'"
  })
  void detectsLikeBasedInjection(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // Comment-based injection
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "-- drop table users",
      "1=1 -- comment",
      "test' --",
      "abc'/*",
      "/* delete from users */"
  })
  void detectsSqlComments(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // Stacked queries & command execution
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "Robert'); DROP TABLE Students;--",
      "'; DROP TABLE users; --",
      "'; INSERT INTO audit_log(message) VALUES('hacked'); --",
      "'; UPDATE users SET role='admin' WHERE username='user'; --",
      "'; DELETE FROM users WHERE '1'='1'; --",
      "'; EXEC xp_cmdshell('dir'); --",
      "'; CALL system('ls'); --",
      "'; EXECUTE IMMEDIATE 'DROP TABLE accounts'; --",
      "'); COPY (SELECT '') TO PROGRAM 'ls'; --",
      "'; SHUTDOWN; --"
  })
  void detectsStackedAndCommandExecutionAttacks(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // Time-based attacks
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "'; WAITFOR DELAY '0:0:5'--",
      "' OR SLEEP(5)--",
      "1; SELECT pg_sleep(5); --"
  })
  void detectsTimeBasedAttacks(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // =====================================================
  // False positives – natural language
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "hello world",
      "Advice and Assistance",
      "Private Family LH Fixed Fee - Children or Finance",
      "Health and Social Care",
      "Children or Finance",
      "A and B",
      "AandB",
      "Terms and Conditions",
      "select a",
      "drop a ball"
  })
  void doesNotDetectNaturalLanguage(String input) {
    assertThat(scanner.scan(input)).isEmpty();
  }

  // =====================================================
  // False positives – hyphens & punctuation
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "-",
      "--",
      "---",
      "LH Fixed Fee - Children",
      "LH Fixed Fee-Children",
      "Range 2023--2024",
      "Range 2023 -- 2024"
  })
  void doesNotDetectHyphensAsSqlComments(String input) {
    assertThat(scanner.scan(input)).isEmpty();
  }

  // =====================================================
  // Null / blank handling
  // =====================================================

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "   ",
      "\n\t"
  })
  void returnsEmptyForBlankInput(String input) {
    assertThat(scanner.scan(input)).isEmpty();
  }

  @Test
  void returnsEmptyForNullInput() {
    assertThat(scanner.scan(null)).isEmpty();
  }
}
