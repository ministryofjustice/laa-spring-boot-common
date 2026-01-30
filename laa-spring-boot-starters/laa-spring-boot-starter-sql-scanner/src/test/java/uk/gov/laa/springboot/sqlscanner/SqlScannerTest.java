package uk.gov.laa.springboot.sqlscanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlScannerTest {

  private final SqlScanner scanner = new SqlScanner();

  // -----------------------------
  // Positive cases (should match)
  // -----------------------------

  @Test
  void detectsDropStatement() {
    assertThat(scanner.scan("DROP table users")).contains("drop");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "select * from users",
      "INSERT into audit_log values (1)",
      "update users set enabled = true",
      "delete from users where id = 1",
      "union select password from users",
      "alter table claims"
  })
  void detectsSqlKeywords(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "status = 'A' or role = 'ADMIN'",
      "id = 1 AND enabled = true",
      "id = 1 and enabled = true",
      "user_id = 10 or username = 'admin'",
      "user_id = 10 OR username = 'admin'"
  })
  void detectsLogicalOperatorsUsedAsSql(String input) {
    assertThat(scanner.scan(input))
        .contains("logical operator (AND/OR)");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "or 1=1",
      "OR    1 = 1",
      "or 1 = 1 -- comment"
  })
  void detectsSqlAlwaysTrueConditions(String input) {
    assertThat(scanner.scan(input))
        .contains("Always true conditions (OR 1=1)");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "-- drop table users",
      "1=1 -- comment",
      "/* delete from users */"
  })
  void detectsSqlComments(String input) {
    assertThat(scanner.scan(input)).isPresent();
  }

  // --------------------------------
  // Negative cases (false positives)
  // --------------------------------

  @ParameterizedTest
  @ValueSource(strings = {
      "hello world",
      "Advice and Assistance",
      "Private Family LH Fixed Fee - Children or Finance",
      "Health and Social Care",
      "Children or Finance",
      "A and B",
      "AandB",
      "Terms and Conditions"
  })
  void doesNotDetectNaturalLanguageAndOr(String input) {
    assertThat(scanner.scan(input)).isEmpty();
  }

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
