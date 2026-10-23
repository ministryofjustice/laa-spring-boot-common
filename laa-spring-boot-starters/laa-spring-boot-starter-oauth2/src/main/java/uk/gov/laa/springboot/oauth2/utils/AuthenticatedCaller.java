package uk.gov.laa.springboot.oauth2.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Immutable, minimum-necessary projection of the authenticated caller's validated token claims. */
public record AuthenticatedCaller(
    Optional<UUID> actorId,
    Set<String> delegatedScopes,
    Set<String> providerOfficeCodes,
    Set<String> applicationRoles) {

  /** Normalises claim values and defensively copies caller facts for command transport. */
  public AuthenticatedCaller {
    actorId = Objects.requireNonNull(actorId, "actorId must not be null");
    delegatedScopes = normalise(delegatedScopes);
    providerOfficeCodes = normalise(providerOfficeCodes);
    applicationRoles = normalise(applicationRoles);
  }

  private static Set<String> normalise(Set<String> values) {
    if (values == null) {
      return Set.of();
    }
    return values.stream()
        .filter(value -> value != null && !value.isBlank())
        .map(String::trim)
        .collect(Collectors.toUnmodifiableSet());
  }
}