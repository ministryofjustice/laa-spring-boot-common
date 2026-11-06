package uk.gov.laa.springboot.oauth2.utils;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Projects validated request-token claims into immutable command caller facts. */
@Component
public class AuthenticatedCallerFactory {

  /** Returns the current authenticated caller without exposing the raw JWT to command handling. */
  public AuthenticatedCaller currentCaller() {
    return new AuthenticatedCaller(
        actorId(),
        SecurityHelper.getProfileId(),
        SecurityHelper.getFirmCode(),
        normalise(SecurityHelper.getScopes()),
        normalise(SecurityHelper.getLaaAccounts()),
        normalise(SecurityHelper.getLaaAppRoles()));
  }

  private UUID actorId() {
    return requireUuid(SecurityHelper.getEntraOid().orElseThrow(
        () -> new IllegalArgumentException("JWT 'oid' claim must be present")));
  }

  private UUID requireUuid(String value) {
    if (value == null) {
      throw new IllegalArgumentException("JWT 'oid' claim must not be null");
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(
          "JWT 'oid' claim must be a valid UUID: " + value, exception);
    }
  }

  private static Set<String> normalise(Collection<String> values) {
    if (values == null) {
      return Set.of();
    }
    return values.stream()
        .filter(StringUtils::hasText)
        .map(String::trim)
        .collect(Collectors.toUnmodifiableSet());
  }
}