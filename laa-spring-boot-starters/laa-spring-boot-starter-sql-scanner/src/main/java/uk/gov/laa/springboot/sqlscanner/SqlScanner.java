package uk.gov.laa.springboot.sqlscanner;

import java.util.List;
import java.util.Optional;

/**
 * Performs a lightweight scan of {@link String} values to highlight SQL-like patterns.
 */
public class SqlScanner {

  private static final List<String> SUSPICIOUS_PATTERNS = List.of(
      "select ", "insert ", "update ", "delete ", "drop ", "truncate ", "alter ",
      "union ", "--", ";", "/*", "*/", " or ", " and ", " or 1=1"
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

    String lower = value.toLowerCase();

    for (String pattern : SUSPICIOUS_PATTERNS) {
      if (lower.contains(pattern)) {
        return Optional.of(pattern);
      }
    }

    return Optional.empty();
  }
}
