package uk.gov.laa.springboot.oauth2;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Evaluates endpoint access by comparing configured role/scope mappings with authorities.
 */
@Slf4j
public class EndpointAccessManager {

  private final Oauth2AuthorizationProperties properties;
  private final ObjectMapper objectMapper;

  @Getter
  private String[] unprotectedUris;

  @Getter
  private Set<AuthorizedAuthority> authorizedRoles;

  @Getter
  private Set<AuthorizedAuthority> authorizedScopes;

  public EndpointAccessManager(
      Oauth2AuthorizationProperties properties,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  private void initialize() {
    unprotectedUris = properties.getUnprotectedUris();
    authorizedRoles = parseMappings(properties.getAuthorizedRoles(), "authorizedRoles");
    authorizedScopes = parseMappings(properties.getAuthorizedScopes(), "authorizedScopes");
    logRegisteredMappings("Authorized Role Registered: '{}'", authorizedRoles);
    logRegisteredMappings("Authorized Scope Registered: '{}'", authorizedScopes);
    if (authorizedRoles.isEmpty() && authorizedScopes.isEmpty()) {
      throw new InvalidPropertyException(
          Oauth2AuthorizationProperties.class,
          "authorizedRoles/authorizedScopes",
          "At least one authorized role or scope mapping must be configured.");
    }
  }

  private Set<AuthorizedAuthority> parseMappings(String mappings, String propertyName) {
    if (mappings == null || mappings.isBlank()) {
      return Collections.emptySet();
    }
    try {
      Set<AuthorizedAuthority> entries = objectMapper.readValue(
          mappings,
          new TypeReference<>() {
          });
      return entries == null ? Collections.emptySet() : entries;
    } catch (JacksonException ex) {
      throw new InvalidPropertyException(
          Oauth2AuthorizationProperties.class,
          propertyName,
          "Could not parse JSON value.");
    }
  }

  private void logRegisteredMappings(String messagePattern, Set<AuthorizedAuthority> mappings) {
    for (AuthorizedAuthority mapping : mappings) {
      log.info(messagePattern, mapping.name());
    }
  }

  /**
   * Determines if any configured role or scope mapping authorizes this request.
   *
   * @param authorities authorities from the authenticated principal
   * @param request incoming request
   * @return {@code true} when at least one matching mapping is found
   */
  public boolean isRequestAuthorized(Collection<? extends GrantedAuthority> authorities,
                                     HttpServletRequest request) {
    if (authorities == null || authorities.isEmpty()) {
      return false;
    }

    Set<String> normalizedRoleNames = normalizeAuthorities(
        authorities,
        properties.getRoleAuthorityPrefix());
    Set<String> normalizedScopeNames = normalizeAuthorities(
        authorities,
        properties.getScopeAuthorityPrefix());

    return matchesRequest(authorizedRoles, normalizedRoleNames, request)
        || matchesRequest(authorizedScopes, normalizedScopeNames, request);
  }

  private Set<String> normalizeAuthorities(Collection<? extends GrantedAuthority> authorities,
                                           String prefix) {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(Objects::nonNull)
        .map(authority -> authority.startsWith(prefix)
            ? authority.substring(prefix.length())
            : authority)
        .collect(Collectors.toSet());
  }

  private boolean matchesRequest(Set<AuthorizedAuthority> mappings,
                                 Set<String> authorityNames,
                                 HttpServletRequest request) {
    return mappings.stream()
        .filter(mapping -> authorityNames.contains(mapping.name()))
        .flatMap(mapping -> Arrays.stream(mapping.uris()))
        .anyMatch(uri -> matchesRequest(uri, request));
  }

  private boolean matchesRequest(AuthorizedRoleUri roleUri, HttpServletRequest request) {
    if (roleUri == null || roleUri.uri() == null) {
      return false;
    }
    if (!PathPatternRequestMatcher.withDefaults().matcher(roleUri.uri()).matches(request)) {
      return false;
    }
    return roleUri.matchesMethod(request.getMethod());
  }
}
