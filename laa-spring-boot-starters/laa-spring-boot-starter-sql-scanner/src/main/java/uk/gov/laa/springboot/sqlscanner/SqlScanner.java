package uk.gov.laa.springboot.sqlscanner;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Performs a lightweight scan of {@link String} values to detect potential SQL injection attempts
 * and malicious SQL patterns while minimizing false positives in regular text.
 *
 * <p>
 * The scanner checks for multiple categories of suspicious patterns:
 * - Basic SQL commands (SELECT, INSERT, UPDATE, DELETE)
 * - Schema modification commands (DROP, TRUNCATE, ALTER)
 * - SQL injection techniques (UNION-based, time-based, comments)
 * - Logical operator abuse (AND/OR in SQL context)
 * - Command execution attempts (stored procedures, system commands)
 * - Information schema access
 * - SQL comment injection (line and block comments)
 * - Always-true conditions and statement chaining
 *
 * <p>
 * Implements several strategies to reduce false positives:
 * - Uses word boundaries to avoid matching within regular words
 * - Considers operator context for AND/OR detection
 * - Pattern matching respects proper SQL syntax and structure
 * - Careful handling of string literals, comments and statement terminators
 *
 * <p>
 * Additional features:
 * - URL decodes input strings before scanning to catch encoded attacks
 * - Case-insensitive pattern matching
 * - Handles malformed URL encodings gracefully
 */
public class SqlScanner {

  private record SqlPattern(String label, Pattern pattern) {}

  private static final String IDENTIFIER =
      "[A-Za-z_][A-Za-z0-9_\\.]*";

  private static final String OPERATOR =
      "(=|!=|<>|<|>|<=|>=|like|in)";

  private static final String VALUE =
      "('[^']*'|\\d+|" + IDENTIFIER + ")";

  private static final Pattern LOGICAL_OPERATOR_PATTERN =
      Pattern.compile(
          IDENTIFIER + "\\s*"
              + OPERATOR   + "\\s*"
              + VALUE      + "\\s+"
              + "\\b(and|or)\\b\\s+"
              + IDENTIFIER + "\\s*"
              + OPERATOR   + "\\s*"
              + VALUE,
          Pattern.CASE_INSENSITIVE
      );

  private static final Pattern LIKE_INJECTION_PATTERN =
      Pattern.compile(
          "\\b(or|and)\\b\\s+"
              + IDENTIFIER + "\\s+"
              + "like\\s+"
              + "'%?[^']*'?$",
          Pattern.CASE_INSENSITIVE
      );

  /**
   * List of SQL injection patterns and malicious SQL constructs to detect.
   *
   * <p>Pattern categories include:
   * <ul>
   *   <li>Basic SQL commands (SELECT, INSERT, UPDATE, DELETE)
   *   <li>Schema modification commands (DROP, TRUNCATE, ALTER)
   *   <li>UNION-based SQL injection attempts
   *   <li>Stacked SQL statements using semicolon chaining
   *   <li>Logical operator abuse (AND/OR in suspicious contexts)
   *   <li>LIKE operator injection patterns
   *   <li>Boolean-based bypasses and always-true conditions
   *   <li>Comment injection using -- and block comment styles
   *   <li>Time-based injection using SLEEP/WAITFOR
   *   <li>Command execution via stored procedures
   *   <li>Information schema enumeration attempts
   * </ul>
   *
   * <p>Each pattern is defined as a {@link SqlPattern} record containing:
   * <ul>
   *   <li>A descriptive label identifying the type of attack
   *   <li>A compiled regex Pattern that matches the suspicious syntax
   *   <li>Word boundary markers and case-insensitive matching
   *   <li>Context-aware detection to minimize false positives
   * </ul>
   */
  private static final List<SqlPattern> SUSPICIOUS_PATTERNS = List.of(

      new SqlPattern("select",
          Pattern.compile("\\bselect\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("insert",
          Pattern.compile("\\binsert\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("update",
          Pattern.compile("\\bupdate\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("delete",
          Pattern.compile("\\bdelete\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("drop",
          Pattern.compile("\\bdrop\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("truncate",
          Pattern.compile("\\btruncate\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("alter",
          Pattern.compile("\\balter\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern(
          "UNION-based injection",
          Pattern.compile("\\bunion\\b\\s+\\bselect\\b", Pattern.CASE_INSENSITIVE)
      ),

      new SqlPattern(
          "stacked SQL statements",
          Pattern.compile(
              ";\\s*(select|insert|update|delete|drop|alter|truncate|exec|call"
                  + "|shutdown|grant|revoke)",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern(
          "logical operator (AND/OR)", LOGICAL_OPERATOR_PATTERN
      ),

      new SqlPattern(
          "SQL injection fragment (LIKE)", LIKE_INJECTION_PATTERN
      ),

      new SqlPattern(
          "boolean bypass / always-true condition",
          Pattern.compile(
              "\\b(or|and)\\b\\s*(1\\s*=\\s*1|['\"%27]\\s*['\"\\d])",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern(
          "SQL comment injection",
          Pattern.compile(
              "("
                  + "['\"\\)]"
                  + "|"                                           // string / expression termination
                  + "\\b\\d+\\s*(=|!=|<>|<|>|<=|>=)\\s*\\d+"
                  + "|"                                           // numeric comparison (1=1)
                  + IDENTIFIER + "\\s*(=|!=|<>|<|>|<=|>=)"
                  + ")"                                           // column comparison
                  + "\\s*(?<!\\d)--(?!\\d)",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern("SQL block comment",
          Pattern.compile("/\\*.*?\\*/",
              Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),

      new SqlPattern(
          "SQL block comment injection",
          Pattern.compile(
              "(['\"\\)])\\s*/\\*",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern(
          "time-based SQL injection",
          Pattern.compile(
              "\\b(sleep|pg_sleep|waitfor\\s+delay)\\b",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern(
          "command execution / stored procedure abuse",
          Pattern.compile(
              "\\b(xp_cmdshell|execute\\s+immediate|copy\\s*\\(|system\\s*\\()",
              Pattern.CASE_INSENSITIVE
          )
      ),

      new SqlPattern(
          "schema enumeration",
          Pattern.compile(
              "information_schema\\.",
              Pattern.CASE_INSENSITIVE
          )
      )
  );

  /**
   * Checks the supplied value for SQL-like tokens.
   *
   * @param value the candidate value.
   * @return an {@link Optional} containing the matched pattern, otherwise empty.
   */
  public Optional<String> scan(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }

    String normalized = decode(value);

    for (SqlPattern sqlPattern : SUSPICIOUS_PATTERNS) {
      if (sqlPattern.pattern().matcher(normalized).find()) {
        return Optional.of(sqlPattern.label());
      }
    }

    return Optional.empty();
  }

  private String decode(String input) {
    try {
      return URLDecoder.decode(input, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ex) {
      // malformed encoding – return original safely
      return input;
    }
  }

}
