package uk.gov.laa.springboot.sqlscanner;

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

      new SqlPattern("union",
          Pattern.compile("\\bunion\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern(
          "logical operator (AND/OR)", LOGICAL_OPERATOR_PATTERN
      ),

      new SqlPattern("Always true conditions (OR 1=1)",
          Pattern.compile("\\bor\\s+1\\s*=\\s*1\\b", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("SQL line comment",
          Pattern.compile("(^|\\s)--\\s+[A-Za-z]", Pattern.CASE_INSENSITIVE)),

      new SqlPattern("SQL block comment",
          Pattern.compile("/\\*.*?\\*/",
              Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),

      new SqlPattern("statement terminator (;)",
          Pattern.compile(";"))
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

    for (SqlPattern sqlPattern : SUSPICIOUS_PATTERNS) {
      if (sqlPattern.pattern().matcher(value).find()) {
        return Optional.of(sqlPattern.label());
      }
    }

    return Optional.empty();
  }
}
