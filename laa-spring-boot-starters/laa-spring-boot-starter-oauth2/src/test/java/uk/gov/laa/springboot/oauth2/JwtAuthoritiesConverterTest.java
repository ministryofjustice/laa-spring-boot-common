package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthoritiesConverterTest {

  @Test
  void supportsNestedRolesClaimPath() {
    Oauth2AuthorizationProperties properties = new Oauth2AuthorizationProperties();
    properties.setRolesClaimPath("realm_access.roles");
    properties.setScopesClaimPath("scope");

    Jwt jwt = new Jwt(
        "nested-token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "none"),
        Map.of(
            "sub", "user-1",
            "scope", "claims:read",
            "realm_access", Map.of("roles", new String[] {"GROUP1"})));

    JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter(properties);

    assertThat(converter.convert(jwt).stream()
        .map(authority -> authority.getAuthority())
        .collect(Collectors.toSet()))
        .containsExactlyInAnyOrder("ROLE_GROUP1", "SCOPE_claims:read");
  }
}
