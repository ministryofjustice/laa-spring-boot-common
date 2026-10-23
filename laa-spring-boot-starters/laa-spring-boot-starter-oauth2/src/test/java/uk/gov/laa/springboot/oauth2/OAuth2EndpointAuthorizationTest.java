package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class Oauth2EndpointAuthorizationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityFilterChain securityFilterChain;

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
  void xAuthorizationRoleAllowsConfiguredRole() throws Exception {
    assertThat(securityFilterChain.getFilters()).anyMatch(XAuthorizationFilter.class::isInstance);
    assertThat(securityFilterChain.getFilters().indexOf(
        securityFilterChain.getFilters().stream()
            .filter(BearerTokenAuthenticationFilter.class::isInstance)
            .findFirst()
            .orElseThrow())).isLessThan(securityFilterChain.getFilters().indexOf(
                securityFilterChain.getFilters().stream()
                    .filter(XAuthorizationFilter.class::isInstance)
                    .findFirst()
                    .orElseThrow()));
    assertThat(securityFilterChain.getFilters().indexOf(
        securityFilterChain.getFilters().stream()
            .filter(XAuthorizationFilter.class::isInstance)
            .findFirst()
            .orElseThrow())).isLessThan(securityFilterChain.getFilters().indexOf(
                securityFilterChain.getFilters().stream()
                    .filter(AuthorizationFilter.class::isInstance)
                    .findFirst()
                    .orElseThrow()));

    mockMvc.perform(get("/resource1/requires-group1-role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer obo-without-roles")
            .header("X-Authorization", "x-authorization-group1"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  void xAuthorizationRoleIsRequiredWhenOboTokenHasNoConfiguredRole() throws Exception {
    mockMvc.perform(get("/resource1/requires-group1-role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer obo-without-roles"))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
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
}
