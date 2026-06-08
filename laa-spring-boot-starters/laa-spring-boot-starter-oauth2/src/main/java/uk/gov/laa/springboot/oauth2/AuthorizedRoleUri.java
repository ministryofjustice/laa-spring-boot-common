package uk.gov.laa.springboot.oauth2;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Holds a URI pattern and optional HTTP methods for role/scope authorization.
 *
 * @param uri the URI pattern
 * @param methods allowed HTTP methods, or {@code null} for all
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public record AuthorizedRoleUri(String uri, String[] methods) {

  /**
   * Normalizes configured HTTP methods for consistent matching.
   */
  public AuthorizedRoleUri {
    if (methods != null) {
      methods = Arrays.stream(methods)
          .filter(Objects::nonNull)
          .map(AuthorizedRoleUri::normalizeMethod)
          .filter(method -> !method.isEmpty())
          .toArray(String[]::new);
      if (methods.length == 0) {
        methods = null;
      }
    }
  }

  /**
   * Determines whether this mapping allows the request method.
   *
   * @param requestMethod incoming HTTP method
   * @return {@code true} when the mapping applies
   */
  public boolean matchesMethod(String requestMethod) {
    if (methods == null || methods.length == 0) {
      return true;
    }
    if (requestMethod == null) {
      return false;
    }
    String normalizedMethod = normalizeMethod(requestMethod);
    return Arrays.stream(methods).anyMatch(method -> method.equals(normalizedMethod));
  }

  private static String normalizeMethod(String method) {
    return method.trim().toUpperCase(Locale.ROOT);
  }
}
