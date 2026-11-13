package uk.gov.laa.springboot.oauth2.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticatedCallerFactoryTest {

  private static final UUID ACTOR_ID = UUID.fromString("9a94c4a4-baf4-4c70-9254-b674dcedbf4e");

  private final AuthenticatedCallerFactory factory = new AuthenticatedCallerFactory();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void projectsProfileAndFirmClaims() {
    authenticate(Map.of(
        "oid", ACTOR_ID.toString(),
        "PROFILE_ID", "profile-1",
        "FIRM_CODE", "firm-1",
        "scp", List.of("scope:read ", " scope:write"),
        "LAA_ACCOUNTS", List.of("account-1", "account-2"),
        "LAA_APP_ROLES", List.of("CASEWORKER", "USER")));

    AuthenticatedCaller caller = factory.currentCaller();

    assertThat(caller.actorId()).isEqualTo(ACTOR_ID);
    assertThat(caller.profileId()).isEqualTo("profile-1");
    assertThat(caller.firmCode()).isEqualTo("firm-1");
    assertThat(caller.delegatedScopes()).containsExactlyInAnyOrder("scope:read", "scope:write");
    assertThat(caller.providerOfficeCodes()).containsExactlyInAnyOrder("account-1", "account-2");
    assertThat(caller.applicationRoles()).containsExactlyInAnyOrder("CASEWORKER", "USER");
  }

  @Test
  void usesNullForAbsentProfileAndFirmClaims() {
    authenticate(Map.of("oid", ACTOR_ID.toString()));

    AuthenticatedCaller caller = factory.currentCaller();

    assertThat(caller.profileId()).isNull();
    assertThat(caller.firmCode()).isNull();
  }

  private void authenticate(Map<String, Object> claims) {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "none")
        .claims(jwtClaims -> jwtClaims.putAll(claims))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}
