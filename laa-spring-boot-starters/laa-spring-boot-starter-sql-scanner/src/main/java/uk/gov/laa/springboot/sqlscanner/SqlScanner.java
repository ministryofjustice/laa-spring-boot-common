package uk.gov.laa.springboot.sqlscanner;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Performs a lightweight scan of {@link String} values to highlight SQL-like patterns
 * while avoiding common English false positives.
 *
 * <p>
 * Uses regex patterns with word boundaries to identify potentially malicious SQL patterns
 * including
 * - SQL commands (SELECT, INSERT, UPDATE, DELETE)
 * - Schema modification commands (DROP, TRUNCATE, ALTER)
 * - SQL injection patterns (UNION, AND/OR operators, always true conditions)
 * - SQL comments (line and block)
 * - Statement terminators
 *
 * <p>
 * Designed to reduce false positives by:
 * - Using word boundaries to avoid matching within words
 * - Only flagging AND/OR when used in SQL expression context
 * - Careful handling of comment syntax and punctuation
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
   * List of SQL patterns to scan for, consisting of:
   * - Basic SQL commands (SELECT, INSERT, UPDATE, DELETE, etc)
   * - Schema modification commands (DROP, TRUNCATE, ALTER)
   * - SQL injection patterns (UNION, AND/OR operators, always true conditions)
   * - SQL comments (line and block)
   * - Statement terminators
   * Each pattern is defined with a descriptive label and a regex Pattern
   * that matches the suspicious content while minimizing false positives.
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
