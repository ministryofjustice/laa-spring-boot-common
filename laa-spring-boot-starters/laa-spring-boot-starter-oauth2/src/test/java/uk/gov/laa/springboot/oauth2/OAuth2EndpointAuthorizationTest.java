package uk.gov.laa.springboot.oauth2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.laa.springboot.oauth2.testsupport.StubJwtDecoder;
import uk.gov.laa.springboot.oauth2.testsupport.StubJwtToken;

@SpringBootTest
@AutoConfigureMockMvc
@Import(Oauth2EndpointAuthorizationTest.StubAuthServiceConfiguration.class)
class Oauth2EndpointAuthorizationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
    mockMvc.perform(get("/resource1/requires-group1-role"))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.detail").value("Unauthorized"));
  }

  @Test
  void unprotectedEndpointWithoutTokenIsAccessible() throws Exception {
    mockMvc.perform(get("/resource1/unrestricted"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  void roleMappingAllowsConfiguredRole() throws Exception {
    mockMvc.perform(get("/resource1/requires-group1-role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer role-group1"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  void roleMappingRejectsMissingRole() throws Exception {
    mockMvc.perform(get("/resource1/requires-group1-role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer scope-read"))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.title").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.detail").value("Access Denied"));
  }

  @Test
  void scopeMappingAllowsConfiguredScope() throws Exception {
    mockMvc.perform(get("/resource1/requires-scope")
            .header(HttpHeaders.AUTHORIZATION, "Bearer scope-read"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  void scopeMappingRejectsMissingScope() throws Exception {
    mockMvc.perform(get("/resource1/requires-scope")
            .header(HttpHeaders.AUTHORIZATION, "Bearer role-group1"))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.title").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.detail").value("Access Denied"));
  }

  @Test
  void methodSpecificScopeAllowsConfiguredMethods() throws Exception {
    mockMvc.perform(get("/resource1/method-scope")
            .header(HttpHeaders.AUTHORIZATION, "Bearer scope-method"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    mockMvc.perform(post("/resource1/method-scope")
            .header(HttpHeaders.AUTHORIZATION, "Bearer scope-method"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  void methodSpecificScopeRejectsOtherMethods() throws Exception {
    mockMvc.perform(patch("/resource1/method-scope")
            .header(HttpHeaders.AUTHORIZATION, "Bearer scope-method"))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }

  @TestConfiguration
  static class StubAuthServiceConfiguration {

    @Bean
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
              Map.of()));
    }
  }
}
