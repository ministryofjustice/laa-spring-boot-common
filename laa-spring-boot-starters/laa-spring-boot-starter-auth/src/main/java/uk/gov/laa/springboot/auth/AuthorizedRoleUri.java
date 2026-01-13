package uk.gov.laa.springboot.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Holds a uri pattern and optional HTTP methods for role-based authorization.
 *
 * @param uri the uri pattern that is accessible to clients that have this role
 * @param methods the HTTP methods that are permitted for this uri pattern, or {@code null} for all
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public record AuthorizedRoleUri(String uri, String[] methods) {

  /**
   * Normalize configured methods for consistent matching.
   *
   * @param uri the uri pattern for the role
   * @param methods the HTTP methods that are permitted for this uri pattern
   */
  public AuthorizedRoleUri {
    if (methods != null) {
      methods = Arrays.stream(methods)
          .filter(Objects::nonNull)
          .map(String::trim)
          .filter(method -> !method.isEmpty())
          .map(method -> method.toUpperCase(Locale.ROOT))
          .toArray(String[]::new);
      if (methods.length == 0) {
        methods = null;
      }
    }
  }

  /**
   * Determine whether the configured methods permit the supplied request method.
   *
   * @param requestMethod the HTTP method of the incoming request
   * @return {@code true} if all methods are allowed or the request method is matched
   */
  public boolean matchesMethod(String requestMethod) {
    if (methods == null || methods.length == 0) {
      return true;
    }
    if (requestMethod == null) {
      return false;
    }
    String normalizedMethod = requestMethod.toUpperCase(Locale.ROOT);
    return Arrays.stream(methods).anyMatch(method -> method.equals(normalizedMethod));
  }
}
