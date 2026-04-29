package uk.gov.laa.springboot.oauth2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts configured role and scope claims into Spring Security authorities.
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final Oauth2AuthorizationProperties properties;

  public JwtAuthoritiesConverter(Oauth2AuthorizationProperties properties) {
    this.properties = properties;
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    readAsStringCollection(jwt.getClaims(), properties.getRolesClaimPath()).stream()
        .map(role -> properties.getRoleAuthorityPrefix() + role)
        .map(SimpleGrantedAuthority::new)
        .forEach(authorities::add);
    readAsScopes(jwt.getClaims(), properties.getScopesClaimPath()).stream()
        .map(scope -> properties.getScopeAuthorityPrefix() + scope)
        .map(SimpleGrantedAuthority::new)
        .forEach(authorities::add);
    return authorities;
  }

  private Collection<String> readAsScopes(Map<String, Object> claims, String claimPath) {
    Object value = resolveClaim(claims, claimPath);
    if (value == null) {
      return Collections.emptySet();
    }
    if (value instanceof Collection<?> items) {
      return items.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .toList();
    }
    return Arrays.stream(value.toString().split("\\s+"))
        .filter(scope -> !scope.isBlank())
        .toList();
  }

  private Collection<String> readAsStringCollection(Map<String, Object> claims, String claimPath) {
    Object value = resolveClaim(claims, claimPath);
    if (value == null) {
      return Collections.emptySet();
    }
    if (value instanceof Collection<?> items) {
      return items.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .toList();
    }
    if (value.getClass().isArray()) {
      return Arrays.stream((Object[]) value)
          .filter(Objects::nonNull)
          .map(Object::toString)
          .toList();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return List.of(stringValue);
    }
    return Collections.emptySet();
  }

  private Object resolveClaim(Map<String, Object> claims, String claimPath) {
    Object current = claims;
    for (String element : claimPath.split("\\.")) {
      if (!(current instanceof Map<?, ?> map)) {
        return null;
      }
      current = map.get(element);
      if (current == null) {
        return null;
      }
    }
    return current;
  }
}
