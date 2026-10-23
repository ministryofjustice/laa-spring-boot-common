package uk.gov.laa.springboot.oauth2;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import uk.gov.laa.springboot.oauth2.testsupport.StubJwtDecoder;
import uk.gov.laa.springboot.oauth2.testsupport.StubJwtToken;

@Configuration
class TestJwtConfiguration {

  @Bean
  @Primary
  JwtDecoder jwtDecoder() {
    return StubJwtDecoder.of(
        new StubJwtToken(
            "role-group1",
            "client-role",
            new String[] {"GROUP1"},
            null,
            Map.of()),
        new StubJwtToken(
            "scope-read",
            "client-scope-read",
            null,
            new String[] {"claims:read"},
            Map.of()),
        new StubJwtToken(
            "scope-method",
            "client-scope-method",
            null,
            new String[] {"claims:method"},
            Map.of()),
        new StubJwtToken(
            "obo-without-roles",
            "client-no-roles",
            null,
            null,
            Map.of("oid", "user-1")));
  }

  @Bean
  JwtDecoder xAuthorizationJwtDecoder() {
    return StubJwtDecoder.of(
        new StubJwtToken(
            "x-authorization-group1",
            "client-no-roles",
            null,
            null,
            Map.of("oid", "user-1", "LAA_APP_ROLES", List.of("GROUP1"))));
  }

  @Bean
  XAuthorizationFilter xAuthorizationFilter(
      @Qualifier("xAuthorizationJwtDecoder") JwtDecoder xAuthorizationJwtDecoder) {
    return new XAuthorizationFilter(xAuthorizationJwtDecoder, "ROLE_");
  }
}
