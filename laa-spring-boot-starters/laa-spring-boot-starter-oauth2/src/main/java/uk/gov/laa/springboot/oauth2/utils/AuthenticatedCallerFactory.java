package uk.gov.laa.springboot.oauth2.utils;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Projects validated request-token claims into immutable command caller facts. */
@Component
public class AuthenticatedCallerFactory {

  /** Returns the current authenticated caller without exposing the raw JWT to command handling. */
  public AuthenticatedCaller currentCaller() {
    return new AuthenticatedCaller(
        actorId(),
        SecurityHelper.getScopes(),
        new LinkedHashSet<>(SecurityHelper.getLaaAccounts()),
        new LinkedHashSet<>(SecurityHelper.getLaaAppRoles()));
  }

  private Optional<UUID> actorId() {
    return SecurityHelper.getEntraOid().flatMap(this::asUuid);
  }

  private Optional<UUID> asUuid(String value) {
    try {
      return Optional.of(UUID.fromString(value));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}